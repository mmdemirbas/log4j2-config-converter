package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Muhammed Demirbaş
 * @since 2018-09-10 01:28
 */
class YamlTest {
    @Test
    fun yaml() {
        assertEquals(Configuration(), Yaml.read("/com/mmdemirbas/log4j2/configconverter/sample.yaml"))
    }
}