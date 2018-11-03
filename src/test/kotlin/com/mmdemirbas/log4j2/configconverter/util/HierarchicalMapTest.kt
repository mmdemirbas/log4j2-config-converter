package com.mmdemirbas.log4j2.configconverter.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HierarchicalMapTest {
    @Test
    fun empty() = emptyList<String>() gives emptyMap()

    @Test
    fun `empty lines`() = listOf("", "", "") gives emptyMap()

    @Test
    fun `blank lines`() = listOf("\t", " ", "\r", "\n") gives emptyMap()

    @Test
    fun `top-level key-value pair`() = listOf("a=1") gives mapOf("a" to "1")

    @Test
    fun `inner-level key-value pair`() =
            listOf("a.b.c=1") gives mapOf("a" to mapOf("b" to mapOf("c" to "1")))

    @Test
    fun `multiple branches`() =
            listOf("a.b=1", "a.c=2") gives mapOf("a" to mapOf("b" to "1",
                                                              "c" to "2"))

    @Test
    fun `multiple lines`() = listOf("a.b=1",
                                    "a.c=2",
                                    "a.d.e=3") gives mapOf("a" to mapOf("b" to "1",
                                                                        "c" to "2",
                                                                        "d" to mapOf(
                                                                                "e" to "3")))

    @Test
    fun `no equals sign`() =
            listOf("a", "b", "c") gives mapOf("a" to "", "b" to "", "c" to "")

    @Test
    fun `single equals sign`() = listOf("a=1") gives mapOf("a" to "1")

    @Test
    fun `multiple equals signs`() = listOf("a=b=c") gives mapOf("a" to "b=c")

    @Test
    fun `top-level unique-keys`() =
            listOf("a=b", "c=d") gives mapOf("a" to "b", "c" to "d")

    @Test
    fun `top-level non-unique-keys`() =
            listOf("a=1", "a=2", "a=3") gives mapOf("a" to "3")

    @Test
    fun `multi-level key`() =
            listOf("a.b.c=d") gives mapOf("a" to mapOf("b" to mapOf("c" to "d")))

    @Test
    fun `leading & trailing spaces trimmed`() =
            listOf(" a . b = c ") gives mapOf("a" to mapOf("b" to "c"))

    @Test
    fun `attemp to reuse non-leaf as leaf`() = listOf("a.b.c.d.e=2",
                                                      "a.b.c=1") throws "'a.b.c' used as both leaf and non-leaf node"

    @Test
    fun `attemp to reuse leaf as non-leaf`() = listOf("a.b.c=1",
                                                      "a.b.c.d.e=2") throws "'a.b.c' used as both leaf and non-leaf node"

    @Nested
    inner class `Line continuation` {
        @Test
        fun `2 lines`() {
            listOf("a=123\\", "456") gives mapOf("a" to "123456")
        }

        @Test
        fun `3 lines`() {
            listOf("a=123\\", "456\\", "789") gives mapOf("a" to "123456789")
        }

        @Test
        fun `pre-continuation`() {
            listOf("a=123", "b=456\\", "789") gives mapOf("a" to "123",
                                                          "b" to "456789")
        }

        @Test
        fun `post-continuation`() {
            listOf("a=123\\", "456", "b=789") gives mapOf("a" to "123456",
                                                          "b" to "789")
        }
    }

    private infix fun List<String>.gives(expected: Map<String?, Any>) =
            assertEquals(expected, parseHierarchicalMap(this))

    private infix fun List<String>.throws(expectedMessage: String) =
            expectException<RuntimeException>(expectedMessage) {
                parseHierarchicalMap(this)
            }

    private inline fun <reified T : Throwable> expectException(expected: String,
                                                               noinline executable: () -> Unit) =
            assertEquals(expected, assertThrows<T>(executable).message)
}