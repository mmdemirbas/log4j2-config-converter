package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.StringReader

/**
 * @author Muhammed Demirbaş
 * @since 2018-09-10 01:28
 */

abstract class BaseTest(val format: Format, val resourceNameToExpectedConfig: Pair<String, Config>) {
    @Test
    fun read() =
            assertEquals(resourceNameToExpectedConfig.second, format.readResource(resourceNameToExpectedConfig.first))

    @Test
    fun write() = format.assertReadIsReversible(resourceNameToExpectedConfig.first)
}

object JsonTest : BaseTest(Json, sampleJson)
object PropertiesTest : BaseTest(Properties, sampleProperties)
object XmlTest : BaseTest(Xml, sampleXml)
object YamlTest : BaseTest(Yaml, sampleYaml)
object SnakeYamlTest : BaseTest(SnakeYaml, sampleYaml)

class ConversionTests {
    @Test
    fun `properties is reversible`() {
        Properties.assertReadIsReversible("/com/mmdemirbas/log4j2/configconverter/prod.properties")
    }

    @Test
    fun `properties to yaml is reversible`() {
        val propConfig =
                log("propConfig") { Properties.readResource("/com/mmdemirbas/log4j2/configconverter/prod.properties") }
        val yamlString = log("yamlString") { propConfig.toString(SnakeYaml) }
        val yamlConfig = log("yamlConfig") { SnakeYaml.read(StringReader(yamlString)) }
        assertEquals(propConfig, yamlConfig)

        val propString = log("propString") { yamlConfig.toString(Properties) }
        val propConfigBack = log("propConfigBack") { Properties.read(StringReader(propString)) }
        assertEquals(propConfig, propConfigBack)
    }
}

private fun Format.assertReadIsReversible(resourceName: String) =
        assertEquals(Format::class.java.getResource(resourceName).readText(), readResource(resourceName).toString(this))

private fun Format.readResource(resourceName: String) =
        read(Format::class.java.getResourceAsStream(resourceName).bufferedReader())

private fun <T> log(title: String, fn: () -> T): T {
    println("===[ $title ]=====================================================================================================================")
    return fn().also {
        println(it)
        println()
    }
}

val sampleJson =
        Pair("/com/mmdemirbas/log4j2/configconverter/sample.json",
             Config(status = Level.debug,
                    name = "RoutingTest",
                    packages = mutableListOf("org.apache.logging.log4j.test"),
                    properties = mutableListOf(Property("filename", "target/rolling1/rollingtest-$\${sd:type}.log")),

                    filter = mutableListOf(Filter(type = "ThresholdFilter", extra = mutableMapOf("level" to "debug"))),
                    appenders = mutableListOf(Appender(type = "Console",
                                                       name = "STDOUT",
                                                       Layout = Layout(type = "PatternLayout",
                                                                       extra = mutableMapOf("pattern" to "%m%n")),
                                                       filters = mutableListOf(Filter(type = "ThresholdFilter",
                                                                                      extra = mutableMapOf("level" to "debug")))),
                                              Appender(type = "Routing",
                                                       name = "Routing",
                                                       extra = mutableMapOf("Routes" to mapOf("pattern" to "$\${sd:type}",
                                                                                              "Route" to mutableListOf(
                                                                                                      mapOf("RollingFile" to mapOf(
                                                                                                              "name" to "Rolling-\${sd:type}",
                                                                                                              "fileName" to "\${filename}",
                                                                                                              "filePattern" to "target/rolling1/test1-\${sd:type}.%i.log.gz",
                                                                                                              "PatternLayout" to mapOf(
                                                                                                                      "pattern" to "%d %p %c{1.} [%t] %m%n"),
                                                                                                              "SizeBasedTriggeringPolicy" to mapOf(
                                                                                                                      "size" to "500"))),
                                                                                                      mapOf("AppenderRef" to "STDOUT",
                                                                                                            "key" to "Audit")))))),
                    loggers = Loggers(Logger = mutableListOf(Logger(name = "EventLogger",
                                                                    level = Level.info,
                                                                    additivity = false,
                                                                    AppenderRef = mutableListOf(AppenderRef(ref = "Routing"))),
                                                             Logger(name = "com.foo.bar",
                                                                    level = Level.error,
                                                                    additivity = false,
                                                                    AppenderRef = mutableListOf(AppenderRef(ref = "STDOUT")))),
                                      Root = RootLogger(level = Level.error,
                                                        appenderRef = mutableListOf(AppenderRef(ref = "STDOUT"))))))


val sampleProperties =
        Pair("/com/mmdemirbas/log4j2/configconverter/sample.properties",
             Config(status = Level.error,
                    name = "PropertiesConfig",
                    dest = "err",
                    properties = mutableListOf(Property("filename", "target/rolling/rollingtest.log")),
                    filter = mutableListOf(Filter(alias = "threshold",
                                                  type = "ThresholdFilter",
                                                  extra = mutableMapOf("level" to "debug"))),
                    appenders = mutableListOf(Appender(alias = "console",
                                                       type = "Console",
                                                       name = "STDOUT",
                                                       Layout = Layout(type = "PatternLayout",
                                                                       extra = mutableMapOf("pattern" to "%m%n")),
                                                       filters = mutableListOf(Filter(alias = "threshold",
                                                                                      type = "ThresholdFilter",
                                                                                      extra = mutableMapOf("level" to "error")))),
                                              Appender(alias = "rolling",
                                                       type = "RollingFile",
                                                       name = "RollingFile",
                                                       Layout = Layout(type = "PatternLayout",
                                                                       extra = mutableMapOf("pattern" to "%d %p %C{1.} [%t] %m%n")),
                                                       extra = mutableMapOf("fileName" to "\${filename}",
                                                                            "filePattern" to "target/rolling2/test1-%d{MM-dd-yy-HH-mm-ss}-%i.log.gz",
                                                                            "policies" to mutableMapOf("type" to "Policies",
                                                                                                       "time" to mutableMapOf(
                                                                                                               "type" to "TimeBasedTriggeringPolicy",
                                                                                                               "interval" to "2",
                                                                                                               "modulate" to "true"),
                                                                                                       "size" to mutableMapOf(
                                                                                                               "type" to "SizeBasedTriggeringPolicy",
                                                                                                               "size" to "100MB")),
                                                                            "strategy" to mutableMapOf("type" to "DefaultRolloverStrategy",
                                                                                                       "max" to "5")))),
                    loggers = Loggers(Logger = mutableListOf(Logger(alias = "rolling",
                                                                    name = "com.example.my.app",
                                                                    level = Level.debug,
                                                                    additivity = false,
                                                                    AppenderRef = mutableListOf(AppenderRef(alias = "rolling",
                                                                                                            ref = "RollingFile")))),
                                      Root = RootLogger(level = Level.info,
                                                        appenderRef = mutableListOf(AppenderRef(alias = "stdout",
                                                                                                ref = "STDOUT"))))))

val sampleXml =
        Pair("/com/mmdemirbas/log4j2/configconverter/sample.xml",
             Config(status = Level.debug,
                    strict = true,
                    name = "XMLConfigTest",
                    packages = mutableListOf("org.apache.logging.log4j.test"),
                    properties = mutableListOf(Property("filename", "target/test.log")),
                    filter = mutableListOf(Filter(type = "ThresholdFilter", extra = mutableMapOf("level" to "trace"))),
                    appenders = mutableListOf(Appender(type = "Console",
                                                       name = "STDOUT",
                                                       Layout = Layout(type = "PatternLayout",
                                                                       extra = mutableMapOf("pattern" to "%m MDC%X%n")),
                                                       filters = mutableListOf(Filter(type = "MarkerFilter",
                                                                                      onMatch = FilterDecision.DENY,
                                                                                      onMismatch = FilterDecision.NEUTRAL,
                                                                                      extra = mutableMapOf("marker" to "FLOW")),
                                                                               Filter(type = "MarkerFilter",
                                                                                      onMatch = FilterDecision.DENY,
                                                                                      onMismatch = FilterDecision.ACCEPT,
                                                                                      extra = mutableMapOf("marker" to "EXCEPTION")))),
                                              Appender(type = "Console",
                                                       name = "FLOW",
                                                       Layout = Layout(type = "PatternLayout",
                                                                       extra = mutableMapOf("pattern" to "%C{1}.%M %m %ex%n")),
                                                       filters = mutableListOf(Filter(type = "MarkerFilter",
                                                                                      onMatch = FilterDecision.ACCEPT,
                                                                                      onMismatch = FilterDecision.NEUTRAL,
                                                                                      extra = mutableMapOf("marker" to "FLOW")),
                                                                               Filter(type = "MarkerFilter",
                                                                                      onMatch = FilterDecision.ACCEPT,
                                                                                      onMismatch = FilterDecision.DENY,
                                                                                      extra = mutableMapOf("marker" to "EXCEPTION")))),
                                              Appender(type = "File",
                                                       name = "File",
                                                       Layout = Layout(type = "PatternLayout",
                                                                       extra = mutableMapOf("Pattern" to "%d %p %C{1.} [%t] %m%n")),
                                                       extra = mutableMapOf("fileName" to "\${filename}"))),
                    loggers = Loggers(Logger = mutableListOf(Logger(name = "org.apache.logging.log4j.test1",
                                                                    level = Level.debug,
                                                                    additivity = false,
                                                                    filter = mutableListOf(Filter(type = "ThreadContextMapFilter",
                                                                                                  extra = mutableMapOf("KeyValuePair" to mapOf(
                                                                                                          "key" to "test",
                                                                                                          "value" to "123")))),
                                                                    AppenderRef = mutableListOf(AppenderRef(ref = "STDOUT"))),
                                                             Logger(name = "org.apache.logging.log4j.test2",
                                                                    level = Level.debug,
                                                                    additivity = false,
                                                                    AppenderRef = mutableListOf(AppenderRef(ref = "File")))),
                                      Root = RootLogger(level = Level.trace,
                                                        appenderRef = mutableListOf(AppenderRef(ref = "STDOUT"))))))

val sampleYaml =
        Pair("/com/mmdemirbas/log4j2/configconverter/sample.yaml",
             Config(status = Level.warn,
                    name = "YAMLConfigTest",
                    properties = mutableListOf(Property(name = "filename", value = "target/test-yaml.log")),
                    filter = mutableListOf(Filter(type = "thresholdFilter", extra = mutableMapOf("level" to "debug"))),
                    appenders = mutableListOf(Appender(type = "Console",
                                                       name = "STDOUT",
                                                       Layout = Layout(type = "PatternLayout",
                                                                       extra = mutableMapOf("Pattern" to "%m%n"))),
                                              Appender(type = "File",
                                                       name = "File",
                                                       Layout = Layout(type = "PatternLayout",
                                                                       extra = mutableMapOf("Pattern" to "%d %p %C{1.} [%t] %m%n")),
                                                       filters = mutableListOf(Filter(type = "ThresholdFilter",
                                                                                      extra = mutableMapOf("level" to "error"))),
                                                       extra = mutableMapOf("fileName" to "\${filename}"))),
                    loggers = Loggers(Logger = mutableListOf(Logger(name = "org.apache.logging.log4j.test1",
                                                                    level = Level.debug,
                                                                    additivity = false,
                                                                    filter = mutableListOf(Filter(type = "ThreadContextMapFilter",
                                                                                                  extra = mutableMapOf("KeyValuePair" to mapOf(
                                                                                                          "key" to "test",
                                                                                                          "value" to 123)))),
                                                                    AppenderRef = mutableListOf(AppenderRef(ref = "STDOUT"))),
                                                             Logger(name = "org.apache.logging.log4j.test2",
                                                                    level = Level.debug,
                                                                    additivity = false,
                                                                    AppenderRef = mutableListOf(AppenderRef(ref = "File")))),
                                      Root = RootLogger(level = Level.error,
                                                        appenderRef = mutableListOf(AppenderRef(ref = "STDOUT"))))))

