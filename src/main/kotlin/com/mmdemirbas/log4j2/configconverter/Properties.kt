package com.mmdemirbas.log4j2.configconverter

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import java.io.InputStream
import java.io.OutputStream

object Properties : Format() {
    private val mapper = JavaPropsMapper().configure2()
    private val reader = mapper.reader(Configuration2::class.java)
    private val writer = mapper.writer()

    override fun read(stream: InputStream) = reader.readValue<Configuration2>(stream).to1()
    override fun write(obj: Configuration, stream: OutputStream) = writer.writeValue(stream, obj.to2())


    data class Configuration2(var status: Level = Level.ERROR,
                              var dest: String = "",
                              var shutdownHook: String = "",
                              var shutdownTimeout: Long = 0,
                              var verbose: String = "",
                              var packages: List<String> = mutableListOf(),
                              var name: String = "",
                              var monitorInterval: Int = 0,
                              var property: Map<String, String> = mutableMapOf(),
                              var script: Map<String, Script> = mutableMapOf(),
                              var customLevel: Map<String, CustomLevel> = mutableMapOf(),
                              var filter: Map<String, Filter2> = mutableMapOf(),
                              var appender: Map<String, Appender2> = mutableMapOf(),
                              var logger: Map<String, Logger2> = mutableMapOf(),
                              var rootLogger: RootLogger2 = RootLogger2(),
                              var extra: DynamicObject = DynamicObject()) {
        @JsonAnyGetter
        fun getAny() = extra.getAny()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) = extra.setAny(name, value)
    }

    data class Filter2(var type: String = "",
                       var onMismatch: FilterDecision = FilterDecision.NEUTRAL,
                       var onMatch: FilterDecision = FilterDecision.NEUTRAL,
                       var extra: DynamicObject = DynamicObject()) {
        @JsonAnyGetter
        fun getAny() = extra.getAny()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) = extra.setAny(name, value)
    }

    data class Appender2(var type: String = "",
                         var name: String = "",
                         var layout: Layout2 = Layout2(),
                         var filter: Map<String, Filter2> = mutableMapOf(),
                         var extra: DynamicObject = DynamicObject()) {
        @JsonAnyGetter
        fun getAny() = extra.getAny()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) = extra.setAny(name, value)
    }

    data class Layout2(var type: String = "", var extra: DynamicObject = DynamicObject()) {
        @JsonAnyGetter
        fun getAny() = extra.getAny()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) = extra.setAny(name, value)
    }

    data class Logger2(var name: String = "",
                       var level: Level = Level.ERROR,
                       var additivity: Boolean = false,
                       var filters: Map<String, Filter2> = mutableMapOf(),
                       var appenderRef: Map<String, AppenderRef> = mutableMapOf(),
                       var extra: DynamicObject = DynamicObject()) {
        @JsonAnyGetter
        fun getAny() = extra.getAny()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) = extra.setAny(name, value)
    }

    data class RootLogger2(var level: Level = Level.ERROR,
                           var filters: Map<String, Filter2> = mutableMapOf(),
                           var appenderRef: Map<String, AppenderRef> = mutableMapOf(),
                           var extra: DynamicObject = DynamicObject()) {
        @JsonAnyGetter
        fun getAny() = extra.getAny()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) = extra.setAny(name, value)
    }


    fun Map<String, Any>.to2New() = DynamicObject(extra = entries.associate {
        val value = it.value
        it.key to when (value) {
            is String -> DynamicObject(value)
            else      -> TODO()
        }
    }.toMutableMap())

    fun DynamicObject.to1New(): MutableMap<String, Any> = extra.entries.associate {
        it.key to when {
            it.value.extra.isNotEmpty() -> it.value.to1New()
            else                        -> it.value.value
        }
    }.toMutableMap()

    // todo: testler geçtikten sonra bunun definition'ını kısaltmaya çalış. @JvmOverloads at falan...
    data class DynamicObject @JvmOverloads constructor(val value: String = "",
                                                       var extra: MutableMap<String, DynamicObject> = mutableMapOf()) {
        @JsonAnyGetter
        fun getAny() = extra

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) = extra.set(name, value)

        override fun toString() = when {
            extra.isNotEmpty() -> extra.toString()
            else               -> value
        }
    }


    // todo: burada çok fazla common kısım var. onları refactor et
    fun Configuration2.to1() = Configuration(status = status,
                                             dest = dest,
                                             shutdownHook = shutdownHook,
                                             shutdownTimeout = shutdownTimeout,
                                             verbose = verbose,
                                             packages = packages,
                                             name = name,
                                             monitorInterval = monitorInterval,
                                             properties = property.map { Property(it.key, it.value) },
                                             scripts = script.values.toList(),
                                             customLevels = customLevel.values.toList(),
                                             filter = filter.values.map {
                                                 Filter(type = it.type,
                                                        onMismatch = it.onMismatch,
                                                        onMatch = it.onMatch,
                                                        extra = it.extra.to1New())
                                             },
                                             appenders = appender.values.map {
                                                 Appender(type = it.type,
                                                          name = it.name,
                                                          layout = Layout(type = it.layout.type,
                                                                          extra = it.layout.extra.to1New()),
                                                          filters = it.filter.values.map {
                                                              Filter(type = it.type,
                                                                     onMismatch = it.onMismatch,
                                                                     onMatch = it.onMatch,
                                                                     extra = it.extra.to1New())
                                                          },
                                                          extra = it.extra.to1New())
                                             },
                                             loggers = Loggers(logger = logger.values.map {
                                                 Logger(name = it.name,
                                                        level = it.level,
                                                        additivity = it.additivity,
                                                        filter = it.filters.values.map {
                                                            Filter(type = it.type,
                                                                   onMismatch = it.onMismatch,
                                                                   onMatch = it.onMatch,
                                                                   extra = it.extra.to1New())
                                                        },
                                                        appenderRef = it.appenderRef.values.toList())
                                             },
                                                               root = RootLogger(level = rootLogger.level,
                                                                                 filter = rootLogger.filters.values.map {
                                                                                     Filter(type = it.type,
                                                                                            onMismatch = it.onMismatch,
                                                                                            onMatch = it.onMatch,
                                                                                            extra = it.extra.to1New())
                                                                                 },
                                                                                 appenderRef = rootLogger.appenderRef.values.toList(),
                                                                                 extra = rootLogger.extra.to1New())),
                                             extra = extra.to1New())

    fun Configuration.to2() = Configuration2(status = status,
                                             dest = dest,
                                             shutdownHook = shutdownHook,
                                             shutdownTimeout = shutdownTimeout,
                                             verbose = verbose,
                                             packages = packages,
                                             name = name,
                                             monitorInterval = monitorInterval,
                                             property = properties.associate { it.name to it.value },
                                             script = scripts.associate { it.name to it },
                                             customLevel = customLevels.associate { it.name to it },
                                             filter = filter.associate {
                                                 it.type to Filter2(type = it.type,
                                                                    onMismatch = it.onMismatch,
                                                                    onMatch = it.onMatch,
                                                                    extra = it.extra.to2New())
                                             },
                                             appender = appenders.associate {
                                                 it.name to Appender2(type = it.type,
                                                                      name = it.name,
                                                                      layout = Layout2(type = it.layout.type,
                                                                                       extra = it.layout.extra.to2New()),
                                                                      filter = it.filters.associate {
                                                                          it.type to Filter2(type = it.type,
                                                                                             onMismatch = it.onMismatch,
                                                                                             onMatch = it.onMatch,
                                                                                             extra = it.extra.to2New())
                                                                      },
                                                                      extra = it.extra.to2New())
                                             },
                                             logger = loggers.logger.associate {
                                                 it.name to Logger2(name = it.name,
                                                                    level = it.level,
                                                                    additivity = it.additivity,
                                                                    filters = it.filter.associate {
                                                                        it.type to Filter2(type = it.type,
                                                                                           onMismatch = it.onMismatch,
                                                                                           onMatch = it.onMatch,
                                                                                           extra = it.extra.to2New())
                                                                    },
                                                                    appenderRef = it.appenderRef.associate { it.ref to it },
                                                                    extra = it.extra.to2New())
                                             },
                                             rootLogger = RootLogger2(level = loggers.root.level,
                                                                      filters = loggers.root.filter.associate {
                                                                          it.type to Filter2(type = it.type,
                                                                                             onMismatch = it.onMismatch,
                                                                                             onMatch = it.onMatch,
                                                                                             extra = it.extra.to2New())
                                                                      },
                                                                      appenderRef = loggers.root.appenderRef.associate { it.ref to it },
                                                                      extra = loggers.root.extra.to2New()),
                                             extra = extra.to2New())


    fun parseHierarchicalMap(lines: List<String>): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        lines.forEach { line ->
            if (line.isNotBlank()) {
                val path = line.substringBefore('=').split('.').map { it.trim() }
                val value = line.substringAfter('=', "").trim()
                var targetMap = map
                path.dropLast(1).forEachIndexed { index, part ->
                    targetMap = targetMap.getOrPut(part) { mutableMapOf<String, Any>() } as? MutableMap<String, Any> ?:
                            throw RuntimeException("'${path.subList(0,
                                                                    index + 1).joinToString(".")}' used as both leaf and non-leaf node")
                }
                val existingValue = targetMap.put(path.last(), value.trim())
                if (existingValue is Map<*, *>) throw RuntimeException("'${path.joinToString(".")}' used as both leaf and non-leaf node")
            }
        }
        return map
    }
}

