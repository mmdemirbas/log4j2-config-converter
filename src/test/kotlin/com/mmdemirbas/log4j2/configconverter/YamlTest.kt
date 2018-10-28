package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Test

private val sampleResourceName = "/com/mmdemirbas/log4j2/configconverter/sample.yaml"

private val sampleConfig =
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
                                                   appenderRef = mutableListOf(AppenderRef(ref = "STDOUT")))))

object YamlTest {
    @Test
    fun read() = Yaml.assertReadResult(sampleResourceName, sampleConfig)

    @Test
    fun write() = Yaml.assertReadWriteGivesSameResult(sampleResourceName)
}


object SnakeYamlTest {
    @Test
    fun read() = SnakeYaml.assertReadResult(sampleResourceName, sampleConfig)

    @Test
    fun write() = SnakeYaml.assertReadWriteGivesSameResult(sampleResourceName)
}

