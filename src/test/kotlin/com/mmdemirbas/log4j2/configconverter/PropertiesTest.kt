package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * @author Muhammed Demirbaş
 * @since 2018-09-10 01:28
 */
class PropertiesTest {
    @Test
    fun properties() {
        assertEquals(Configuration(status = Level.ERROR,
                                   name = "PropertiesConfig",
                                   dest = "err",
                                   properties = listOf(Property("filename", "target/rolling/rollingtest.log")),
                                   filter = listOf(Filter(type = "ThresholdFilter",
                                                          extra = mutableMapOf("level" to "debug"))),
                                   appenders = listOf(Appender(type = "Console",
                                                               name = "STDOUT",
                                                               layout = Layout(type = "PatternLayout",
                                                                               extra = mutableMapOf("pattern" to "%m%n")),
                                                               filters = listOf(Filter(type = "ThresholdFilter",
                                                                                       extra = mutableMapOf("level" to "error")))),
                                                      Appender(type = "RollingFile",
                                                               name = "RollingFile",
                                                               layout = Layout(type = "PatternLayout",
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
                                   loggers = Loggers(logger = listOf(Logger(name = "com.example.my.app",
                                                                            level = Level.DEBUG,
                                                                            additivity = false,
                                                                            appenderRef = listOf(AppenderRef(ref = "RollingFile")))),
                                                     root = RootLogger(level = Level.INFO,
                                                                       appenderRef = listOf(AppenderRef(ref = "STDOUT"))))),
                     Properties.read("/com/mmdemirbas/log4j2/configconverter/sample.properties"))
    }

    @Nested
    inner class ParseHierarchicalMapTest {
        @Test
        fun empty() = emptyList<String>() gives emptyMap()

        @Test
        fun `empty lines`() = listOf("", "", "") gives emptyMap()

        @Test
        fun `blank lines`() = listOf("\t", " ", "\r", "\n") gives emptyMap()

        @Test
        fun `top-level key-value pair`() = listOf("a=1") gives mapOf("a" to "1")

        @Test
        fun `inner-level key-value pair`() = listOf("a.b.c=1") gives mapOf("a" to mapOf("b" to mapOf("c" to "1")))

        @Test
        fun `multiple branches`() = listOf("a.b=1", "a.c=2") gives mapOf("a" to mapOf("b" to "1", "c" to "2"))

        @Test
        fun `multiple lines`() = listOf("a.b=1", "a.c=2", "a.d.e=3") gives mapOf("a" to mapOf("b" to "1",
                                                                                              "c" to "2",
                                                                                              "d" to mapOf("e" to "3")))

        @Test
        fun `no equals sign`() = listOf("a", "b", "c") gives mapOf("a" to "", "b" to "", "c" to "")

        @Test
        fun `single equals sign`() = listOf("a=1") gives mapOf("a" to "1")

        @Test
        fun `multiple equals signs`() = listOf("a=b=c") gives mapOf("a" to "b=c")

        @Test
        fun `top-level unique-keys`() = listOf("a=b", "c=d") gives mapOf("a" to "b", "c" to "d")

        @Test
        fun `top-level non-unique-keys`() = listOf("a=1", "a=2", "a=3") gives mapOf("a" to "3")

        @Test
        fun `multi-level key`() = listOf("a.b.c=d") gives mapOf("a" to mapOf("b" to mapOf("c" to "d")))

        @Test
        fun `leading & trailing spaces trimmed`() = listOf(" a . b = c ") gives mapOf("a" to mapOf("b" to "c"))

        @Test
        fun `attemp to reuse non-leaf as leaf`() =
                listOf("a.b.c.d.e=2", "a.b.c=1") throws "'a.b.c' used as both leaf and non-leaf node"

        @Test
        fun `attemp to reuse leaf as non-leaf`() =
                listOf("a.b.c=1", "a.b.c.d.e=2") throws "'a.b.c' used as both leaf and non-leaf node"

        private infix fun List<String>.gives(expected: Map<String, Any>) = assertEquals(expected, exec())
        private infix fun List<String>.throws(expected: String) = expectException<RuntimeException>(expected) { exec() }
        private fun List<String>.exec() = Properties.parseHierarchicalMap(this)

        private inline fun <reified T : Throwable> expectException(expected: String, noinline executable: () -> Unit) =
                assertEquals(expected, assertThrows<T>(executable).message)
    }
}