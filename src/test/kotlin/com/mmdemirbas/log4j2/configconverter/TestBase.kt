package com.mmdemirbas.log4j2.configconverter

import com.mmdemirbas.log4j2.configconverter.serializer.JacksonJsonSerializer
import com.mmdemirbas.log4j2.configconverter.serializer.JacksonYamlSerializer
import com.mmdemirbas.log4j2.configconverter.serializer.PropertiesSerializer
import com.mmdemirbas.log4j2.configconverter.serializer.SnakeYamlSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.function.Executable
import org.junit.platform.commons.util.BlacklistedExceptions
import org.opentest4j.MultipleFailuresError

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class TestBase(val config: Config,
                        val strings: Map<Serializer.Format, Set<String>>) {

    constructor(format: Serializer.Format,
                string: Set<String>,
                config: Config) : this(config = config,
                                       strings = mapOf(format to string))

    @DisplayName("serialize")
    @TestFactory
    fun serialize(): List<DynamicTest> {
        return serializers.map { serializer ->
            DynamicTest.dynamicTest("${serializer.simpleName}.serialize()") {
                val serialized = config.toString(serializer)
                val format = serializer.format
                val expecteds = strings[format]!!
                val singleExpected = expecteds.singleOrNull()
                if (singleExpected != null) {
                    assertEquals(singleExpected, serialized) {
                        serializer.fullName
                    }
                } else {
                    assertAny(expecteds.mapIndexed { index, expected ->
                        Executable {
                            assertEquals(expected, serialized) {
                                serializer.fullName + " (${format.name.toLowerCase()}[$index] of ${expecteds.size})"
                            }
                        }
                    })
                }
            }
        }
    }

    @DisplayName("deserialize")
    @TestFactory
    fun deserialize(): List<DynamicNode> {
        return serializers.map { serializer ->
            val format = serializer.format
            val executables = strings[format]!!.map { expected ->
                Executable {
                    val deserialized = expected.toConfig(serializer)
                    assertEquals(config, deserialized) { serializer.fullName }
                }
            }
            val singleExecutable = executables.singleOrNull()
            if (singleExecutable != null) {
                DynamicTest.dynamicTest("${serializer.simpleName}.deserialize()",
                                        singleExecutable)
            } else DynamicContainer.dynamicContainer("${serializer.simpleName}.deserialize()",
                                                     executables.mapIndexed { index, executable ->
                                                         DynamicTest.dynamicTest(
                                                                 "${format.name.toLowerCase()}[$index]",
                                                                 executable)
                                                     })
        }
    }

    @DisplayName("convert")
    @TestFactory
    fun convert(): List<DynamicTest> {
        return serializers.map { serializer ->
            DynamicTest.dynamicTest("${serializer.simpleName}.serialize().deserialize()") {
                val serialized = config.toString(serializer)
                val deserialized = serialized.toConfig(serializer)
                assertEquals(config, deserialized) {
                    """${serializer.fullName} =>

===[ ${serializer.simpleName}.serialize() ]===============================================================

$serialized


===[ ${serializer.simpleName}.deserialize() ]=============================================================

$deserialized

                """
                }
            }
        }
    }

    private val serializers
        get() = setOf(JacksonJsonSerializer,
                      JacksonYamlSerializer,
                      SnakeYamlSerializer,
                // todo: XmlSerializer,
                      PropertiesSerializer).filter { serializer ->
            serializer.format in strings.keys
        }

    companion object {

        fun fromResource(resourceName: String) =
                setOf(TestBase::class.java.getResource(resourceName).readText())

        fun assertAny(executables: Collection<Executable>) {
            val failures = mutableListOf<Throwable>()
            for (executable in executables) {
                try {
                    executable.execute()
                    return
                } catch (t: Throwable) {
                    BlacklistedExceptions.rethrowIfBlacklisted(t)
                    failures.add(t)
                }
            }

            if (!failures.isEmpty()) {
                throw MultipleFailuresError(null, failures)
            }
        }

        val Any.simpleName get() = javaClass.simpleName!!
        val Any.fullName get() = javaClass.name!!
    }
}
