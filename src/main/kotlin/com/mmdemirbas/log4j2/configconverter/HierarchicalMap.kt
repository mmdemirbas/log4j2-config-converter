package com.mmdemirbas.log4j2.configconverter

import java.io.Writer

/**
 * @author Muhammed Demirbaş
 * @since 2018-09-14 08:44
 */
fun parseHierarchicalMap(lines: List<String>): Map<String, Any> {
    // todo: support comments starting with # -- for full compatibility see Wikipedia/.properties
    val map = mutableMapOf<String, Any>()
    val normalizedLines = mutableListOf<String>()
    lines.filter { it.isNotBlank() }.forEach { item ->
        normalizedLines += when {
            normalizedLines.lastOrNull()?.endsWith("\\") == true -> {
                val previousItem =
                        normalizedLines.removeAt(normalizedLines.size - 1)
                previousItem.substring(0, previousItem.length - 1) + item
            }
            else                                                 -> item
        }
    }
    normalizedLines.forEach { line ->
        val path = line.substringBefore('=').split('.').map { it.trim() }
        val value = line.substringAfter('=', "").trim()
        var targetMap = map
        path.dropLast(1).forEachIndexed { index, part ->
            targetMap = targetMap.getOrPut(part) { mutableMapOf<String, Any>() } as? MutableMap<String, Any> ?:
                    throw RuntimeException("'${path.subList(0,
                                                            index + 1).joinToString(
                            ".")}' used as both leaf and non-leaf node")
        }
        val existingValue = targetMap.put(path.last(), value.trim())
        if (existingValue is Map<*, *>) throw RuntimeException("'${path.joinToString(
                ".")}' used as both leaf and non-leaf node")
    }
    return map
}


fun Writer.writeHierarchicalMap(prefix: String = "", map: Map<String?, *>?) {
    val prefixAndDot = if (prefix.isEmpty()) "" else "$prefix."
    map?.forEach { key, value ->
        val fullKey = "$prefixAndDot$key"
        when (value) {
            null         -> {
                // skip
            }
            is String    -> writePair(fullKey, value)
            is Int       -> writePair(fullKey, value)
            is Long      -> writePair(fullKey, value)
            is Boolean   -> writePair(fullKey, value)
            is Enum<*>   -> writePair(fullKey, value)
            is List<*>   -> writePair(fullKey, value.joinToString())
            is Map<*, *> -> writeHierarchicalMap(fullKey,
                                                 value as Map<String?, *>)
            else         -> TODO("Key '$key' has a value of an unsupported type ${value.javaClass}")
        }
    }
}

private fun Writer.writePair(fullKey: String, value: Any?) =
        write("$fullKey=$value\n")