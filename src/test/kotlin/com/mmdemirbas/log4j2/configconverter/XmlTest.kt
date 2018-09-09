package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Muhammed Demirbaş
 * @since 2018-09-10 01:28
 */
class XmlTest {
    @Test
    fun xml() {
        assertEquals(Configuration(status = Level.DEBUG,
                                   strict = true,
                                   name = "XMLConfigTest",
                                   packages = listOf("org.apache.logging.log4j.test"),
                                   properties = listOf(Property("filename", "target/test.log")),
                                   filter = listOf(Filter(type = "ThresholdFilter",
                                                          extra = mutableMapOf("level" to "trace"))),
                                   appenders = listOf(Appender(type = "Console",
                                                               name = "STDOUT",
                                                               layout = Layout(type = "PatternLayout",
                                                                               extra = mutableMapOf("pattern" to "%m MDC%X%n")),
                                                               filters = listOf(Filter(type = "MarkerFilter",
                                                                                       onMatch = FilterDecision.DENY,
                                                                                       onMismatch = FilterDecision.NEUTRAL,
                                                                                       extra = mutableMapOf("marker" to "FLOW")),
                                                                                Filter(type = "MarkerFilter",
                                                                                       onMatch = FilterDecision.DENY,
                                                                                       onMismatch = FilterDecision.ACCEPT,
                                                                                       extra = mutableMapOf("marker" to "EXCEPTION")))),
                                                      Appender(type = "Console",
                                                               name = "FLOW",
                                                               layout = Layout(type = "PatternLayout",
                                                                               extra = mutableMapOf("pattern" to "%C{1}.%M %m %ex%n")),
                                                               filters = listOf(Filter(type = "MarkerFilter",
                                                                                       onMatch = FilterDecision.ACCEPT,
                                                                                       onMismatch = FilterDecision.NEUTRAL,
                                                                                       extra = mutableMapOf("marker" to "FLOW")),
                                                                                Filter(type = "MarkerFilter",
                                                                                       onMatch = FilterDecision.ACCEPT,
                                                                                       onMismatch = FilterDecision.DENY,
                                                                                       extra = mutableMapOf("marker" to "EXCEPTION")))),
                                                      Appender(type = "File",
                                                               name = "File",
                                                               layout = Layout(type = "PatternLayout",
                                                                               extra = mutableMapOf("Pattern" to "%d %p %C{1.} [%t] %m%n")),
                                                               extra = mutableMapOf("fileName" to "\${filename}"))),
                                   loggers = Loggers(logger = listOf(Logger(name = "org.apache.logging.log4j.test1",
                                                                            level = Level.DEBUG,
                                                                            additivity = false,
                                                                            filter = listOf(Filter(type = "ThreadContextMapFilter",
                                                                                                   extra = mutableMapOf(
                                                                                                           "test" to "123"))),
                                                                            appenderRef = listOf(AppenderRef(ref = "STDOUT"))),
                                                                     Logger(name = "org.apache.logging.log4j.test2",
                                                                            level = Level.DEBUG,
                                                                            additivity = false,
                                                                            appenderRef = listOf(AppenderRef(ref = "File")))),
                                                     root = RootLogger(level = Level.TRACE,
                                                                       appenderRef = listOf(AppenderRef(ref = "STDOUT"))))),
                     Xml.read("/com/mmdemirbas/log4j2/configconverter/sample.xml"))
    }
}