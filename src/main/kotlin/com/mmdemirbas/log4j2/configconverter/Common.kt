package com.mmdemirbas.log4j2.configconverter

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BigIntegerNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.databind.node.FloatNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.LongNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.databind.node.ShortNode
import com.fasterxml.jackson.databind.node.TextNode
import org.w3c.dom.Attr
import org.w3c.dom.CDATASection
import org.w3c.dom.Comment
import org.w3c.dom.Document
import org.w3c.dom.DocumentFragment
import org.w3c.dom.DocumentType
import org.w3c.dom.Element
import org.w3c.dom.Entity
import org.w3c.dom.EntityReference
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.NodeList
import org.w3c.dom.Notation
import org.w3c.dom.ProcessingInstruction
import org.w3c.dom.Text
import java.io.Reader
import java.io.Writer
import java.util.*


abstract class Format {
    abstract fun read(reader: Reader): Config
    abstract fun write(config: Config, writer: Writer)
}

// todo: declared type immutable iken default value'lar mutable tanımlansa da bunun bir kullanım alanı var mı? Belki java tarafına geçince anlamlı oluyordur.


// todo: out-of-the-box layout'lar karşılık gelen sınıflar tek tek yazılarak support edilebilir => buna gerek kalmayabilir extra map sayesinde

// todo: support Script, ScriptFile and ScriptRef separately => genel olarak Log4J'nin plugin'lere gerenic yaklaşımı gibi yaklaşılabilir. Sadece type özel olarak ele alınıp diğer herşey extra gibi değerlendirilebilir.
// belki extra'larda da top-level item'lar type içerebilir diye düşünülse iyi olabilir

// todo: copy other samples from log4j website & repos for testing


infix fun <K, V> Map<K, V>.without(key: K) = without(listOf(key))
fun <K, V> Map<K, V>.without(vararg keys: K) = without(keys.asList())
infix fun <K, V> Map<K, V>.without(keys: List<K>): MutableMap<K, V>? {
    val map = (this - keys).toMutableMap()
    return if (map.isEmpty()) null else map
}

inline fun <reified E : Enum<E>> String?.asEnum() = this?.let {
    E::class.java.enumConstants.firstOrNull {
        it.name.equals(this, ignoreCase = true)
    }
}

inline fun <K, V, R> Map<out K, V>.mapMutable(transform: (Map.Entry<K, V>) -> R) = map(transform).toMutableList()

inline fun <T, R> Iterable<T>.flatMapMutable(transform: (T) -> Iterable<R>) = flatMap(transform).toMutableList()
inline fun <T, R> Iterable<T>.mapMutable(transform: (T) -> R) = map(transform).toMutableList()


fun <E> List<E>?.unwrapIfSingle() = if (this?.size == 1) this[0] else this

fun <K, V> Map<K, V>?.toMutableMapOrNull() = if (this?.isEmpty() == false) toMutableMap() else null
fun List<Filter>?.toMutableListOrNull() = if (this?.isEmpty() == false) toMutableList() else null


/**
 * Returns map of the given pairs except the empty ones according to [isEmptyValue] method.
 */
fun <K, V> mapOfNonEmpty(vararg pairs: Pair<K, V?>?) = mapOfNonEmpty(pairs.asList())

/**
 * Returns map of the given pairs except the empty ones according to [isEmptyValue] method.
 */
fun <K, V> mapOfNonEmpty(pairs: List<Pair<K, V?>?>) =
        pairs.filterNotNull().filterNot { it.isEmptyValue() }.map { (k, v) ->
            k to (v ?: throw RuntimeException("value was null for key $k"))
        }.toMap()

/**
 * Returns `true` only for the following:
 * 1. `null`
 * 2.  Empty [Collection] and [Map].
 * 3. [Pair]s where second value is empty according to this method.
 */
fun Any?.isEmptyValue(): Boolean {
    return when (this) {
        null             -> true
        is Collection<*> -> isEmpty()
        is Map<*, *>     -> isEmpty()
        is Pair<*, *>    -> second.isEmptyValue()
        else             -> false
    }
}


fun readWithMapper(reader: Reader, objectMapper: ObjectMapper): Config {
    val foundRoot = objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true).readTree(reader)
    val logicalRoot = foundRoot.singleOrNull() ?: foundRoot
    return (logicalRoot.jsonNodeToMap() as Map<String, Any>).toConfig()
}

fun Config.writeWithMapper(writer: Writer, objectMapper: ObjectMapper) {
    objectMapper.writeValue(writer, mapOf("Configuration" to this))
}

fun JsonNode.jsonNodeToMap(): Any? {
    return when (this) {
        is ArrayNode      -> elements().asSequence().map { it.jsonNodeToMap() }.toList()
        is ObjectNode     -> fields().asSequence().associate { (k, v) -> k to v.jsonNodeToMap() }
        is NullNode       -> null
        is MissingNode    -> TODO()
        is TextNode       -> asText()
        is IntNode        -> asInt()
        is ShortNode      -> TODO()
        is LongNode       -> asLong()
        is DoubleNode     -> asDouble()
        is FloatNode      -> TODO()
        is BigIntegerNode -> TODO()
        is DecimalNode    -> TODO()
        is BooleanNode    -> asBoolean()
        is BinaryNode     -> TODO()
        is POJONode       -> TODO()
        else              -> TODO()
    }
}

fun org.w3c.dom.Node.domNodeToMap(): Any? {
    return when (this) {
        is Element               -> {
            val all = attributes.toList() + childNodes.toList()
            val filtered = all.mapNotNull {
                when (it) {
                    is Element, is Attr -> it.nodeName to it.domNodeToMap()
                    else                -> null
                }
            }
            val text = all.filter { it is Text }.joinToString("") { it.nodeValue }.trim()
            when {
                filtered.isEmpty() -> text
                else               -> {
                    val selected = if (text.isEmpty()) filtered else filtered + ("value" to text)
                    val names = selected.map { it.first }
                    val unique = names.distinct().size == names.size
                    when {
                        unique -> selected.toMap()
                        else   -> selected.map { mapOf(it) }
                    }
                }
            }
        }
        is Attr                  -> value
        is Text                  -> wholeText
        is CDATASection          -> TODO()
        is EntityReference       -> TODO()
        is Entity                -> TODO()
        is ProcessingInstruction -> TODO()
        is Comment               -> null
        is Document              -> TODO()
        is DocumentType          -> TODO()
        is DocumentFragment      -> TODO()
        is Notation              -> TODO()
        else                     -> TODO()
    }
}

private fun NamedNodeMap.toList() = (0 until length).map(this::item)
private fun NodeList.toList() = (0 until length).map(this::item)


fun Map<String, Any>.toConfig(): Config {
    // todo: strict mode support - strict mode'da type attribute'u kullanmak zorunlu olsun, element name'le type bildirmek kabul edilmesin
    val map = asCaseInsensitiveMap()
    return Config(advertiser = map.string("advertiser"),
                  dest = map.string("dest"),
                  monitorIntervalSeconds = map.int("monitorInterval"),
                  name = map.string("name"),
                  packages = map.string("packages")?.split(',')?.mapMutable { it.trim() },
                  schemaResource = map.string("schema"),
                  isShutdownHookEnabled = map.string("shutdownHook")?.equals("disable")?.not(),
                  status = map.enum<Level>("status"),
                  strict = map.bool("strict"),
                  shutdownTimeoutMillis = map.long("shutdownTimeout"),
                  verbose = map.string("verbose"),
                  properties = map.toProperties(),
                  scripts = null, // todo: scripts support
                  customLevels = null, // todo: customLevels support
                  filter = map.toFilters(),
                  appenders = map.toAppenders(),
                  loggers = map.toLoggers())
}

fun Map<String, Any>.toProperties(): MutableList<Property>? {
    return list("Properties")?.flatMapMutable { x ->
        (x as Map<String, Any>).toProperty()
    }
}

fun Map<String, Any>.toProperty(): List<Property> {
    return asCaseInsensitiveMap().list("property")!!.map { propertyMap ->
        propertyMap as Map<String, Any>
        Property(name = propertyMap.string("name"), value = propertyMap.string("value"))
    }
}

@Suppress("USELESS_CAST")
fun Map<String, Any>.toAppenders(): MutableList<Appender>? {
    val appenders = this["Appenders"]
    return when (appenders) {
        is List<*>   -> appenders.map {
            it as Map<String, Any>
            "Appender" to (it.asCaseInsensitiveMap().map("Appender") ?: it)
        }
        is Map<*, *> -> (appenders as Map<String, Any>?)?.asCaseInsensitiveMap()?.let { appendersMap ->
            val appenders = appendersMap["Appender"] ?: appendersMap
            when (appenders) {
                is List<*>   -> appenders.map { appender ->
                    // todo: cast'ler varsayımsal olmasın, handle edilmeyen case'leri gölgelemesin.
                    appender as Map<String, Any>
                    appender.string("type") to appender.asCaseInsensitiveMap()
                }
                is Map<*, *> -> appenders.entries.flatMap { (alias, appender) ->
                    when (appender) {
                        is List<*>   -> {
                            val list = appender as List<Map<String, Any>>
                            list.map {
                                it.explicit("alias",
                                            alias as String) to (it + mapOfNonEmpty("type" to it.explicit("type",
                                                                                                          alias as String)))
                            }
                        }
                        is Map<*, *> -> {
                            appender as Map<String, Any>
                            listOf(appender.explicit("alias",
                                                     alias as String) to (appender + mapOfNonEmpty("type" to appender.explicit(
                                    "type",
                                    alias as String))).asCaseInsensitiveMap())
                        }
                        else         -> TODO()
                    }
                }
                else         -> TODO()
            }
        }
        else         -> TODO()
    }?.mapMutable { (alias, appender) ->
        appender.toAppender(alias)
    }
}

private fun Map<String, Any>.explicit(key: String, default: String?): String? {
    val explicit = this[key] as? String
    return when {
        explicit.isNullOrEmpty() -> default
        else                     -> explicit
    }
}

fun Map<String, Any>.toAppender(alias: String?): Appender {
    val type = effectiveType(alias!!, "Appender")
    return Appender(alias = if (alias.equals(type, ignoreCase = true) || alias.equals("appender",
                                                                                      ignoreCase = true)) null else alias,
                    type = type,
                    name = string("name"),
                    Layout = toLayout(),
                    filters = toFilters(),
                    extra = without(fullMatches = listOf("type", "name", "alias", "Filters", "Layout"),
                                    suffices = listOf("Layout", "Filter")))
}

fun Map<String, Any>.toLoggers(): Loggers {
    val wrapper = this["Loggers"]
    val loggers = when (wrapper) {
        is Map<*, *> -> (wrapper as Map<String, Any>).asCaseInsensitiveMap().list("Logger")
        is List<*>   -> wrapper.filter { (it as? Map<String, Any>)?.entries?.firstOrNull()?.key != "Root" }
        else         -> TODO()
    }
    val root = when (wrapper) {
        is Map<*, *> -> (wrapper as Map<String, Any>).asCaseInsensitiveMap().map("Root")
        is List<*>   -> wrapper.firstOrNull { (it as? Map<String, Any>)?.entries?.firstOrNull()?.key == "Root" } as Map<String, Any>
        else         -> TODO()
    }
    return Loggers(Logger = loggers?.mapMutable {
        val map = it as Map<String, Any>
        val (alias, logger) = map.entries.first()
        when (logger) {
            is Map<*, *> -> (logger as Map<String, Any>).toLogger(alias)
            else         -> map.toLogger("Logger")
        }
    }, Root = root?.toRootLogger())
}

fun Map<String, Any>.toLogger(alias: String): Logger {
    return Logger(alias = effectiveType(explicit("alias", alias)!!, "Logger"),
                  name = string("name"),
                  level = enum<Level>("level"),
                  additivity = bool("additivity"),
                  filter = toFilters(),
                  AppenderRef = toAppenderRefs(),
                  extra = without(fullMatches = listOf("name", "alias", "level", "additivity", "AppenderRef"),
                                  suffices = listOf("Filter")))
}

fun Map<String, Any>.toRootLogger(): RootLogger {
    val root = map("Root") ?: this
    return RootLogger(level = root.enum<Level>("level"),
                      filter = root.toFilters(),
                      appenderRef = root.toAppenderRefs(),
                      extra = root.without(fullMatches = listOf("level", "AppenderRef"), suffices = listOf("Filter")))
}

private fun Map<String, Any>?.toFilters(): MutableList<Filter>? {
    val items =
            list("Filters") ?: list("Filter")?.map { mapOf("Filter" to it) }
            ?: this?.keys?.filter { it.endsWith("Filter") }?.map { key ->
                mapOf(key to map(key)!!)
            }
    return items?.mapMutable { item ->
        val (alias, props) = (item as Map<String, Any>).entries.first()
        when (props) {
            is List<*>   -> TODO()
            is Map<*, *> -> (props as Map<String, Any>).toFilter(props.explicit("alias", alias)!!)
            else         -> TODO("type: ${props.javaClass.name}  toString: $props")
        }
    }.toMutableListOrNull()
}

fun Map<String, Any>.toFilter(alias: String): Filter {
    val type = effectiveType(alias, "Filter")
    return Filter(alias = if (alias.equals(type, ignoreCase = true) || alias.equals("filter",
                                                                                    ignoreCase = true)) null else alias,
                  type = type,
                  onMismatch = enum<FilterDecision>("onMismatch"),
                  onMatch = enum<FilterDecision>("onMatch"),
                  extra = without(fullMatches = listOf("type", "alias", "onMismatch", "onMatch")))
}

fun Map<String, Any>.toLayout(): Layout? {
    val layoutMap = map("Layout")?.let { mapOf("Layout" to it) } ?: keys.filter { it.endsWith("Layout") }.map { key ->
        mapOf(key to map(key))
    }.firstOrNull()
    return layoutMap?.let {
        val (alias, layout) = layoutMap.entries.first()
        Layout(type = layout.effectiveType(alias, "Layout"),
               extra = layout.without(fullMatches = listOf("type", "Layout"), suffices = listOf("Layout")))
    }
}

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

fun Map<String, Any>?.toAppenderRefs() = map("AppenderRef")?.let { appenderRef ->
    mutableListOf(AppenderRef(alias = appenderRef.stringOrNull("alias", "appender"),
                              ref = appenderRef.string("ref"),
                              filter = appenderRef.toFilters()))
}

fun Map<String, Any>?.effectiveType(suggested: String, unacceptable: String): String? {
    val type = string("type")
    return when {
        type?.isNotEmpty() == true                        -> type
        suggested.equals(unacceptable, ignoreCase = true) -> type
        else                                              -> suggested
    }
}

fun <V> Map<String, V>?.without(fullMatches: List<String> = emptyList(), suffices: List<String> = emptyList()) =
        this?.filterNot { (key, _) -> key in fullMatches || suffices.any { key.endsWith(it) } }.toMutableMapOrNull()

fun String?.isNullOrEmpty() = this == null || this.isEmpty()


fun Config.configToYamlMap() = mapOfNonEmpty("Configuration" to mapOfNonEmpty("advertiser" to advertiser,
                                                                              "dest" to dest,
                                                                              "monitorInterval" to monitorIntervalSeconds,
                                                                              "name" to name,
                                                                              "packages" to packages?.joinToString(),
                                                                              "schema" to schemaResource,
                                                                              "shutdownHook" to isShutdownHookEnabled,
                                                                              "status" to status?.name,
                                                                              "strict" to strict,
                                                                              "shutdownTimeout" to shutdownTimeoutMillis,
                                                                              "verbose" to verbose,
                                                                              "properties" to mapOfNonEmpty("property" to properties.orEmpty().map {
                                                                                  mapOf("name" to it.name,
                                                                                        "value" to it.value)
                                                                              }.unwrapIfSingle()),
                                                                              "script" to scripts,
                                                                              "customLevel" to customLevels) + filter.filters() + mapOf(
        "appenders" to appenders?.groupBy { it.type }?.entries?.associate { (type, appenders) ->
            // todo: burada olduğu gibi associate kullanılan diğer yerlerde de key'lerin birbirini ezmediğinden emin ol.
            type to appenders.map { appender ->
                mapOfNonEmpty("name" to appender.name,
                        // todo: alias'ı sırf properties ile reversible yapabilmek için koydum, non-standard property. belki bir flag'e bağlanabilir. başka yerlerde de aynı pattern var.
                              "alias" to appender.alias) + appender.extra.orEmpty() + appender.Layout?.type?.let {
                    mapOf(it to appender.Layout?.extra)
                }.orEmpty() + mapOfNonEmpty("Filters" to appender.filters.filters())

            }.unwrapIfSingle()
        },
        "Loggers" to mapOf("logger" to loggers?.Logger?.map {
            mapOfNonEmpty("name" to it.name,
                          "alias" to it.alias,
                          "level" to it.level?.name,
                          "additivity" to it.additivity) + it.filter.filters() + mapOf("AppenderRef" to it.AppenderRef.appenderRefs()) + it.extra.orEmpty()
        }.unwrapIfSingle(), "Root" to loggers?.Root?.let {
            mapOfNonEmpty("level" to it.level?.name) + it.filter.filters() + mapOf("AppenderRef" to it.appenderRef.appenderRefs()) + it.extra.orEmpty()
        })))

private fun Iterable<AppenderRef>?.appenderRefs() = this?.map {
    mapOfNonEmpty("ref" to it.ref, "alias" to it.alias) + it.filter.filters()
}.unwrapIfSingle()

private fun Iterable<Filter>?.filters() = this?.associate {
    it.type to mapOfNonEmpty("alias" to it.alias,
                             "onMismatch" to it.onMismatch,
                             "onMatch" to it.onMatch) + it.extra.orEmpty()
}.orEmpty()
