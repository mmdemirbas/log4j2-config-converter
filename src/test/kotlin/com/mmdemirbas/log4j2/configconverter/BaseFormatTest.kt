package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

abstract class BaseFormatTest(val inputFormat: Format, val resourceName: String, val expected: Config) {
    private val inputText = BaseFormatTest::class.java.getResource(resourceName).readText()

    @Test
    fun load() {
        val inputConfig = inputFormat.load(inputText.reader())
        assertEquals(expected, inputConfig)
    }

    @Test
    fun save() {
        val inputConfig = inputFormat.load(inputText.reader())
        val outputText = inputConfig.toStringAs(inputFormat)
        assertEquals(inputText, outputText)
    }
}

