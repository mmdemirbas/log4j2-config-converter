package com.mmdemirbas.log4j2.configconverter.deprecated

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.mmdemirbas.log4j2.configconverter.Appender
import com.mmdemirbas.log4j2.configconverter.AppenderRef
import com.mmdemirbas.log4j2.configconverter.Config
import com.mmdemirbas.log4j2.configconverter.CustomLevel
import com.mmdemirbas.log4j2.configconverter.DynamicObject
import com.mmdemirbas.log4j2.configconverter.Filter
import com.mmdemirbas.log4j2.configconverter.FilterDecision
import com.mmdemirbas.log4j2.configconverter.Format
import com.mmdemirbas.log4j2.configconverter.Layout
import com.mmdemirbas.log4j2.configconverter.Level
import com.mmdemirbas.log4j2.configconverter.Logger
import com.mmdemirbas.log4j2.configconverter.Loggers
import com.mmdemirbas.log4j2.configconverter.Property
import com.mmdemirbas.log4j2.configconverter.RootLogger
import com.mmdemirbas.log4j2.configconverter.Script
import com.mmdemirbas.log4j2.configconverter.associateMutable
import com.mmdemirbas.log4j2.configconverter.lateinitDynamicObject
import com.mmdemirbas.log4j2.configconverter.lateinitList
import com.mmdemirbas.log4j2.configconverter.mapMutable
import com.mmdemirbas.log4j2.configconverter.toDynamicObject
import org.apache.logging.log4j.core.jackson.Log4jJsonObjectMapper
import java.io.Reader
import java.io.Writer


object DeprecatedJson : Format() {
    private val mapper = Log4jJsonObjectMapper().configure(unwrapRootValue = true)
    private val reader = mapper.readerFor(MyConfig::class.java)
    private val writer = mapper.writer()

    override fun read(reader: Reader) = DeprecatedJson.reader.readValue<MyConfig>(reader).toCommon()
    override fun write(config: Config, writer: Writer) = DeprecatedJson.writer.writeValue(writer, config.toMine())

    @JsonRootName("configuration")
    data class MyConfig(var status: Level? = null,
                        var strict: Boolean? = null,
                        var dest: String? = null,
                        var shutdownHook: String? = null,
                        var shutdownTimeout: Long? = null,
                        var verbose: String? = null,
                        var packages: MutableList<String?>? = null,
                        var name: String? = null,
                        var monitorInterval: Int? = null,
                        var properties: MyProperties? = null,
                        var script: MutableList<Script>? = null,
                        var customLevel: MutableList<CustomLevel>? = null,
                        var filter: MutableList<MyFilter>? = null,
                        var appenders: MyAppenders? = null,
                        var loggers: MyLoggers? = null,
                        @JsonIgnore
                        var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = extra?.toMap()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            when {
                name.endsWith("Filter") -> lateinitList(::filter).add(MyFilter(type = name,
                                                                               onMismatch = value.asFilterDecision("onMismatch"),
                                                                               onMatch = value.asFilterDecision("onMatch"),
                                                                               extra = value))
                else                    -> lateinitDynamicObject(::extra)[name] = value
            }
        }
    }

    data class MyProperties(var property: MutableList<Property>? = null)
    data class MyAppenders(var appender: MutableList<MyAppender>? = null)

    data class MyFilter(var type: String? = null,
                        var onMismatch: FilterDecision? = null,
                        var onMatch: FilterDecision? = null,
                        @JsonIgnore
                        var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = extra?.toMap()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            lateinitDynamicObject(::extra)[name] = value
        }
    }

    data class MyAppender(var type: String? = null,
                          var name: String? = null,
                          var layout: MyLayout? = null,
                          var filters: MutableList<MyFilter>? = null,
                          @JsonIgnore
                          var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = extra?.toMap()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            when {name.endsWith("Filter") -> lateinitList(::filters).add(MyFilter(type = name,
                                                                                  onMismatch = value.asFilterDecision("onMismatch"),
                                                                                  onMatch = value.asFilterDecision("onMatch"),
                                                                                  extra = value))
                name.endsWith("Layout")   -> layout = MyLayout(type = name, extra = value)
                else                      -> lateinitDynamicObject(::extra)[name] = value
            }
        }
    }

    data class MyLoggers(var logger: MutableList<MyLogger>? = null, var root: MyRootLogger? = null)

    data class MyLayout(var type: String? = null,
                        @JsonIgnore
                        var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = extra?.toMap()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            lateinitDynamicObject(::extra)[name] = value
        }
    }

    data class MyLogger(var name: String? = null,
                        var level: Level? = null,
                        var additivity: Boolean? = null,
                        var filter: MutableList<MyFilter>? = null,
                        var appenderRef: MutableList<AppenderRef>? = null,
                        @JsonIgnore
                        var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = extra?.toMap()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            when {
                name.endsWith("Filter") -> lateinitList(::filter).add(MyFilter(type = name,
                                                                               onMismatch = value.asFilterDecision("onMismatch"),
                                                                               onMatch = value.asFilterDecision("onMatch"),
                                                                               extra = value))
                else                    -> lateinitDynamicObject(::extra)[name] = value
            }
        }
    }

    data class MyRootLogger(var level: Level? = null,
                            var filters: MutableList<MyFilter>? = null,
                            var appenderRef: MutableList<AppenderRef>? = null,
                            @JsonIgnore
                            var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = extra?.toMap()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            when {
                name.endsWith("Filter") -> lateinitList(::filters).add(MyFilter(type = name,
                                                                                onMismatch = value.asFilterDecision("onMismatch"),
                                                                                onMatch = value.asFilterDecision("onMatch"),
                                                                                extra = value))
                else                    -> lateinitDynamicObject(::extra)[name] = value
            }
        }
    }

    fun MyConfig.toCommon() = Config(status = status,
                                     strict = strict,
                                     dest = dest,
                                     isShutdownHookEnabled = shutdownHook?.toBoolean(),
                                     shutdownTimeoutMillis = shutdownTimeout,
                                     verbose = verbose,
                                     packages = packages,
                                     name = name,
                                     monitorIntervalSeconds = monitorInterval,
                                     properties = properties?.property,
                                     scripts = script,
                                     customLevels = customLevel,
                                     filter = filter?.mapMutable {
                                         Filter(type = it.type,
                                                onMismatch = it.onMismatch,
                                                onMatch = it.onMatch,
                                                extra = it.extra?.toMap())
                                     },
                                     appenders = appenders?.appender?.mapMutable {
                                         Appender(type = it.type, name = it.name, Layout = it.layout?.let { layout ->
                                             Layout(type = layout.type, extra = layout.extra?.toMap())
                                         }, filters = it.filters?.mapMutable {
                                             Filter(type = it.type,
                                                    onMismatch = it.onMismatch,
                                                    onMatch = it.onMatch,
                                                    extra = it.extra?.toMap())
                                         }, extra = it.extra?.toMap())
                                     },
                                     loggers = Loggers(Logger = loggers?.logger?.mapMutable {
                                         Logger(name = it.name,
                                                level = it.level,
                                                additivity = it.additivity,
                                                filter = it.filter?.mapMutable {
                                                    Filter(type = it.type,
                                                           onMismatch = it.onMismatch,
                                                           onMatch = it.onMatch,
                                                           extra = it.extra?.toMap())
                                                },
                                                AppenderRef = it.appenderRef)
                                     },
                                                       Root = RootLogger(level = loggers?.root?.level,
                                                                         filter = loggers?.root?.filters?.mapMutable {
                                                                             Filter(type = it.type,
                                                                                    onMismatch = it.onMismatch,
                                                                                    onMatch = it.onMatch,
                                                                                    extra = it.extra?.toMap())
                                                                         },
                                                                         appenderRef = loggers?.root?.appenderRef,
                                                                         extra = loggers?.root?.extra?.toMap())),
                                     extra = extra?.toMap())

    fun Config.toMine() = MyConfig(status = status,
                                   strict = strict,
                                   dest = dest,
                                   shutdownHook = isShutdownHookEnabled?.toString(),
                                   shutdownTimeout = shutdownTimeoutMillis,
                                   verbose = verbose,
                                   packages = packages,
                                   name = name,
                                   monitorInterval = monitorIntervalSeconds,
                                   properties = MyProperties(properties),
                                   script = scripts,
                                   customLevel = customLevels,
                                   filter = filter?.mapMutable {
                                       MyFilter(type = it.type,
                                                onMismatch = it.onMismatch,
                                                onMatch = it.onMatch,
                                                extra = it.extra.toDynamicObject())
                                   },
                                   appenders = MyAppenders(appenders?.mapMutable {
                                       MyAppender(type = it.type,
                                                  name = it.name,
                                                  layout = MyLayout(type = it?.Layout?.type,
                                                                    extra = it.Layout?.extra?.toDynamicObject()),
                                                  filters = it.filters?.mapMutable {
                                                      MyFilter(type = it.type,
                                                               onMismatch = it.onMismatch,
                                                               onMatch = it.onMatch,
                                                               extra = it.extra.toDynamicObject())
                                                  },
                                                  extra = it.extra.toDynamicObject())
                                   }),
                                   loggers = MyLoggers(loggers?.Logger?.mapMutable {
                                       MyLogger(name = it.name,
                                                level = it.level,
                                                additivity = it.additivity,
                                                filter = it.filter?.mapMutable {
                                                    MyFilter(type = it.type,
                                                             onMismatch = it.onMismatch,
                                                             onMatch = it.onMatch,
                                                             extra = it.extra.toDynamicObject())
                                                },
                                                appenderRef = it.AppenderRef,
                                                extra = it.extra.toDynamicObject())
                                   },
                                                       root = MyRootLogger(level = loggers?.Root?.level,
                                                                           filters = loggers?.Root?.filter?.mapMutable {
                                                                               MyFilter(type = it.type,
                                                                                        onMismatch = it.onMismatch,
                                                                                        onMatch = it.onMatch,
                                                                                        extra = it.extra.toDynamicObject())
                                                                           },
                                                                           appenderRef = loggers?.Root?.appenderRef,
                                                                           extra = loggers?.Root?.extra.toDynamicObject())),
                                   extra = extra.toDynamicObject())

}


internal fun <T : ObjectMapper> T.configure(unwrapRootValue: Boolean): T {
    configure(DeserializationFeature.UNWRAP_ROOT_VALUE, unwrapRootValue)
    enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
    enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    enable(SerializationFeature.INDENT_OUTPUT)
    return this
}

@JsonAnyGetter
fun DynamicObject.toMap(): MutableMap<String, Any?>? {
    val valueMap: MutableMap<String, Any?> = mutableMapOf("value" to value)
    val extraMap = extra?.entries?.associateMutable { (key, objects) ->
        val maps = objects.map { it?.toMap() }
        key to when (maps.size) {
            0    -> ""
            1    -> {
                val singleMap = maps.single()
                when (singleMap?.size) {
                    0    -> ""
                    1    -> {
                        val (singleKey, singleValue) = singleMap.entries.single()
                        when {
                            singleKey == "value" && singleValue is String -> singleValue
                            else                                          -> singleMap
                        }
                    }
                    else -> singleMap
                }
            }
            else -> maps
        }
    }
    return when {
        value.isEmpty()          -> extraMap
        extra?.isEmpty() == true -> valueMap
        else                     -> (extraMap.orEmpty() + valueMap).toMutableMap()
    }
}
