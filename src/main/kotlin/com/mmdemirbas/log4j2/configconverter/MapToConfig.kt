package com.mmdemirbas.log4j2.configconverter

/**
 * @author Muhammed Demirbaş
 * @since 2018-10-28 17:25
 */


// todo: declared type immutable iken default value'lar mutable tanımlansa da bunun bir kullanım alanı var mı? Belki java tarafına geçince anlamlı oluyordur.


// todo: out-of-the-box layout'lar karşılık gelen sınıflar tek tek yazılarak support edilebilir => buna gerek kalmayabilir extra map sayesinde

// todo: support Script, ScriptFile and ScriptRef separately => genel olarak Log4J'nin plugin'lere gerenic yaklaşımı gibi yaklaşılabilir. Sadece type özel olarak ele alınıp diğer herşey extra gibi değerlendirilebilir.
// belki extra'larda da top-level item'lar type içerebilir diye düşünülse iyi olabilir

// todo: copy other samples from log4j website & repos for testing


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

private fun Map<String, Any>.toProperties(): MutableList<Property>? {
    return list("Properties")?.flatMapMutable { x ->
        (x as Map<String, Any>).toProperty()
    }
}

private fun Map<String, Any>.toProperty(): List<Property> {
    return asCaseInsensitiveMap().list("property")!!.map { propertyMap ->
        propertyMap as Map<String, Any>
        Property(name = propertyMap.string("name"), value = propertyMap.string("value"))
    }
}

private fun Map<String, Any>.toAppenders(): MutableList<Appender>? {
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

private fun Map<String, Any>.toAppender(alias: String?): Appender {
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

private fun Map<String, Any>.toLayout(): Layout? {
    val layoutMap = map("Layout")?.let { mapOf("Layout" to it) } ?: keys.filter { it.endsWith("Layout") }.map { key ->
        mapOf(key to map(key))
    }.firstOrNull()
    return layoutMap?.let {
        val (alias, layout) = layoutMap.entries.first()
        Layout(type = layout.effectiveType(alias, "Layout"),
               extra = layout.without(fullMatches = listOf("type", "Layout"), suffices = listOf("Layout")))
    }
}

private fun Map<String, Any>.toLoggers(): Loggers {
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

private fun Map<String, Any>.toLogger(alias: String): Logger {
    return Logger(alias = effectiveType(explicit("alias", alias)!!, "Logger"),
                  name = string("name"),
                  level = enum<Level>("level"),
                  additivity = bool("additivity"),
                  filter = toFilters(),
                  AppenderRef = toAppenderRefs(),
                  extra = without(fullMatches = listOf("name", "alias", "level", "additivity", "AppenderRef"),
                                  suffices = listOf("Filter")))
}

private fun Map<String, Any>.toRootLogger(): RootLogger {
    val root = map("Root") ?: this
    return RootLogger(level = root.enum<Level>("level"),
                      filter = root.toFilters(),
                      appenderRef = root.toAppenderRefs(),
                      extra = root.without(fullMatches = listOf("level", "AppenderRef"), suffices = listOf("Filter")))
}

private fun Map<String, Any>?.toAppenderRefs() = map("AppenderRef")?.let { appenderRef ->
    mutableListOf(AppenderRef(alias = appenderRef.string("alias"),
                              ref = appenderRef.string("ref"),
                              filter = appenderRef.toFilters()))
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
            is Map<*, *> -> (props as Map<String, Any>).toFilter(alias)
            else         -> TODO("type: ${props.javaClass.name}  toString: $props")
        }
    }.toMutableListOrNull()
}

private fun Map<String, Any>.toFilter(alias: String): Filter {
    val effectiveAlias = explicit("alias", alias)
    val effectiveType = explicit("type", alias)
    return Filter(alias = if (effectiveAlias.equals(effectiveType, ignoreCase = true) || effectiveAlias.equals("filter",
                                                                                                               ignoreCase = true)) null else effectiveAlias,
                  type = effectiveType,
                  onMismatch = enum<FilterDecision>("onMismatch"),
                  onMatch = enum<FilterDecision>("onMatch"),
                  extra = without(fullMatches = listOf("type", "alias", "onMismatch", "onMatch")))
}

private fun Map<String, Any>?.effectiveType(suggested: String, unacceptable: String): String? {
    val type = string("type")
    return when {
        type?.isNotEmpty() == true                        -> type
        suggested.equals(unacceptable, ignoreCase = true) -> type
        else                                              -> suggested
    }
}

private fun List<Filter>?.toMutableListOrNull() = if (this?.isEmpty() == false) toMutableList() else null

private inline fun <T, R> Iterable<T>.flatMapMutable(transform: (T) -> Iterable<R>) = flatMap(transform).toMutableList()

private inline fun <T, R> Iterable<T>.mapMutable(transform: (T) -> R) = map(transform).toMutableList()
