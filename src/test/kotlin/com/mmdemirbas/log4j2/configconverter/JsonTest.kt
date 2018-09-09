package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Muhammed Demirbaş
 * @since 2018-09-10 01:28
 */
class JsonTest {
    @Test
    fun json() {
        assertEquals(Configuration(), Json.read("/com/mmdemirbas/log4j2/configconverter/sample.json"))
    }
}