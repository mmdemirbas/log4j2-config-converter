package com.mmdemirbas.log4j2.configconverter

import java.util.*

/**
 * @author Muhammed Demirbaş
 * @since 2018-10-28 17:25
 */


fun Map<String, Any>?.long(key: String) = this?.get(key)?.let {
    when (it) {
        is Long   -> it
        is String -> it.toLong()
        else      -> TODO()
    }
}

fun Map<String, Any>?.int(key: String) = this?.get(key)?.let {
    when (it) {
        is Int    -> it
        is String -> it.toInt()
        else      -> TODO()
    }
}

fun Map<String, Any>?.bool(key: String) = this?.get(key)?.let {
    when (it) {
        is Boolean -> it
        is String  -> it.toBoolean()
        else       -> TODO()
    }
}

inline fun <reified T : Enum<T>> Map<String, Any>?.enum(key: String) = string(key)?.asEnum<T>()

inline fun <reified E : Enum<E>> String?.asEnum() = this?.let {
    E::class.java.enumConstants.firstOrNull {
        it.name.equals(this, ignoreCase = true)
    }
}

fun Map<String, Any>?.list(key: String) = this?.get(key)?.let {
    when (it) {
        is List<*> -> it.toMutableList()
        else       -> mutableListOf(it)
    }
}

fun Map<String, Any>?.map(key: String) = this?.get(key)?.let {
    when {
        it is Map<*, *> -> it as Map<String, Any>
        it is List<*>   -> (it as List<Map<String, Any>>).associate { (it["name"] as String) to it["value"]!! }
        else            -> TODO("key: $key, value (${it.javaClass.name}): $it")
    }
}

fun Map<String, Any>?.string(key: String) = this?.get(key)?.toString()

fun Map<String, Any>?.stringOrNull(key: String, unacceptable: String) =
        this?.get(key)?.toString()?.let { if (it == unacceptable) null else it }

fun <V> Map<String, V>.asCaseInsensitiveMap(): Map<String, V> {
    val indices = keys.withIndex().associate { (index, s) ->
        s.toLowerCase() to index
    }
    val map = TreeMap<String, V>(Comparator.comparing<String, Int> {
        indices[it.toLowerCase()] ?: -1
    })
    map.putAll(this)
    return map
}

fun <V> Map<String, V>?.without(fullMatches: List<String> = emptyList(), suffices: List<String> = emptyList()) =
        this?.filterNot { (key, _) -> key in fullMatches || suffices.any { key.endsWith(it) } }.toMutableMapOrNull()


fun Map<String, Any>.explicit(key: String, default: String?): String? {
    val explicit = this[key] as? String
    return when {
        explicit.isNullOrEmpty() -> default
        else                     -> explicit
    }
}

private fun String?.isNullOrEmpty() = this == null || this.isEmpty()

private fun <K, V> Map<K, V>?.toMutableMapOrNull() = if (this?.isEmpty() == false) toMutableMap() else null
