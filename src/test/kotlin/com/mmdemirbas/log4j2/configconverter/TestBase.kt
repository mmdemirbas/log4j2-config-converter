package com.mmdemirbas.log4j2.configconverter

import com.mmdemirbas.log4j2.configconverter.serializer.JacksonJsonSerializer
import com.mmdemirbas.log4j2.configconverter.serializer.JacksonYamlSerializer
import com.mmdemirbas.log4j2.configconverter.serializer.PropertiesSerializer
import com.mmdemirbas.log4j2.configconverter.serializer.SnakeYamlSerializer
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class TestBase(val config: Config,
                        val strings: Map<Serializer.Format, Set<String>>) {

    constructor(format: Serializer.Format,
                string: Set<String>,
                config: Config) : this(config = config,
                                       strings = mapOf(format to string))

    // keep public to make getSerializer method available
    @Suppress("unused") val serializers =
            setOf(JacksonJsonSerializer,
                  JacksonYamlSerializer,
                  SnakeYamlSerializer,
                    // todo: XmlSerializer,
                  PropertiesSerializer).filter { serializer ->
                serializer.format in strings.keys
            }.map { serializer ->
                Arguments.of(serializer.simpleName, serializer)!!
            }

    @DisplayName("serialize")
    @ParameterizedTest(name = "[{index}] {0}.serialize()")
    @MethodSource("getSerializers")
    fun serialize(serializerName: String, serializer: Serializer) {
        val serialized = config.toString(serializer)
        val expecteds = strings[serializer.format]!!
        if (serialized !in expecteds) {
            assertEquals(expecteds.firstOrNull(), serialized) {
                when {
                    expecteds.size == 1 -> serializer.fullName
                    else                -> serializer.fullName + " (none of the ${expecteds.size} alternative(s) matched)"
                }
            }
        }
    }

    @DisplayName("deserialize")
    @ParameterizedTest(name = "[{index}] {0}.deserialize()")
    @MethodSource("getSerializers")
    fun deserialize(serializerName: String, serializer: Serializer) {
        val expecteds = strings[serializer.format]!!
        val singleExpected = expecteds.singleOrNull()
        if (singleExpected != null) {
            assertEquals(config, singleExpected.toConfig(serializer)) {
                serializer.fullName
            }
        } else {
            assertAll(expecteds.mapIndexed { index, expected ->
                Executable {
                    assertEquals(config, expected.toConfig(serializer)) {
                        serializer.fullName + " (alternative at [$index] among ${expecteds.size} alternative(s))"
                    }
                }
            })
        }
    }

    @DisplayName("convert")
    @ParameterizedTest(name = "[{index}] {0}.serialize().deserialize()")
    @MethodSource("getSerializers")
    fun convert(serializerName: String, serializer: Serializer) {
        val serialized = config.toString(serializer)
        val deserialized = serialized.toConfig(serializer)
        assertEquals(config, deserialized) {
            """${serializer.fullName} =>

===[ $serializerName.serialize() ]===============================================================

$serialized


===[ $serializerName.deserialize() ]=============================================================

$deserialized

                """
        }
    }

    private val Any.simpleName get() = javaClass.simpleName
    private val Any.fullName get() = javaClass.name!!

    companion object {

        fun fromResource(resourceName: String) =
                setOf(TestBase::class.java.getResource(resourceName).readText())
    }
}
