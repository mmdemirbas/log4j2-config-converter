package com.mmdemirbas.log4j2.configconverter

object XmlTest : BaseFormatTest(Xml,
                                "/com/mmdemirbas/log4j2/configconverter/sample.xml",
                                Config(status = Level.debug,
                                       strict = true,
                                       name = "XMLConfigTest",
                                       packages = mutableListOf("org.apache.logging.log4j.test"),
                                       properties = mutableListOf(Property("filename",
                                                                           "target/test.log")),
                                       filter = mutableListOf(Filter(type = "ThresholdFilter",
                                                                     extra = mutableMapOf(
                                                                             "level" to "trace"))),
                                       appenders = mutableListOf(Appender(type = "Console",
                                                                          name = "STDOUT",
                                                                          Layout = Layout(
                                                                                  type = "PatternLayout",
                                                                                  extra = mutableMapOf(
                                                                                          "pattern" to "%m MDC%X%n")),
                                                                          filters = mutableListOf(
                                                                                  Filter(type = "MarkerFilter",
                                                                                         onMatch = FilterDecision.DENY,
                                                                                         onMismatch = FilterDecision.NEUTRAL,
                                                                                         extra = mutableMapOf(
                                                                                                 "marker" to "FLOW")),
                                                                                  Filter(type = "MarkerFilter",
                                                                                         onMatch = FilterDecision.DENY,
                                                                                         onMismatch = FilterDecision.ACCEPT,
                                                                                         extra = mutableMapOf(
                                                                                                 "marker" to "EXCEPTION")))),
                                                                 Appender(type = "Console",
                                                                          name = "FLOW",
                                                                          Layout = Layout(
                                                                                  type = "PatternLayout",
                                                                                  extra = mutableMapOf(
                                                                                          "pattern" to "%C{1}.%M %m %ex%n")),
                                                                          filters = mutableListOf(
                                                                                  Filter(type = "MarkerFilter",
                                                                                         onMatch = FilterDecision.ACCEPT,
                                                                                         onMismatch = FilterDecision.NEUTRAL,
                                                                                         extra = mutableMapOf(
                                                                                                 "marker" to "FLOW")),
                                                                                  Filter(type = "MarkerFilter",
                                                                                         onMatch = FilterDecision.ACCEPT,
                                                                                         onMismatch = FilterDecision.DENY,
                                                                                         extra = mutableMapOf(
                                                                                                 "marker" to "EXCEPTION")))),
                                                                 Appender(type = "File",
                                                                          name = "File",
                                                                          Layout = Layout(
                                                                                  type = "PatternLayout",
                                                                                  extra = mutableMapOf(
                                                                                          "Pattern" to "%d %p %C{1.} [%t] %m%n")),
                                                                          extra = mutableMapOf(
                                                                                  "fileName" to "\${filename}"))),
                                       loggers = Loggers(Logger = mutableListOf(
                                               Logger(name = "org.apache.logging.log4j.test1",
                                                      level = Level.debug,
                                                      additivity = false,
                                                      filter = mutableListOf(
                                                              Filter(type = "ThreadContextMapFilter",
                                                                     extra = mutableMapOf(
                                                                             "KeyValuePair" to mapOf(
                                                                                     "key" to "test",
                                                                                     "value" to "123")))),
                                                      AppenderRef = mutableListOf(
                                                              AppenderRef(ref = "STDOUT"))),
                                               Logger(name = "org.apache.logging.log4j.test2",
                                                      level = Level.debug,
                                                      additivity = false,
                                                      AppenderRef = mutableListOf(
                                                              AppenderRef(ref = "File")))),
                                                         Root = RootLogger(level = Level.trace,
                                                                           appenderRef = mutableListOf(
                                                                                   AppenderRef(
                                                                                           ref = "STDOUT"))))))
