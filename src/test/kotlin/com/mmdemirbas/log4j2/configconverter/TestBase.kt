package com.mmdemirbas.log4j2.configconverter

import com.mmdemirbas.log4j2.configconverter.serializer.JacksonJsonSerializer
import com.mmdemirbas.log4j2.configconverter.serializer.JacksonYamlSerializer
import com.mmdemirbas.log4j2.configconverter.serializer.PropertiesSerializer
import com.mmdemirbas.log4j2.configconverter.serializer.SnakeYamlSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class TestBase(val strings: Map<Serializer.Format, String>,
                        val config: Config) {

    constructor(format: Serializer.Format,
                string: String,
                config: Config) : this(strings = mapOf(format to string),
                                       config = config)

    @DisplayName("serialize")
    @ParameterizedTest(name = "[{index}] serialize via {0}")
    @MethodSource("serializers")
    fun serialize(serializerName: String, serializer: Serializer) {
        assertEquals(stringOf(serializer),
                     config.toString(serializer)) { serializer.name }
    }

    @DisplayName("deserialize")
    @ParameterizedTest(name = "[{index}] deserialize via {0}")
    @MethodSource("serializers")
    fun deserialize(serializerName: String, serializer: Serializer) {
        assertEquals(config,
                     stringOf(serializer).toConfig(serializer)) { serializer.name }
    }

    private fun stringOf(serializer: Serializer) = strings[serializer.format]!!

    @DisplayName("convert")
    @ParameterizedTest(name = "[{index}] serialize & deserialize back via {0}")
    @MethodSource("serializers")
    fun convert(serializerName: String, serializer: Serializer) {
        assertEquals(config, "config"  {
            "string" { config.toString(serializer) }.toConfig(serializer)
        }) { serializer.name }
    }

    private val Serializer.name get() = javaClass.name!!

    private operator fun <T> String.invoke(fn: () -> T): T {
        println()
        println("===[ ${this} ]=====================================================================================================================")
        println()
        return fn().also {
            println(it)
            println()
        }
    }

    @Suppress("unused")
    private fun serializers() = setOf(SnakeYamlSerializer,
                                      JacksonYamlSerializer,
                                      JacksonJsonSerializer,
            // todo: XmlSerializer,
                                      PropertiesSerializer).filter { it.format in strings.keys }.map {
        Arguments.of(it.javaClass.simpleName, it)!!
    }

    companion object {

        fun readResource(resourceName: String) =
                TestBase::class.java.getResource(resourceName).readText()
    }
}
