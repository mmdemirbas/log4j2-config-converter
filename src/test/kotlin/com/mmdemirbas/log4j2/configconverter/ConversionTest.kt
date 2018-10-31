package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
object ConversionTest {
    @ParameterizedTest(name = "[{index}] to {0}")
    @DisplayName("from prod.properties")
    @MethodSource("formatsList")
    fun `from properties - prod`(format: Format) {
        assertCanConvert(config("/com/mmdemirbas/log4j2/configconverter/prod.properties", Properties), format)
    }

    @ParameterizedTest(name = "[{index}] to {0}")
    @DisplayName("from sample.properties")
    @MethodSource("formatsList")
    fun `from properties - sample`(format: Format) {
        assertCanConvert(config("/com/mmdemirbas/log4j2/configconverter/sample.properties", Properties), format)
    }

    @ParameterizedTest(name = "[{index}] to {0}")
    @DisplayName("from prod.yaml")
    @MethodSource("formatsList")
    fun `from yaml - prod`(format: Format) {
        assertCanConvert(config("/com/mmdemirbas/log4j2/configconverter/prod.yaml", Yaml), format)
    }

    @ParameterizedTest(name = "[{index}] to {0}")
    @DisplayName("from sample.yaml")
    @MethodSource("formatsList")
    fun `from yaml - sample`(format: Format) {
        assertCanConvert(config("/com/mmdemirbas/log4j2/configconverter/sample.yaml", Yaml), format)
    }

    @ParameterizedTest(name = "[{index}] to {0}")
    @DisplayName("from prod.xml")
    @MethodSource("formatsList")
    fun `from xml - prod`(format: Format) {
        assertCanConvert(config("/com/mmdemirbas/log4j2/configconverter/prod.xml", Xml), format)
    }

    @ParameterizedTest(name = "[{index}] to {0}")
    @DisplayName("from sample.xml")
    @MethodSource("formatsList")
    fun `from xml - sample`(format: Format) {
        assertCanConvert(config("/com/mmdemirbas/log4j2/configconverter/sample.xml", Xml), format)
    }

    @ParameterizedTest(name = "[{index}] to {0}")
    @DisplayName("from sample.json")
    @MethodSource("formatsList")
    fun `from json - sample`(format: Format) {
        assertCanConvert(config("/com/mmdemirbas/log4j2/configconverter/sample.json", Json), format)
    }

    @Suppress("unused")
    private fun formatsList() = listOf(Properties, Yaml, SnakeYaml, Json, Xml)

    private fun config(resourceName: String, inputFormat: Format): Config {
        val inputText = ConversionTest::class.java.getResource(resourceName).readText()
        val inputConfig = inputFormat.load(inputText.reader())
        return inputConfig
    }

    private fun assertCanConvert(inputConfig: Config, format: Format) {
        val outputText = inputConfig.toStringAs(format)
        val outputConfig = format.load(outputText.reader())
        assertEquals(inputConfig, outputConfig)
    }
}
