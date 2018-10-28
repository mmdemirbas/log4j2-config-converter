package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Test

private val sampleResourceName = "/com/mmdemirbas/log4j2/configconverter/sample.properties"
private val sampleConfig =
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
                                                                                           ref = "STDOUT")))))

object PropertiesTest {
    @Test
    fun read() = Properties.assertReadResult(sampleResourceName, sampleConfig)


    @Test
    fun write() = Properties.assertWriteResult(sampleResourceName)
}

