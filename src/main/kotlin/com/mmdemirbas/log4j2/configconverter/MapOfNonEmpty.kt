package com.mmdemirbas.log4j2.configconverter

/**
 * @author Muhammed Demirbaş
 * @since 2018-10-28 17:25
 */

/**
 * Returns map of the given pairs except the empty ones according to [isEmptyValue] method.
 */
fun <K, V> mapOfNonEmpty(vararg pairs: Pair<K, V?>?) =
        pairs.asList().filterNotNull().filterNot { it.isEmptyValue() }.map { (k, v) ->
            k to (v ?: throw RuntimeException("value was null for key $k"))
        }.toMap()

/**
 * Returns `true` only for the following:
 * 1. `null`
 * 2.  Empty [Collection] and [Map].
 * 3. [Pair]s where second value is empty according to this method.
 */
private fun Any?.isEmptyValue(): Boolean {
    return when (this) {
        null             -> true
        is Collection<*> -> isEmpty()
        is Map<*, *>     -> isEmpty()
        is Pair<*, *>    -> second.isEmptyValue()
        else             -> false
    }
}

