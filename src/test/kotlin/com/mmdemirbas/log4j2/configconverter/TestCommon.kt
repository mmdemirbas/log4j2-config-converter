package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Assertions.assertEquals

fun Format.assertReadResult(resourceName: String, expected: Config) = assertEquals(expected, readResource(resourceName))

fun Format.assertWriteResult(resourceName: String) =
        assertEquals(Format::class.java.getResource(resourceName).readText(), readResource(resourceName).toString(this))

fun Format.readResource(resourceName: String) =
        read(Format::class.java.getResourceAsStream(resourceName).bufferedReader())
