package com.mmdemirbas.log4j2.configconverter.resources

import com.mmdemirbas.log4j2.configconverter.Appender
import com.mmdemirbas.log4j2.configconverter.AppenderRef
import com.mmdemirbas.log4j2.configconverter.Config
import com.mmdemirbas.log4j2.configconverter.Filter
import com.mmdemirbas.log4j2.configconverter.Layout
import com.mmdemirbas.log4j2.configconverter.Level
import com.mmdemirbas.log4j2.configconverter.Logger
import com.mmdemirbas.log4j2.configconverter.Loggers
import com.mmdemirbas.log4j2.configconverter.Property
import com.mmdemirbas.log4j2.configconverter.RootLogger
import com.mmdemirbas.log4j2.configconverter.Serializer.Format.JSON
import com.mmdemirbas.log4j2.configconverter.TestBase
import org.junit.jupiter.api.DisplayName


@DisplayName("sample.json")
object SampleJsonTest : TestBase(JSON,
                                 readResource("/com/mmdemirbas/log4j2/configconverter/sample.json"),
                                 Config(status = Level.debug,
                                        name = "RoutingTest",
                                        packages = mutableListOf("org.apache.logging.log4j.test"),
                                        properties = mutableListOf(Property("filename",
                                                                            "target/rolling1/rollingtest-$\${sd:type}.log")),

                                        filter = mutableListOf(Filter(type = "ThresholdFilter",
                                                                      extra = mutableMapOf(
                                                                              "level" to "debug"))),
                                        appenders = mutableListOf(Appender(type = "Console",
                                                                           name = "STDOUT",
                                                                           Layout = Layout(
                                                                                   type = "PatternLayout",
                                                                                   extra = mutableMapOf(
                                                                                           "pattern" to "%m%n")),
                                                                           filters = mutableListOf(
                                                                                   Filter(type = "ThresholdFilter",
                                                                                          extra = mutableMapOf(
                                                                                                  "level" to "debug")))),
                                                                  Appender(type = "Routing",
                                                                           name = "Routing",
                                                                           extra = mutableMapOf(
                                                                                   "Routes" to mapOf(
                                                                                           "pattern" to "$\${sd:type}",
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
                                        loggers = Loggers(Logger = mutableListOf(
                                                Logger(name = "EventLogger",
                                                       level = Level.info,
                                                       additivity = false,
                                                       AppenderRef = mutableListOf(
                                                               AppenderRef(ref = "Routing"))),
                                                Logger(name = "com.foo.bar",
                                                       level = Level.error,
                                                       additivity = false,
                                                       AppenderRef = mutableListOf(
                                                               AppenderRef(ref = "STDOUT")))),
                                                          Root = RootLogger(
                                                                  level = Level.error,
                                                                  appenderRef = mutableListOf(
                                                                          AppenderRef(
                                                                                  ref = "STDOUT"))))))
