package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Assertions.assertEquals

fun ConfigFormat.assertReadResult(resourceName: String, expected: Config) =
        assertEquals(expected, readResource(resourceName))

fun ConfigFormat.assertReadWriteGivesSameResult(resourceName: String) =
        assertEquals(ConfigFormat::class.java.getResource(resourceName).readText(),
                     readResource(resourceName).toString(this))

fun ConfigFormat.readResource(resourceName: String) =
        read(ConfigFormat::class.java.getResourceAsStream(resourceName).bufferedReader())
