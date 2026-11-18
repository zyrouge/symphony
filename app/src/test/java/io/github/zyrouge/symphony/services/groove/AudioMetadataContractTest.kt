package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.utils.SimplePath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class AudioMetadataContractTest {
    private val metaphonyPolicy = ArtworkPolicy(
        nameWithMimeExtension = true,
        dropWhenMimeUnknown = true,
        copyBytesWhenNoResize = true,
    )

    private val retrieverPolicy = ArtworkPolicy(
        nameWithMimeExtension = false,
        dropWhenMimeUnknown = false,
        copyBytesWhenNoResize = false,
    )

    @Test
    fun `secondsToMillisOrZero converts whole seconds`() {
        assertEquals(0L, secondsToMillisOrZero(0L))
        assertEquals(1000L, secondsToMillisOrZero(1L))
        assertEquals(183_000L, secondsToMillisOrZero(183L))
    }

    @Test
    fun `secondsToMillisOrZero folds a missing value to zero`() {
        assertEquals(0L, secondsToMillisOrZero(null))
    }

    @Test
    fun `kbpsToBps scales kilobits into bits`() {
        assertEquals(320_000L, kbpsToBps(320L))
        assertEquals(0L, kbpsToBps(0L))
    }

    @Test
    fun `kbpsToBps keeps a missing bitrate missing`() {
        assertNull(kbpsToBps(null))
    }

    @ParameterizedTest
    @CsvSource("image/jpg,jpg", "image/jpeg,jpg", "image/png,png")
    fun `coverExtensionFor maps jpeg and png mime types`(mimeType: String, expected: String) {
        assertEquals(expected, coverExtensionFor(mimeType))
    }

    @ParameterizedTest
    @ValueSource(strings = ["image/webp", "image/gif", "", "IMAGE/PNG"])
    fun `coverExtensionFor rejects every other mime type`(mimeType: String) {
        assertNull(coverExtensionFor(mimeType))
    }

    @Test
    fun `coverExtensionFor rejects a missing mime type`() {
        assertNull(coverExtensionFor(null))
    }

    @Test
    fun `titleOrFileName prefers the tag title`() {
        assertEquals("Real Title", titleOrFileName("Real Title", SimplePath("folder/Track.flac")))
    }

    @Test
    fun `titleOrFileName falls back to the file name without extension`() {
        assertEquals("Set.01", titleOrFileName(null, SimplePath("folder/Set.01.flac")))
    }

    @Test
    fun `shouldStoreLyrics requires both a storing reader and present lyrics`() {
        assertTrue(shouldStoreLyrics(storesLyrics = true, lyrics = "la la"))
        assertFalse(shouldStoreLyrics(storesLyrics = false, lyrics = "la la"))
        assertFalse(shouldStoreLyrics(storesLyrics = true, lyrics = null))
        assertFalse(shouldStoreLyrics(storesLyrics = false, lyrics = null))
    }

    @Test
    fun `coverPlanFor returns no cover when the reader produced no picture`() {
        val plan = coverPlanFor(metaphonyPolicy, null, null, "id")

        assertEquals(CoverPlan(null, CoverMode.NONE), plan)
    }

    @Test
    fun `coverPlanFor copies png bytes verbatim when no resize is requested`() {
        val picture = EmbeddedPicture(byteArrayOf(1, 2, 3), "image/png")

        val plan = coverPlanFor(metaphonyPolicy, picture, null, "id")

        assertEquals(CoverPlan("id.png", CoverMode.RAW_COPY), plan)
    }

    @Test
    fun `coverPlanFor resizes jpeg artwork when a max side is configured`() {
        val picture = EmbeddedPicture(byteArrayOf(1, 2, 3), "image/jpeg")

        val plan = coverPlanFor(metaphonyPolicy, picture, 512, "id")

        assertEquals(CoverPlan("id.jpg", CoverMode.JPEG_RESIZE), plan)
    }

    @Test
    fun `coverPlanFor drops artwork with an unknown mime type for the preserving reader`() {
        val picture = EmbeddedPicture(byteArrayOf(1, 2, 3), "image/webp")

        val plan = coverPlanFor(metaphonyPolicy, picture, 512, "id")

        assertEquals(CoverPlan(null, CoverMode.NONE), plan)
    }

    @Test
    fun `coverPlanFor always names retriever artwork jpg regardless of mime type`() {
        val picture = EmbeddedPicture(byteArrayOf(1, 2, 3), "image/png")

        val plan = coverPlanFor(retrieverPolicy, picture, 512, "id.mr")

        assertEquals(CoverPlan("id.mr.jpg", CoverMode.JPEG_RESIZE), plan)
    }

    @Test
    fun `coverPlanFor resizes png artwork into a jpg when a max side is configured`() {
        val picture = EmbeddedPicture(byteArrayOf(1, 2, 3), "image/png")

        val plan = coverPlanFor(metaphonyPolicy, picture, 512, "id")

        assertEquals(CoverPlan("id.jpg", CoverMode.JPEG_RESIZE), plan)
    }

    @Test
    fun `coverPlanFor never copies retriever artwork verbatim`() {
        val picture = EmbeddedPicture(byteArrayOf(1, 2, 3), "image/png")

        val plan = coverPlanFor(retrieverPolicy, picture, null, "id.mr")

        assertEquals(CoverPlan("id.mr.jpg", CoverMode.JPEG_RESIZE), plan)
    }
}
