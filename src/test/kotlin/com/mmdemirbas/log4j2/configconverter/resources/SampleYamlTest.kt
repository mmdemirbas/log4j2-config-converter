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
import com.mmdemirbas.log4j2.configconverter.Serializer.Format.YAML
import com.mmdemirbas.log4j2.configconverter.TestBase
import org.junit.jupiter.api.DisplayName

@DisplayName("sample.yaml")
object SampleYamlTest : TestBase(YAML,
                                 fromResource("/com/mmdemirbas/log4j2/configconverter/sample.yaml"),
                                 Config(status = Level.warn,
                                        name = "YAMLConfigTest",
                                        properties = mutableListOf(Property(name = "filename",
                                                                            value = "target/test-yaml.log")),
                                        filter = mutableListOf(Filter(type = "thresholdFilter",
                                                                      extra = mutableMapOf(
                                                                              "level" to "debug"))),
                                        appenders = mutableListOf(Appender(type = "Console",
                                                                           name = "STDOUT",
                                                                           Layout = Layout(
                                                                                   type = "PatternLayout",
                                                                                   extra = mutableMapOf(
                                                                                           "Pattern" to "%m%n"))),
                                                                  Appender(type = "File",
                                                                           name = "File",
                                                                           Layout = Layout(
                                                                                   type = "PatternLayout",
                                                                                   extra = mutableMapOf(
                                                                                           "Pattern" to "%d %p %C{1.} [%t] %m%n")),
                                                                           filters = mutableListOf(
                                                                                   Filter(type = "ThresholdFilter",
                                                                                          extra = mutableMapOf(
                                                                                                  "level" to "error"))),
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
                                                                                      "value" to 123)))),
                                                       AppenderRef = mutableListOf(
                                                               AppenderRef(ref = "STDOUT"))),
                                                Logger(name = "org.apache.logging.log4j.test2",
                                                       level = Level.debug,
                                                       additivity = false,
                                                       AppenderRef = mutableListOf(
                                                               AppenderRef(ref = "File")))),
                                                          Root = RootLogger(
                                                                  level = Level.error,
                                                                  appenderRef = mutableListOf(
                                                                          AppenderRef(
                                                                                  ref = "STDOUT"))))))
