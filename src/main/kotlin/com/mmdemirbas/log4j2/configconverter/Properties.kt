package com.mmdemirbas.log4j2.configconverter

import java.io.Reader
import java.io.Writer

object Properties : Format() {
    // todo: support comments and empty lines when loading

    override fun load(reader: Reader): Config {
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
                          val type = appender["type"]?.toString()
                          Appender(alias = if (alias.equals(type)) null else alias,
                                   type = type,
                                   name = appender["name"]?.toString(),
                                   Layout = (appender["layout"] as? Map<String, Any>)?.let { layoutMap ->
                                       Layout(type = layoutMap["type"]?.toString(), extra = layoutMap.without("type"))
                                   },
                                   filters = appender.filters(),
                                   extra = appender.without("type", "name", "layout", "filter"))
                      },
                      loggers = Loggers(Logger = (config["logger"] as? Map<String, Map<String, Any>>)?.mapMutable { (alias, logger) ->
                          val name = logger["name"]?.toString()
                          Logger(alias = if (alias.equals(name)) null else alias,
                                 name = name,
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
                val ref = appenderRef["ref"]?.toString()
                AppenderRef(alias = if (alias.equals(ref)) null else alias, ref = ref, filter = appenderRef.filters())
            }

    private fun Map<String, Any>.filters() =
            (this["filter"] as? Map<String, Map<String, Any>>)?.mapMutable { (alias, props) ->
                val type = props["type"]?.toString()
                Filter(alias = if (alias.equals(type)) null else alias,
                       type = type,
                       onMismatch = props["onMismatch"]?.toString()?.asEnum<FilterDecision>(),
                       onMatch = props["onMatch"]?.toString()?.asEnum<FilterDecision>(),
                       extra = props.without("type", "onMismatch", "onMatch"))
            }

    private inline fun <K, V, R> Map<out K, V>.mapMutable(transform: (Map.Entry<K, V>) -> R) =
            map(transform).toMutableList()

    private fun <K, V> Map<K, V>.without(vararg keys: K): MutableMap<K, V>? {
        val map = (this - keys.asList()).toMutableMap()
        return if (map.isEmpty()) null else map
    }


    override fun save(config: Config, writer: Writer) =
            writer.writeHierarchicalMap(map = mapOf("advertiser" to config.advertiser,
                                                    "dest" to config.dest,
                                                    "monitorInterval" to config.monitorIntervalSeconds,
                                                    "name" to config.name,
                                                    "packages" to config.packages?.joinToString(),
                                                    "schema" to config.schemaResource,
                                                    "shutdownHook" to config.isShutdownHookEnabled,
                                                    "status" to config.status,
                                                    "strict" to config.strict,
                                                    "shutdownTimeout" to config.shutdownTimeoutMillis,
                                                    "verbose" to config.verbose,
                                                    "appenders" to config.appenders?.map { it.alias ?: it.type },
                                                    "loggers" to config.loggers?.Logger?.map { it.alias ?: it.name },
                                                    "property" to config.properties.orEmpty().associate { it.name to it.value },
                                                    "script" to config.scripts,
                                                    "customLevel" to config.customLevels,
                                                    "filter" to config.filter.filters(),
                                                    "appender" to config.appenders?.associate {
                                                        (it.alias ?: it.type) to mapOf("type" to it.type,
                                                                                       "name" to it.name,
                                                                                       "layout" to it.Layout?.let {
                                                                                           mapOf("type" to it.type) + it.extra.orEmpty()
                                                                                       },
                                                                                       "filter" to it.filters.filters()) + it.extra.orEmpty()
                                                    },
                                                    "logger" to config.loggers?.Logger?.associate {
                                                        (it.alias ?: it.name) to mapOf("name" to it.name,
                                                                                       "level" to it.level,
                                                                                       "additivity" to it.additivity,
                                                                                       "filter" to it.filter.filters(),
                                                                                       "appenderRef" to it.AppenderRef.appenderRefs()) + it.extra.orEmpty()
                                                    },
                                                    "rootLogger" to config.loggers?.Root?.let {
                                                        mapOf("level" to it.level,
                                                              "filter" to it.filter.filters(),
                                                              "appenderRef" to it.appenderRef.appenderRefs()) + it.extra.orEmpty()
                                                    }))

    private fun Iterable<AppenderRef>?.appenderRefs() = this?.associate {
        (it.alias ?: it.ref) to mapOf("ref" to it.ref, "filter" to it.filter.filters())
    }

    private fun Iterable<Filter>?.filters() = this?.associate {
        (it.alias ?: it.type) to mapOf("type" to it.type,
                                       "onMismatch" to it.onMismatch,
                                       "onMatch" to it.onMatch) + it.extra.orEmpty()
    }
}