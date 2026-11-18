# Audio metadata parsing invariants

Status: pre-refactor record, written 2026-11-18 before any P0.6 work.
Scope: `Song.parse` and the two paths it dispatches to. This document is the
contract a future "turn the dual path into an interface" refactor must preserve.

The two paths are **not** two implementations of one behaviour. They read
different sources, produce different ids, normalise units differently, and
handle artwork with different policies. Treating them as interchangeable is the
single most likely way to change shipped behaviour during a refactor.

## 1. Entry point and dispatch

| Fact                                                                                                          | Evidence                                                                               |
| ------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| Single entry: `Song.parse(path, file, options)` returns a non-null `Song`                                     | `app/src/main/java/io/github/zyrouge/symphony/services/groove/Song.kt:101`             |
| Single production caller                                                                                      | `app/src/main/java/io/github/zyrouge/symphony/services/groove/MediaExposer.kt:133`     |
| Metaphony is attempted only when the setting is on (default `true`)                                           | `Song.kt:106`; `app/src/main/java/io/github/zyrouge/symphony/services/Settings.kt:328` |
| Metaphony returning `null` **or throwing** falls through to the retriever path, which always returns a `Song` | `Song.kt:106-116`                                                                      |
| A metaphony exception is logged as `Logger.error("Song", "could not parse using metaphony", err)`             | `Song.kt:112-114`                                                                      |

INV-1: setting off means the retriever path runs and metaphony is never touched.
INV-2: setting on with a failing metaphony means one error log, then the retriever path — never a crash, never a null result.
INV-3: `process` order is metaphony first, retriever second; nothing else may reorder this.

## 2. Separator handling (both paths, shared)

`ParseOptions` carries two regexes built from settings: `artistSeparatorRegex`
from `artistTagSeparators`, `genreSeparatorRegex` from `genreTagSeparators`
(`Song.kt:50-60`). `makeSeparatorsRegex` wraps each separator in
`Pattern.quote` and prefixes the alternation with a negative lookbehind for a
backslash: `(?<!\\)(a|b)` (`Song.kt:260-263`).

`parseMultiValue` trims the whole value, splits on the regex, trims each part,
drops empty parts, and collects into a `Set` (`Song.kt:265-281`).

INV-4: artists, composers and album artists use the **artist** regex; genres use the **genre** regex; this split holds on both paths (`Song.kt:161-164`, `237-240`).
INV-5: `parseMultiValue` semantics stay as they are; this is the only part of the file with test coverage today (7 cases in `app/src/test/java/io/github/zyrouge/symphony/services/groove/SongTest.kt`).

## 3. Ids

| Path      | Id                                                | Evidence      |
| --------- | ------------------------------------------------- | ------------- |
| metaphony | `symphony.groove.song.idGenerator.next()`         | `Song.kt:129` |
| retriever | `symphony.groove.song.idGenerator.next() + ".mr"` | `Song.kt:192` |

INV-6: the `.mr` suffix stays on the retriever path and stays absent on the metaphony path. It is the only externally visible marker of which path produced a row, so dropping it makes the two populations indistinguishable.

## 4. Cover artwork

Metaphony path (`Song.kt:130-153`):

- takes `metadata.pictures.firstOrNull()`; no picture means `coverFile = null`
- derives an extension from the picture mime: `image/jpg` and `image/jpeg` → `jpg`, `image/png` → `png`; **any other mime yields no cover at all**
- when `settings.artworkQuality.value.maxSide == null`: writes the picture bytes **verbatim** to `artworkCache` as `<id>.<extension>`, no decode, no re-encode
- otherwise: decodes with `BitmapFactory`, names the file `<id>.jpg`, resizes with `ImagePreserver.resize(bitmap, quality)` and compresses JPEG at quality 100

Retriever path (`Song.kt:193-203`):

- takes `retriever.embeddedPicture`; null means `coverFile = null`
- always names the file `<id>.jpg`, always decodes, always resizes, always JPEG 100 — there is no raw-copy branch and no png branch

INV-7: the raw-byte copy when `maxSide == null` is metaphony-only behaviour. A shared "write cover" helper must keep it reachable only from that path, or the app starts re-encoding artwork it currently stores untouched.
INV-8: the png/jpg extension choice and the "unknown mime drops the cover" rule are metaphony-only.
INV-9: the retriever path's unconditional JPEG conversion stays unconditional.
INV-10: cover file naming stays keyed by the song id, so `<id>.<ext>` and `<id>.jpg` never collide across paths because of INV-6.

## 5. Field mapping, path by path

| Song field                             | metaphony source                                                | retriever source                                                         |
| -------------------------------------- | --------------------------------------------------------------- | ------------------------------------------------------------------------ |
| `title`                                | `metadata.title` else `path.nameWithoutExtension`               | same key else `path.nameWithoutExtension`                                |
| `album`                                | `metadata.album`                                                | `METADATA_KEY_ALBUM`                                                     |
| `artists`, `composers`, `albumArtists` | `parseMultiValue(..., artistSeparatorRegex)`                    | same                                                                     |
| `genres`                               | `parseMultiValue(..., genreSeparatorRegex)`                     | same                                                                     |
| `trackNumber`, `trackTotal`            | `AudioMetadata.trackNumber/trackTotal`                          | `METADATA_KEY_CD_TRACK_NUMBER`, `METADATA_KEY_NUM_TRACKS`, `toIntOrNull` |
| `discNumber`                           | `AudioMetadata.discNumber`                                      | `METADATA_KEY_DISC_NUMBER`, `toIntOrNull`                                |
| `discTotal`                            | `AudioMetadata.discTotal`                                       | **always null**                                                          |
| `date`                                 | `AudioMetadata.date` (`LocalDate?`)                             | `runCatching { LocalDate.parse(date) }.getOrNull()`                      |
| `year`                                 | `metadata.date?.year`                                           | `METADATA_KEY_YEAR?.toIntOrNull()`                                       |
| `duration` (ms)                        | `lengthInSeconds * 1000` else `0`                               | `METADATA_KEY_DURATION` (already ms) else `0`                            |
| `bitrate` (bps)                        | `bitrate * 1000` (kbps source) else null                        | `METADATA_KEY_BITRATE` (already bps)                                     |
| `samplingRate`                         | `sampleRate.toLong()`                                           | only when `SDK_INT >= S`, else null                                      |
| `channels`                             | `metadata.channels`                                             | **always null**                                                          |
| `encoder`                              | `metadata.encoding`                                             | **always null**                                                          |
| `dateModified`, `size`, `uri`, `path`  | `file.lastModified`, `file.size`, `file.uri`, `path.pathString` | identical                                                                |

INV-11: `duration` is milliseconds and `bitrate` is bits per second on both paths; the multiplication by 1000 happens exactly once, on the metaphony side only.
INV-12: `discTotal`, `channels`, `encoder` stay null on the retriever path; `samplingRate` stays gated on API 31 there. Filling them in "for symmetry" is a behaviour change.
INV-13: `year` comes from the parsed date on metaphony and from its own tag on the retriever path; they can legitimately disagree.
INV-14: `title` falls back to the file name without extension on both paths.

## 6. Side effects

| Effect                        | metaphony                                               | retriever                                                   |
| ----------------------------- | ------------------------------------------------------- | ----------------------------------------------------------- |
| `lyricsCache.put(id, lyrics)` | yes, when `metadata.lyrics != null` (`Song.kt:154-156`) | never                                                       |
| `artworkCache` write          | yes when a cover exists                                 | yes when a cover exists                                     |
| `Logger.error` on failure     | yes, on exception only                                  | never (the path cannot signal failure; it returns a `Song`) |

INV-15: the lyrics cache write stays metaphony-only.
INV-16: a refactor must not move the lyrics write behind a shared helper that both paths call.

## 7. Verification status today

- Covered: `parseMultiValue` only, 7 parameterised cases.
- Not covered by any test: dispatch and fallback, id suffix, cover policy, unit normalisation, date/year resolution, cache side effects.
- Reason: `Song.parse` needs `Uri`, `Bitmap`/`BitmapFactory`, `DocumentFileX` and `MediaMetadataRetriever`, so it cannot run as a host JVM unit test without mocks, and this project has no instrumentation test for it.
- No on-device smoke test has been run for any of these paths.

## 8. What a P0.6 refactor has to satisfy

1. Every INV above holds after the change, stated one by one in the plan's success criteria.
2. A new seam (an interface returning plain data) must expose the raw values plus the per-path policy flags, not a "unified metadata" object that hides INV-12 and INV-13.
3. Any extracted pure logic — unit normalisation, id suffixing, title fallback, cover-extension decision — gets host JVM unit tests, because that is the only part that can be tested without Android.
4. Android-bound calls stay in the implementations; no Robolectric, no mocks of `MediaMetadataRetriever`, no test-only production hooks.
5. Manual/device verification remains mandatory for the cover-writing branch and for a real flac/ogg/mp3 parse, and the plan must say plainly that it cannot be automated here.
