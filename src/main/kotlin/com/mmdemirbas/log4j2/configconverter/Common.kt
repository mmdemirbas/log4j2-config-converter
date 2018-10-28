package com.mmdemirbas.log4j2.configconverter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
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
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import org.apache.logging.log4j.core.util.Throwables
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
import org.xml.sax.InputSource
import java.io.Reader
import java.io.StringReader
import java.io.StringWriter
import java.io.Writer
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.reflect.KMutableProperty


abstract class Format {
    fun read(resourceName: String) = read(Format::class.java.getResourceAsStream(resourceName).bufferedReader())

    fun write(config: Config) = StringWriter().apply { use { write(config, it) } }.toString()

    abstract fun read(reader: Reader): Config
    abstract fun write(config: Config, writer: Writer)
}

// todo: declared type immutable iken default value'lar mutable tanımlansa da bunun bir kullanım alanı var mı? Belki java tarafına geçince anlamlı oluyordur.
/**
 *
 * @property advertiser (Optional) The Advertiser plugin name which will be used to advertise individual FileAppender or SocketAppender configurations. The only Advertiser plugin provided is 'multicastdns".
 * @property dest  Either "err", which will send output to stderr, or a file path or URL.
 * @property monitorIntervalSeconds The minimum amount of time, in seconds, that must elapse before the file configuration is checked for changes.
 * @property name The name of the configuration.
 * @property packages A comma separated list of package names to search for plugins. Plugins are only loaded once per classloader so changing this value may not have any effect upon reconfiguration.
 * @property schemaResource Identifies the location for the classloader to located the XML Schema to use to validate the configuration. Only valid when strict is set to true. If not set no schema validation will take place.
 * @property isShutdownHookEnabled Specifies whether or not Log4j should automatically shutdown when the JVM shuts down. The shutdown hook is enabled by default but may be disabled by setting this attribute to "disable"
 * @property shutdownTimeoutMillis Specifies how many milliseconds appenders and background tasks will get to shutdown when the JVM shuts down. Default is zero which mean that each appender uses its default timeout, and don't wait for background tasks. Not all appenders will honor this, it is a hint and not an absolute guarantee that the shutdown procedure will not take longer. Setting this too low increase the risk of losing outstanding log events not yet written to the final destination. See LoggerContext.stop(long, java.util.concurrent.TimeUnit). (Not used if shutdownHook is set to "disable".)
 * @property status The level of internal Log4j events that should be logged to the console. Valid values for this attribute are "trace", "debug", "info", "warn", "error" and "fatal". Log4j will log details about initialization, rollover and other internal actions to the status logger. Setting status="trace" is one of the first tools available to you if you need to troubleshoot log4j. (Alternatively, setting system property log4j2.debug will also print internal Log4j2 logging to the console, including internal logging that took place before the configuration file was found.)
 * @property strict Enables the use of the strict XML format. Not supported in JSON configurations.
 * @property verbose Enables diagnostic information while loading plugins.
 * @property properties
 * @property scripts
 * @property customLevels
 * @property filter
 * @property appenders
 * @property loggers
 * @property extra
 */
data class Config(var advertiser: String? = null,
                  var dest: String? = null,
                  var monitorIntervalSeconds: Int? = null,
                  var name: String? = null,
                  var packages: MutableList<String?>? = null,
                  var schemaResource: String? = null,
                  var isShutdownHookEnabled: Boolean? = null,
                  var status: Level? = null,
                  var strict: Boolean? = null,
                  var shutdownTimeoutMillis: Long? = null,
                  var verbose: String? = null,
                  var properties: MutableList<Property>? = null,
                  var scripts: MutableList<Script>? = null,
                  var customLevels: MutableList<CustomLevel>? = null,
                  var filter: MutableList<Filter>? = null,
                  var appenders: MutableList<Appender>? = null,
                  var loggers: Loggers? = null,
                  var extra: MutableMap<String, Any?>? = null) {
    fun toString(format: Format) = format.write(this)
}


enum class Level { all, trace, debug, info, warn, error, fatal, off }

data class Property(var name: String? = null, var value: String? = null)

// todo: out-of-the-box layout'lar karşılık gelen sınıflar tek tek yazılarak support edilebilir => buna gerek kalmayabilir extra map sayesinde

// todo: support Script, ScriptFile and ScriptRef separately => genel olarak Log4J'nin plugin'lere gerenic yaklaşımı gibi yaklaşılabilir. Sadece type özel olarak ele alınıp diğer herşey extra gibi değerlendirilebilir.
// belki extra'larda da top-level item'lar type içerebilir diye düşünülse iyi olabilir

// todo: copy other samples from log4j website & repos for testing

data class Script(var type: String? = null,
                  var name: String? = null,
                  var language: String? = null,
                  var text: String? = null,
                  var path: String? = null)

data class CustomLevel(var name: String? = null, var value: Int? = null)

enum class FilterDecision { ACCEPT, NEUTRAL, DENY }

data class Filter(var alias: String? = null,
                  var type: String? = null,
                  var onMismatch: FilterDecision? = null,
                  var onMatch: FilterDecision? = null,
                  var extra: MutableMap<String, Any?>? = null)


data class Appender(var alias: String? = null,
                    var type: String? = null,
                    var name: String? = null,
                    var Layout: Layout? = null,
                    var filters: MutableList<Filter>? = null,
                    var extra: MutableMap<String, Any?>? = null)


data class Layout(var type: String? = null, var extra: MutableMap<String, Any?>? = null)

data class Loggers(var Logger: MutableList<Logger>? = null, var Root: RootLogger? = null)

data class Logger(var alias: String? = null,
                  var name: String? = null,
                  var level: Level? = null,
                  var additivity: Boolean? = null,
                  var filter: MutableList<Filter>? = null,
                  var AppenderRef: MutableList<AppenderRef>? = null,
                  var extra: MutableMap<String, Any?>? = null)


data class RootLogger(var level: Level? = null,
                      var filter: MutableList<Filter>? = null,
                      var appenderRef: MutableList<AppenderRef>? = null,
                      var extra: MutableMap<String, Any?>? = null)

data class AppenderRef(var alias: String? = null, var ref: String? = null, var filter: MutableList<Filter>? = null)


fun MutableMap<String, Any?>?.toDynamicObject(): DynamicObject? {
    if (this == null) return null

    val valueEntry = this["value"]
    val hasValue = valueEntry is String
    val value = if (hasValue) valueEntry.toString() else ""
    val extras = if (hasValue) this - "value" else this

    return DynamicObject(value = value, extra = extras.entries.associateMutable { (key, value) ->
        key to when (value) {
            is MutableMap<*, *> -> mutableListOf((value as MutableMap<String, Any?>).toDynamicObject())
            is String           -> mutableListOf(DynamicObject(value = value))
            is List<*>          -> value.mapMutable { DynamicObject(value = it.toString()) }
            else                -> TODO("Unsupported type: ${value?.javaClass?.name}: $value")
        }
    })
}

// todo: manual parsing işlemlerinden sonra DynamicObject gereksiz hale gelmiş olabilir. önce write support ekle, sonra deprecated'ları kaldır, sonra da DynamicObject'i

data class DynamicObject @JvmOverloads constructor(val value: String = "",
                                                   var extra: MutableMap<String, MutableList<out DynamicObject?>>? = null) {
    override fun toString() = when {
        value.isEmpty()          -> extra.toString()
        extra?.isEmpty() == true -> value
        else                     -> "$value $extra"
    }

    fun asFilterDecision(key: String) = singleValueOrNull(key)?.let { FilterDecision.valueOf(it) }
    fun singleValueOrNull(key: String) = extra?.get(key)?.singleOrNull()?.value

    operator fun set(name: String, value: DynamicObject) {
        TODO()
    }
}


fun lateinitDynamicObject(prop: KMutableProperty<DynamicObject?>) = lateinit(prop) { DynamicObject() }

fun <K, V> lateinitMap(prop: KMutableProperty<MutableMap<K, V>?>) = lateinit(prop) { mutableMapOf() }

fun <T> lateinitList(prop: KMutableProperty<MutableList<T>?>) = lateinit(prop) { mutableListOf() }
fun <R> lateinit(prop: KMutableProperty<R?>, supply: () -> R): R {
    if (prop.getter.call() == null) prop.setter.call(supply())
    return prop.getter.call()!!
}


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
val <V> Map<*, V>.valuesList: MutableList<V> get() = values.toMutableList()
inline fun <T, K, V> Iterable<T>.associateMutable(transform: (T) -> Pair<K, V>) = associate(transform).toMutableMap()


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
    return Appender(alias = if (alias == type) null else alias,
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
    return Filter(alias = if (alias == type) null else alias,
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

object Json : Format() {
    private val mapper =
            ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    override fun read(reader: Reader) = readWithMapper(reader, mapper)
    override fun write(config: Config, writer: Writer) = config.writeWithMapper(writer, mapper)
}

object Yaml : Format() {
    val mapper =
            YAMLMapper().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .disable(YAMLGenerator.Feature.MINIMIZE_QUOTES).setSerializationInclusion(JsonInclude.Include.NON_NULL)

    override fun read(reader: Reader) = readWithMapper(reader, mapper)
    override fun write(config: Config, writer: Writer) = config.writeWithMapper(writer, mapper)
}

object SnakeYaml : Format() {
    // todo: unwrapIfSingle özelliğinin çalıştığından emin olmak için test yazılabilir. Benzer şekilde farklı feature'lar için testler yazılmalı

    // todo: unwrapIfSingle özelliği, generate edilen map'lerde kullanılmalı mı? Kullanılacaksa mümkün olan her yerde mi kullanılsa? Okuma kısmı nasıl olacak?

    // todo: extra'lar genelde key-value pair'ler olduğu için composite entry'lerden önce dump edilebilir. Ya da simple olan kısımları önce, composite olanlar sonra yapılabilir.

    // todo: equalsIgnoreCase kullanılmalı

    // todo: parsing işlemi olabildiğince toleranslı yapılsın. Parse edilemeyen kısımla ilgili warning verilsin ama işlem iptal edilmesin.

    override fun read(reader: Reader) =
            (org.yaml.snakeyaml.Yaml().load(reader) as Map<String, Any>).map("Configuration")!!.toConfig()

    override fun write(config: Config, writer: Writer) =
            writer.write(org.yaml.snakeyaml.Yaml().dumpAsMap(config.configToYamlMap()))
}

object Xml : Format() {
    private const val XINCLUDE_FIXUP_LANGUAGE = "http://apache.org/xml/features/xinclude/fixup-language"
    private const val XINCLUDE_FIXUP_BASE_URIS = "http://apache.org/xml/features/xinclude/fixup-base-uris"

    override fun read(reader: Reader): Config {
        val buffer = reader.readText()
        val root = parseXml(buffer)
        val map = root.domNodeToMap() as Map<String, Any>
        return map.toConfig()
    }

    override fun write(config: Config, writer: Writer) {
        TODO("not implemented")
    }

    private fun parseXml(xmlString: String) = try {
        parseXml(xmlString, true)
    } catch (e: Exception) {
        // LOG4J2-1127
        if (Throwables.getRootCause(e) is UnsupportedOperationException) {
            parseXml(xmlString, false)
        } else {
            throw e
        }
    }

    private fun parseXml(xmlString: String, enableXInclude: Boolean) = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true

        if (true) {
            isValidating = false
            isExpandEntityReferences = false
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        }

        if (enableXInclude) {
            isXIncludeAware = true
            setFeature(XINCLUDE_FIXUP_BASE_URIS, true)
            setFeature(XINCLUDE_FIXUP_LANGUAGE, true)
        }
    }.newDocumentBuilder().parse(InputSource(StringReader(xmlString))).documentElement!!
}

object Properties : Format() {
    // todo: support comments and empty lines when loading

    override fun read(reader: Reader): Config {
        val lines = reader.readLines()
        val config = parseHierarchicalMap(lines)
        return Config(advertiser = config["advertiser"]?.toString(),
                      dest = config["dest"]?.toString(),
                      monitorIntervalSeconds = config["monitorInterval"]?.toString()?.toInt(),
                      name = config["name"]?.toString(),
                      packages = config["packages"]?.toString()?.split(',')?.toMutableList(),
                      schemaResource = config["schema"]?.toString(),
                      isShutdownHookEnabled = config["shutdownHook"]?.toString()?.toBoolean(),
                      status = config["status"]?.toString()?.asEnum<Level>(),
                      strict = config["strict"]?.toString()?.toBoolean(),
                      shutdownTimeoutMillis = config["shutdownTimeout"]?.toString()?.toLong(),
                      verbose = config["verbose"]?.toString(),
                      properties = (config["property"] as? Map<String, String>)?.mapMutable { (key, value) ->
                          Property(name = key, value = value)
                      },
                      scripts = null, // map["scripts"]?.toString(),
                      customLevels = null, // map["customLevels"]?.toString(),
                      filter = config.filters(),
                      appenders = (config["appender"] as? Map<String, Map<String, Any>>)?.mapMutable { (alias, appender) ->
                          Appender(alias = alias,
                                   type = appender["type"]?.toString(),
                                   name = appender["name"]?.toString(),
                                   Layout = (appender["layout"] as? Map<String, Any>)?.let { layoutMap ->
                                       Layout(type = layoutMap["type"]?.toString(), extra = layoutMap without "type")
                                   },
                                   filters = appender.filters(),
                                   extra = appender.without("type", "name", "layout", "filter"))
                      },
                      loggers = Loggers(Logger = (config["logger"] as? Map<String, Map<String, Any>>)?.mapMutable { (alias, logger) ->
                          Logger(alias = alias,
                                 name = logger["name"]?.toString(),
                                 level = logger["level"]?.toString()?.asEnum<Level>(),
                                 additivity = logger["additivity"]?.toString()?.toBoolean(),
                                 filter = logger.filters(),
                                 AppenderRef = logger.appenderRefs(),
                                 extra = logger.without("name", "level", "additivity", "filter", "appenderRef"))
                      }, Root = (config["rootLogger"] as? Map<String, Any>)?.let { rootLogger ->
                          RootLogger(level = rootLogger["level"]?.toString()?.asEnum<Level>(),
                                     filter = rootLogger.filters(),
                                     appenderRef = rootLogger.appenderRefs(),
                                     extra = rootLogger.without("level", "filter", "appenderRef"))
                      }))
    }

    private fun Map<String, Any>.appenderRefs() =
            (this["appenderRef"] as? Map<String, Map<String, Any>>)?.mapMutable { (alias, appenderRef) ->
                AppenderRef(alias = alias, ref = appenderRef["ref"]?.toString(), filter = appenderRef.filters())
            }

    private fun Map<String, Any>.filters() =
            (this["filter"] as? Map<String, Map<String, Any>>)?.mapMutable { (alias, props) ->
                Filter(alias = alias,
                       type = props["type"]?.toString(),
                       onMismatch = props["onMismatch"]?.toString()?.asEnum<FilterDecision>(),
                       onMatch = props["onMatch"]?.toString()?.asEnum<FilterDecision>(),
                       extra = props.without("type", "onMismatch", "onMatch"))
            }

    override fun write(config: Config, writer: Writer) = writer.writeHierarchicalMap(map = config.toMap())

    fun Config.toMap() = mapOf("advertiser" to advertiser,
                               "dest" to dest,
                               "monitorInterval" to monitorIntervalSeconds,
                               "name" to name,
                               "packages" to packages?.joinToString(),
                               "schema" to schemaResource,
                               "shutdownHook" to isShutdownHookEnabled,
                               "status" to status,
                               "strict" to strict,
                               "shutdownTimeout" to shutdownTimeoutMillis,
                               "verbose" to verbose,
                               "appenders" to appenders?.map { it.alias },
                               "loggers" to loggers?.Logger?.map { it.alias },
                               "property" to properties.orEmpty().associate { it.name to it.value },
                               "script" to scripts,
                               "customLevel" to customLevels,
                               "filter" to filter.filters(),
                               "appender" to appenders?.associate {
                                   it.alias to mapOf("type" to it.type, "name" to it.name, "layout" to it.Layout?.let {
                                       mapOf("type" to it.type) + it.extra.orEmpty()
                                   }, "filter" to it.filters.filters()) + it.extra.orEmpty()
                               },
                               "logger" to loggers?.Logger?.associate {
                                   it.alias to mapOf("name" to it.name,
                                                     "level" to it.level,
                                                     "additivity" to it.additivity,
                                                     "filter" to it.filter.filters(),
                                                     "appenderRef" to it.AppenderRef.appenderRefs()) + it.extra.orEmpty()
                               },
                               "rootLogger" to loggers?.Root?.let {
                                   mapOf("level" to it.level,
                                         "filter" to it.filter.filters(),
                                         "appenderRef" to it.appenderRef.appenderRefs()) + it.extra.orEmpty()
                               })

    private fun Iterable<AppenderRef>?.appenderRefs() = this?.associate {
        it.alias to mapOf("ref" to it.ref, "filter" to it.filter.filters())
    }

    private fun Iterable<Filter>?.filters() = this?.associate {
        it.alias to mapOf("type" to it.type,
                          "onMismatch" to it.onMismatch,
                          "onMatch" to it.onMatch) + it.extra.orEmpty()
    }
}
