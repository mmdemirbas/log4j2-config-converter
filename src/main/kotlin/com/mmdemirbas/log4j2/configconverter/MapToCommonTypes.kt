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

inline fun <reified T : Enum<T>> Map<String, Any>?.enum(key: String) =
        string(key)?.asEnum<T>()

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

fun Map<String, Any>?.anyString(keys: List<String>,
                                unacceptables: Set<String?> = emptySet(),
                                default: String?): String? {
    keys.forEach { key ->
        val value = string(key)
        if (!value.isNullOrEmpty()) {
            return value
        }
    }
    return if (unacceptables.any {
                it.equals(default,
                          ignoreCase = true)
            }) null else default
}

fun Map<String, Any>?.string(key: String) = this?.get(key)?.toString()

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

fun <V> Map<String, V>?.without(fullMatches: List<String> = emptyList(),
                                suffices: List<String> = emptyList()) =
        this?.filterNot { (key, _) ->
            key in fullMatches || suffices.any {
                key.endsWith(it)
            }
        }.toMutableMapOrNull()

fun List<Filter>?.toMutableListOrNull() =
        if (this?.isEmpty() == false) toMutableList() else null

inline fun <T, R> Iterable<T>.flatMapMutable(transform: (T) -> Iterable<R>) =
        flatMap(transform).toMutableList()

inline fun <T, R> Iterable<T>.mapMutable(transform: (T) -> R) =
        map(transform).toMutableList()

private fun <K, V> Map<K, V>?.toMutableMapOrNull() =
        if (this?.isEmpty() == false) toMutableMap() else null
