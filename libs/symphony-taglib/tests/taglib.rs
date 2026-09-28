use std::fs;
#[cfg(unix)]
use std::fs::File;
#[cfg(unix)]
use std::io::{Read, Seek, SeekFrom};
#[cfg(unix)]
use std::os::unix::io::AsRawFd;
use std::path::{Path, PathBuf};
use std::sync::atomic::{AtomicU32, Ordering};

use symphony_taglib::taglib::TagLibFile;
use symphony_taglib::taglib_error::TagLibError;

static TEMP_TEST_DATA_DIR_ID: AtomicU32 = AtomicU32::new(0);

struct TempTestDataDir {
    path: PathBuf,
}

impl TempTestDataDir {
    fn new() -> TempTestDataDir {
        let path = std::env::temp_dir().join(format!(
            "symphony-taglib-tests-{}-{}",
            std::process::id(),
            TEMP_TEST_DATA_DIR_ID.fetch_add(1, Ordering::Relaxed)
        ));
        fs::create_dir_all(&path).expect("failed to create temporary test data directory");
        TempTestDataDir { path }
    }

    fn copy_file(&self, name: &str, source: &Path) -> std::io::Result<PathBuf> {
        let path = self.path.join(name);
        fs::copy(source, &path)?;
        Ok(path)
    }
}

impl Drop for TempTestDataDir {
    fn drop(&mut self) {
        let _ = fs::remove_dir_all(&self.path);
    }
}

fn taglib_test_data_dir() -> PathBuf {
    let path = Path::new(env!("CARGO_MANIFEST_DIR"))
        .join("..")
        .join("symphony-taglib-sys")
        .join("vendor")
        .join("taglib")
        .join("tests")
        .join("data");
    assert!(
        path.is_dir(),
        "missing vendor/taglib/tests/data directory, run `git submodule update --init`"
    );
    path
}

fn taglib_test_data_file(name: &str) -> PathBuf {
    let path = taglib_test_data_dir().join(name);
    assert!(path.is_file(), "missing vendor/taglib/tests/data/{name}");
    path
}

#[test]
fn from_path_opens_valid_mp3() {
    let dir = TempTestDataDir::new();
    let path = dir
        .copy_file("lame_cbr.mp3", &taglib_test_data_file("lame_cbr.mp3"))
        .expect("failed to copy fixture");
    let opened = TagLibFile::from_path(&path);
    assert!(
        opened.is_ok(),
        "expected fixture {path:?} to open: {opened:?}"
    );
}

#[test]
fn from_path_detects_mp3_without_extension() {
    let test_data_dir = TempTestDataDir::new();
    let path = test_data_dir
        .copy_file("track", &taglib_test_data_file("lame_vbr.mp3"))
        .expect("failed to copy fixture");
    let opened = TagLibFile::from_path(&path);
    assert!(
        opened.is_ok(),
        "expected fixture {path:?} to open: {opened:?}"
    );
}

#[test]
fn from_path_rejects_missing_file() {
    let path = taglib_test_data_dir().join("does-not-exist.mp3");
    let opened = TagLibFile::from_path(&path);
    assert_eq!(opened.unwrap_err(), TagLibError::InvalidFile);
}

#[test]
fn from_path_rejects_unsupported_content() {
    let path = taglib_test_data_file("unsupported-extension.xx");
    let opened = TagLibFile::from_path(&path);
    assert_eq!(opened.unwrap_err(), TagLibError::InvalidFile);
}

#[test]
fn from_path_rejects_invalid_path() {
    let empty = TagLibFile::from_path(Path::new(""));
    assert_eq!(empty.unwrap_err(), TagLibError::InvalidFile);
}

#[cfg(unix)]
#[test]
fn from_named_file_descriptor_rejects_interior_nul_in_name() {
    let opened = TagLibFile::from_named_file_descriptor(-1, true, "bad\0name.mp3");
    assert_eq!(opened.unwrap_err(), TagLibError::InvalidFile);
}

#[cfg(unix)]
#[test]
fn from_file_descriptor_rejects_invalid_descriptor() {
    let opened = TagLibFile::from_file_descriptor(-1, true);
    assert_eq!(opened.unwrap_err(), TagLibError::StreamCreationFailed);
}

#[cfg(unix)]
#[test]
fn from_file_descriptor_opens() {
    let file = File::open(taglib_test_data_file("mpeg2.mp3")).expect("failed to open fixture");
    let opened = TagLibFile::from_file_descriptor(file.as_raw_fd(), true);
    assert!(opened.is_ok(), "expected descriptor to open: {opened:?}");
}

#[cfg(unix)]
#[test]
fn from_file_descriptor_falls_back_to_read_only() {
    let file = File::open(taglib_test_data_file("bladeenc.mp3")).expect("failed to open fixture");
    let opened = TagLibFile::from_file_descriptor(file.as_raw_fd(), false);
    assert!(
        opened.is_ok(),
        "expected read-only descriptor to fall back: {opened:?}"
    );
}

#[cfg(unix)]
#[test]
fn from_named_file_descriptor_detects_by_name_extension() {
    let file = File::open(taglib_test_data_file("ape.mp3")).expect("failed to open fixture");

    let opened = TagLibFile::from_named_file_descriptor(file.as_raw_fd(), true, "song.mp3");

    assert!(
        opened.is_ok(),
        "expected named descriptor to open: {opened:?}"
    );
}

#[cfg(unix)]
#[test]
fn from_named_file_descriptor_detects_by_content_when_name_is_unknown() {
    let file = File::open(taglib_test_data_file("itunes10.mp3")).expect("failed to open fixture");
    let opened = TagLibFile::from_named_file_descriptor(file.as_raw_fd(), true, "song.xyz");
    assert!(
        opened.is_ok(),
        "expected named descriptor to open: {opened:?}"
    );
}

#[cfg(unix)]
#[test]
fn drop_keeps_the_original_descriptor_usable() {
    let mut file =
        File::open(taglib_test_data_file("rare_frames.mp3")).expect("failed to open fixture");
    let mut magic = [0u8; 3];
    file.read_exact(&mut magic).expect("failed to read magic");
    file.seek(SeekFrom::Start(0))
        .expect("failed to rewind original descriptor");
    let opened = TagLibFile::from_file_descriptor(file.as_raw_fd(), true)
        .expect("failed to open fixture from descriptor");
    drop(opened);
    file.seek(SeekFrom::Start(0))
        .expect("original descriptor must still be open after drop");
    let mut reopened = [0u8; 3];
    file.read_exact(&mut reopened)
        .expect("failed to read from original descriptor");
    assert_eq!(reopened, magic);
}

#[test]
fn from_path_rejects_interior_nul_in_path() {
    let opened = TagLibFile::from_path(Path::new("bad\0name.mp3"));
    assert_eq!(opened.unwrap_err(), TagLibError::InvalidFile);
}

#[test]
fn from_path_opens_non_ascii_filename() {
    let dir = TempTestDataDir::new();
    let subdir = dir.path.join("音楽");
    fs::create_dir_all(&subdir).expect("failed to create non-ascii subdirectory");
    let path = subdir.join("tökél-音楽.mp3");
    fs::copy(taglib_test_data_file("lame_cbr.mp3"), &path).expect("failed to copy fixture");
    let opened = TagLibFile::from_path(&path);
    assert!(
        opened.is_ok(),
        "expected fixture {path:?} to open: {opened:?}"
    );
}

#[test]
fn keys_returns_property_keys() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("silence-44-s.flac");
    let path = dir
        .copy_file("silence-44-s.flac", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    let mut keys: Vec<String> = file.keys().collect();
    keys.sort();
    assert_eq!(
        keys,
        [
            "ALBUM".to_owned(),
            "ARTIST".to_owned(),
            "DATE".to_owned(),
            "GENRE".to_owned(),
            "TITLE".to_owned(),
            "TRACKNUMBER".to_owned()
        ]
    );
    assert!(!keys.iter().any(|key| key == "PICTURE"));
}

#[test]
fn keys_is_empty_when_no_properties() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("no-tags.flac");
    let path = dir
        .copy_file("no-tags.flac", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    assert_eq!(file.keys().count(), 0);
}

#[test]
fn keys_survives_file_drop() {
    let mut keys = {
        let dir = TempTestDataDir::new();
        let source = taglib_test_data_file("silence-44-s.flac");
        let path = dir
            .copy_file("silence-44-s.flac", &source)
            .expect("failed to copy fixture");
        let file = TagLibFile::from_path(&path)
            .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
        file.keys()
    };
    let mut collected: Vec<String> = keys.by_ref().collect();
    collected.sort();
    assert_eq!(collected.len(), 6);
    assert!(keys.next().is_none());
}

#[test]
fn get_key_returns_single_value() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("silence-44-s.flac");
    let path = dir
        .copy_file("silence-44-s.flac", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    assert_eq!(file.get_key("TITLE").as_deref(), Some("Silence"));
    assert_eq!(
        file.get_key("ALBUM").as_deref(),
        Some("Quod Libet Test Data")
    );
    assert_eq!(file.get_key("TRACKNUMBER").as_deref(), Some("02/10"));
}

#[test]
fn get_key_returns_first_value_for_multi_valued_key() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("silence-44-s.flac");
    let path = dir
        .copy_file("silence-44-s.flac", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    assert_eq!(file.get_key("ARTIST").as_deref(), Some("piman"));
}

#[test]
fn get_key_returns_none_for_missing_key() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("silence-44-s.flac");
    let path = dir
        .copy_file("silence-44-s.flac", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    assert_eq!(file.get_key("NO_SUCH_KEY"), None);
}

#[test]
fn get_key_returns_none_for_interior_nul() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("silence-44-s.flac");
    let path = dir
        .copy_file("silence-44-s.flac", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    assert_eq!(file.get_key("A\0B"), None);
}

#[test]
fn set_key_then_save_persists() {
    let dir = TempTestDataDir::new();
    let path = dir
        .copy_file("xing.mp3", &taglib_test_data_file("xing.mp3"))
        .expect("failed to copy fixture");
    {
        let mut file = TagLibFile::from_path(&path).expect("failed to open fixture copy");
        file.set_key("COMPOSER", Some("Composer 1"))
            .expect("failed to set key");
        assert!(file.save(), "expected save to succeed");
    }
    let reopened = TagLibFile::from_path(&path).expect("failed to reopen fixture copy");
    assert_eq!(reopened.get_key("COMPOSER").as_deref(), Some("Composer 1"));
}

#[test]
fn set_key_replaces_existing_value() {
    let dir = TempTestDataDir::new();
    let path = dir
        .copy_file("xing.mp3", &taglib_test_data_file("xing.mp3"))
        .expect("failed to copy fixture");
    {
        let mut file = TagLibFile::from_path(&path).expect("failed to open fixture copy");
        file.set_key("COMPOSER", Some("Composer 1"))
            .expect("failed to set key");
        file.set_key("COMPOSER", Some("Composer 2"))
            .expect("failed to replace key");
        assert!(file.save(), "expected save to succeed");
    }
    let reopened = TagLibFile::from_path(&path).expect("failed to reopen fixture copy");
    assert_eq!(reopened.get_key("COMPOSER").as_deref(), Some("Composer 2"));
}

#[test]
fn set_key_without_save_is_discarded() {
    let dir = TempTestDataDir::new();
    let path = dir
        .copy_file("xing.mp3", &taglib_test_data_file("xing.mp3"))
        .expect("failed to copy fixture");
    {
        let mut file = TagLibFile::from_path(&path).expect("failed to open fixture copy");
        file.set_key("COMPOSER", Some("Not Persisted"))
            .expect("failed to set key");
    }
    let reopened = TagLibFile::from_path(&path).expect("failed to reopen fixture copy");
    assert_ne!(
        reopened.get_key("COMPOSER").as_deref(),
        Some("Not Persisted")
    );
}

#[test]
fn set_key_with_none_erases_value() {
    let dir = TempTestDataDir::new();
    let path = dir
        .copy_file("xing.mp3", &taglib_test_data_file("xing.mp3"))
        .expect("failed to copy fixture");
    {
        let mut file = TagLibFile::from_path(&path).expect("failed to open fixture copy");
        file.set_key("COMPOSER", Some("Composer 1"))
            .expect("failed to set key");
        assert!(file.save(), "expected save to succeed");
    }
    {
        let mut file = TagLibFile::from_path(&path).expect("failed to reopen fixture copy");
        assert_eq!(
            file.get_key("COMPOSER").as_deref(),
            Some("Composer 1"),
            "expected the previous save to persist"
        );
        file.set_key("COMPOSER", None).expect("failed to erase key");
        assert!(file.save(), "expected save to succeed");
    }
    let reopened = TagLibFile::from_path(&path).expect("failed to reopen fixture copy");
    assert_eq!(reopened.get_key("COMPOSER"), None);
}

#[test]
fn set_key_rejects_interior_nul() {
    let dir = TempTestDataDir::new();
    let path = dir
        .copy_file("xing.mp3", &taglib_test_data_file("xing.mp3"))
        .expect("failed to copy fixture");
    let mut file = TagLibFile::from_path(&path).expect("failed to open fixture copy");
    assert!(file.set_key("A\0B", Some("value")).is_err());
    assert!(file.set_key("COMPOSER", Some("bad\0value")).is_err());
}

#[test]
fn save_returns_true_on_unmodified_writable_file() {
    let dir = TempTestDataDir::new();
    let path = dir
        .copy_file("xing.mp3", &taglib_test_data_file("xing.mp3"))
        .expect("failed to copy fixture");
    let mut file = TagLibFile::from_path(&path).expect("failed to open fixture copy");
    assert!(file.save(), "expected save to succeed");
}

#[test]
fn get_picture_returns_none_when_absent() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("lame_cbr.mp3");
    let path = dir
        .copy_file("lame_cbr.mp3", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    assert!(file.get_picture().is_none());
}

#[test]
fn get_picture_reads_flac_picture() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("silence-44-s.flac");
    let path = dir
        .copy_file("silence-44-s.flac", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    let picture = file
        .get_picture()
        .expect("expected picture in flac fixture");
    assert_eq!(picture.mime_type().as_deref(), Some("image/png"));
    assert_eq!(picture.description().as_deref(), Some("A pixel."));
    assert_eq!(picture.picture_type().as_deref(), Some("Front Cover"));
    assert_eq!(picture.size(), 150);
    let data = picture.data().expect("expected picture data");
    assert_eq!(data.len(), 150);
    assert_eq!(&data[..8], b"\x89PNG\r\n\x1a\n");
}

#[test]
fn get_picture_reads_ogg_picture() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("lowercase-fields.ogg");
    let path = dir
        .copy_file("lowercase-fields.ogg", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    let picture = file.get_picture().expect("expected picture in ogg fixture");
    assert_eq!(picture.mime_type().as_deref(), Some("image/jpeg"));
    assert_eq!(picture.description().as_deref(), Some("new image"));
    assert_eq!(picture.picture_type().as_deref(), Some("Back Cover"));
    assert_eq!(picture.size(), 9);
    assert_eq!(
        picture.data().map(|data| data.to_vec()),
        Some(b"JPEG data".to_vec())
    );
}

#[test]
fn get_picture_reads_first_of_multiple_pictures() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("has-tags.m4a");
    let path = dir
        .copy_file("has-tags.m4a", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    let picture = file.get_picture().expect("expected picture in m4a fixture");
    assert_eq!(picture.mime_type().as_deref(), Some("image/png"));
    assert_eq!(picture.size(), 79);
}

#[test]
fn picture_accessors_are_repeatable() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("silence-44-s.flac");
    let path = dir
        .copy_file("silence-44-s.flac", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    let picture = file
        .get_picture()
        .expect("expected picture in flac fixture");
    let mime_first = picture.mime_type();
    let mime_second = picture.mime_type();
    assert_eq!(mime_first, mime_second);
    assert_eq!(mime_first.as_deref(), Some("image/png"));
    let data_first = picture.data();
    let data_second = picture.data();
    assert_eq!(data_first, data_second);
    assert_eq!(data_first.map(|data| data.len()), Some(150));
}

#[test]
fn picture_outlives_file() {
    let picture = {
        let dir = TempTestDataDir::new();
        let source = taglib_test_data_file("silence-44-s.flac");
        let path = dir
            .copy_file("silence-44-s.flac", &source)
            .expect("failed to copy fixture");
        let file = TagLibFile::from_path(&path)
            .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
        file.get_picture()
            .expect("expected picture in flac fixture")
    };
    assert_eq!(picture.mime_type().as_deref(), Some("image/png"));
    assert_eq!(picture.description().as_deref(), Some("A pixel."));
    assert_eq!(picture.picture_type().as_deref(), Some("Front Cover"));
    assert_eq!(picture.size(), 150);
    assert_eq!(
        picture.data().map(|data| data.len()),
        Some(150),
        "picture data must stay valid after the file is dropped"
    );
}

#[test]
fn audio_returns_properties_for_mp3() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("xing.mp3");
    let path = dir
        .copy_file("xing.mp3", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    let audio = file.audio().expect("expected audio properties");
    assert_eq!(audio.bitrate, 32);
    assert_eq!(audio.channels, 2);
    assert_eq!(audio.length, 2);
    assert_eq!(audio.samplerate, 44100);
}

#[test]
fn audio_returns_properties_for_flac() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("sinewave.flac");
    let path = dir
        .copy_file("sinewave.flac", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    let audio = file.audio().expect("expected audio properties");
    assert_eq!(audio.bitrate, 145);
    assert_eq!(audio.channels, 2);
    assert_eq!(audio.length, 3);
    assert_eq!(audio.samplerate, 44100);
}

#[test]
fn audio_returns_mono_properties() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("lame_cbr.mp3");
    let path = dir
        .copy_file("lame_cbr.mp3", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    let audio = file.audio().expect("expected audio properties");
    assert_eq!(audio.bitrate, 64);
    assert_eq!(audio.channels, 1);
    assert_eq!(audio.length, 1887);
    assert_eq!(audio.samplerate, 44100);
}

#[test]
fn audio_outlives_file() {
    let audio = {
        let dir = TempTestDataDir::new();
        let source = taglib_test_data_file("xing.mp3");
        let path = dir
            .copy_file("xing.mp3", &source)
            .expect("failed to copy fixture");
        let file = TagLibFile::from_path(&path)
            .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
        file.audio().expect("expected audio properties")
    };
    assert_eq!(audio.bitrate, 32);
    assert_eq!(audio.channels, 2);
    assert_eq!(audio.length, 2);
    assert_eq!(audio.samplerate, 44100);
}

#[test]
fn iterator_returns_none_after_exhaustion() {
    let dir = TempTestDataDir::new();
    let source = taglib_test_data_file("silence-44-s.flac");
    let path = dir
        .copy_file("silence-44-s.flac", &source)
        .expect("failed to copy fixture");
    let file = TagLibFile::from_path(&path)
        .unwrap_or_else(|error| panic!("failed to open fixture {path:?}: {error}"));
    let mut keys = file.keys();
    while keys.next().is_some() {}
    assert!(keys.next().is_none());
    assert!(keys.next().is_none());
    assert!(keys.next().is_none());
}

#[test]
fn error_display_messages() {
    assert_eq!(TagLibError::InvalidFile.to_string(), "invalid file");
    assert_eq!(
        TagLibError::StreamCreationFailed.to_string(),
        "stream creation failed"
    );
    assert_eq!(
        TagLibError::FileCreationFailed.to_string(),
        "file creation failed"
    );
}
