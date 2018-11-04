package com.mmdemirbas.log4j2.configconverter

import com.mmdemirbas.log4j2.configconverter.Serializer.Format.JSON
import com.mmdemirbas.log4j2.configconverter.Serializer.Format.PROPERTIES
import com.mmdemirbas.log4j2.configconverter.Serializer.Format.XML
import com.mmdemirbas.log4j2.configconverter.Serializer.Format.YAML
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested

@DisplayName("configuration")
class ConfigTest {
    @Nested
    inner class advertiser : TestBase(config = Config(advertiser = "123"),
                                      strings = mapForProperty("advertiser",
                                                               "123"))

    @Nested
    inner class dest : TestBase(config = Config(dest = "123"),
                                strings = mapForProperty("dest", "123"))

    @Nested
    inner class monitorIntervalSeconds : TestBase(config = Config(
            monitorIntervalSeconds = 123),
                                                  strings = mapForProperty("monitorInterval",
                                                                           123))

    @Nested
    inner class name : TestBase(config = Config(name = "123"),
                                strings = mapForProperty("name", "123"))

    @Nested
    inner class packages : TestBase(config = Config(packages = "1,2,3"),
                                    strings = mapForProperty("packages",
                                                             "1,2,3"))

    @Nested
    inner class schemaResource : TestBase(config = Config(schemaResource = "123"),
                                          strings = mapForProperty("schema",
                                                                   "123"))

    @Nested
    inner class isShutdownHookEnabled : TestBase(config = Config(
            isShutdownHookEnabled = true),
                                                 strings = mapForProperty("shutdownHook",
                                                                          true))

    @Nested
    inner class status : TestBase(config = Config(status = Level.debug),
                                  strings = mapForProperty("status", "debug"))

    @Nested
    inner class strict : TestBase(config = Config(strict = true),
                                  strings = mapForProperty("strict", true))

    @Nested
    inner class shutdownTimeoutMillis : TestBase(config = Config(
            shutdownTimeoutMillis = 123),
                                                 strings = mapForProperty("shutdownTimeout",
                                                                          123))

    @Nested
    inner class verbose : TestBase(config = Config(verbose = "full"),
                                   strings = mapForProperty("verbose", "full"))

    private fun mapForProperty(name: String, value: Any) =
            mapOf(PROPERTIES to setOf("$name=$value\n"),
                  YAML to setOf("Configuration:\n  $name: $value\n",
                                "Configuration:\n  $name: '$value'\n"),
                  XML to setOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Configuration $name=\"$value\"/>\n"),
                  JSON to setOf("{\n  \"Configuration\" : {\n    \"$name\" : ${if (value is String) "\"$value\"" else value}\n  }\n}"))


    @Nested
    inner class properties : TestBase(config = Config(properties = mutableListOf(Property(name = "secretOfUniverse", value = "42"))),
                                      strings = mapOf(PROPERTIES to setOf("property.secretOfUniverse=42\n"),
                                                      YAML to setOf("""
                                                        Configuration:
                                                          properties:
                                                            property:
                                                              name: secretOfUniverse
                                                              value: 42

                                                        """.trimIndent(), """
                                                        Configuration:
                                                          properties:
                                                            property:
                                                              name: secretOfUniverse
                                                              value: '42'

                                                        """.trimIndent()),
                                                      XML to setOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Configuration verbose=\"full\"/>\n"),
                                                      JSON to setOf("""
                                                        {
                                                          "Configuration" : {
                                                            "properties" : {
                                                              "property" : {
                                                                "name" : "secretOfUniverse",
                                                                "value" : "42"
                                                              }
                                                            }
                                                          }
                                                        }""".trimIndent())))

    //    @Nested
    //    inner class scripts : TestBase(strings = mapForProperty("verbose", "full"),
    //                                   config = Config(scripts = "full"))
    //
    //    @Nested
    //    inner class customLevels : TestBase(strings = mapForProperty("verbose",
    //                                                                 "full"),
    //                                        config = Config(customLevels = "full"))
    //
    //    @Nested
    //    inner class filter : TestBase(strings = mapForProperty("verbose", "full"),
    //                                  config = Config(filter = "full"))
    //
    //    @Nested
    //    inner class appenders : TestBase(strings = mapForProperty("verbose",
    //                                                              "full"),
    //                                     config = Config(appenders = "full"))
    //
    //    @Nested
    //    inner class loggers : TestBase(strings = mapForProperty("verbose", "full"),
    //                                   config = Config(loggers = "full"))
    //
    //    @Nested
    //    inner class extra : TestBase(strings = mapForProperty("verbose", "full"),
    //                                 config = Config(extra = "full"))


}