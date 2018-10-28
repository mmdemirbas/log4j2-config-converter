package com.mmdemirbas.log4j2.configconverter

import java.io.Reader
import java.io.Writer

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

    private fun Config.toMap() = mapOf("advertiser" to advertiser,
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
                                           it.alias to mapOf("type" to it.type,
                                                             "name" to it.name,
                                                             "layout" to it.Layout?.let {
                                                                 mapOf("type" to it.type) + it.extra.orEmpty()
                                                             },
                                                             "filter" to it.filters.filters()) + it.extra.orEmpty()
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

    private inline fun <K, V, R> Map<out K, V>.mapMutable(transform: (Map.Entry<K, V>) -> R) =
            map(transform).toMutableList()

    private infix fun <K, V> Map<K, V>.without(key: K) = without(listOf(key))

    private fun <K, V> Map<K, V>.without(vararg keys: K) = without(keys.asList())

    private infix fun <K, V> Map<K, V>.without(keys: List<K>): MutableMap<K, V>? {
        val map = (this - keys).toMutableMap()
        return if (map.isEmpty()) null else map
    }
}