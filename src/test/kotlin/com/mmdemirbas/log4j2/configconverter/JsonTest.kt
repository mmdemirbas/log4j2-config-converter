package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Test

private val sampleResourceName = "/com/mmdemirbas/log4j2/configconverter/sample.json"
private val sampleConfig =
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
                                                                                         "Route" to mutableListOf(mapOf(
                                                                                                 "RollingFile" to mapOf(
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
                                                   appenderRef = mutableListOf(AppenderRef(ref = "STDOUT")))))

object JsonTest {
    @Test
    fun read() = Json.assertReadResult(sampleResourceName, sampleConfig)


    @Test
    fun write() = Json.assertWriteResult(sampleResourceName)
}

