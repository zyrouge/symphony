package io.github.zyrouge.symphony.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ImageQualityTest {
    @Test
    fun `loseless quality never rescales`() {
        assertNull(calculateResizedDimensions(4000, 3000, ImageQuality.Loseless.maxSide))
    }

    @Test
    fun `an image already within the max side is kept as is`() {
        assertNull(calculateResizedDimensions(200, 150, ImageQuality.Low.maxSide))
    }

    @Test
    fun `a landscape image scales to the max width and keeps the ratio`() {
        assertEquals(1024 to 768, calculateResizedDimensions(2048, 1536, ImageQuality.High.maxSide))
    }

    @Test
    fun `a portrait image scales to the max height and keeps the ratio`() {
        assertEquals(768 to 1024, calculateResizedDimensions(1536, 2048, ImageQuality.High.maxSide))
    }

    @Test
    fun `a square image scales both sides to the max side`() {
        assertEquals(512 to 512, calculateResizedDimensions(2048, 2048, ImageQuality.Medium.maxSide))
    }

    @Test
    fun `an image exactly at the max side is scaled to the same box`() {
        assertEquals(1024 to 1024, calculateResizedDimensions(1024, 1024, ImageQuality.High.maxSide))
    }
}
