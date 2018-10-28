package com.mmdemirbas.log4j2.configconverter.deprecated

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
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
import com.mmdemirbas.log4j2.configconverter.lateinitMap
import com.mmdemirbas.log4j2.configconverter.mapMutable
import com.mmdemirbas.log4j2.configconverter.toDynamicObject
import com.mmdemirbas.log4j2.configconverter.valuesList
import org.apache.logging.log4j.core.jackson.Log4jYamlObjectMapper
import java.io.Reader
import java.io.Writer

object DeprecatedYaml : Format() {
    private val reader = Log4jYamlObjectMapper().configure(unwrapRootValue = true).readerFor(MyConfig::class.java)
    private val writer =
            Log4jYamlObjectMapper().configure(unwrapRootValue = false).enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).writer()

    override fun read(reader: Reader) = DeprecatedYaml.reader.readValue<MyConfig>(reader).toCommon()

    override fun write(config: Config, writer: Writer) =
            DeprecatedYaml.writer.writeValue(writer, mapOf("Configuration" to config.toMine()))

    @JsonRootName("Configuration")
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
                        var script: MutableMap<String?, Script>? = null,
                        var customLevel: MutableMap<String?, CustomLevel>? = null,
                        @JsonIgnore
                        var filter: MutableMap<String?, MyFilter>? = null,
                        var appenders: MutableMap<String?, MyAppender>? = null,
                        var loggers: MyLoggers? = null,
                        @JsonIgnore
                        var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = filter.orEmpty() + extra?.toMap().orEmpty()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            when {
                name.endsWith("Filter") -> lateinitMap(::filter)[name] =
                        MyFilter(type = null,
                                 onMismatch = value.asFilterDecision("onMismatch"),
                                 onMatch = value.asFilterDecision("onMatch"),
                                 extra = value)
                else                    -> lateinitDynamicObject(::extra)[name] = value
            }
        }
    }

    data class MyProperties(var property: MutableList<Property>? = null)

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

    @JsonRootName("Appender")
    data class MyAppender(var type: String? = null, var name: String? = null,
                          @JsonIgnore
                          var layout: MutableMap<String?, MyLayout>? = null,
                          @JsonIgnore
                          var filters: MutableMap<String?, MyFilter>? = null,
                          @JsonIgnore
                          var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = layout.orEmpty() + filters.orEmpty() + extra?.toMap().orEmpty()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            when {name.endsWith("Filter") -> lateinitMap(::filters)[name] =
                    MyFilter(type = name,
                             onMismatch = value.asFilterDecision("onMismatch"),
                             onMatch = value.asFilterDecision("onMatch"),
                             extra = value)
                name.endsWith("Layout")   -> lateinitMap(::layout)[name] = MyLayout(type = name, extra = value)
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
                        @JsonIgnore
                        var filter: MutableMap<String?, MyFilter>? = null,
                        var AppenderRef: MutableList<AppenderRef>? = null,
                        @JsonIgnore
                        var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = filter.orEmpty() + extra?.toMap().orEmpty()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            when {
                name.endsWith("Filter") -> lateinitMap(::filter)[name] =
                        MyFilter(type = null,
                                 onMismatch = value.asFilterDecision("onMismatch"),
                                 onMatch = value.asFilterDecision("onMatch"),
                                 extra = value)
                else                    -> lateinitDynamicObject(::extra)[name] = value
            }
        }
    }

    data class MyRootLogger(var level: Level? = null,
                            @JsonIgnore
                            var filters: MutableMap<String?, MyFilter>? = null,
                            var AppenderRef: MutableList<AppenderRef>? = null,
                            @JsonIgnore
                            var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = filters.orEmpty() + extra?.toMap().orEmpty()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            when {
                name.endsWith("Filter") -> lateinitMap(::filters)[name] =
                        MyFilter(type = null,
                                 onMismatch = value.asFilterDecision("onMismatch"),
                                 onMatch = value.asFilterDecision("onMatch"),
                                 extra = value)
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
                                     scripts = script?.valuesList,
                                     customLevels = customLevel?.valuesList,
                                     filter = filter?.entries?.mapMutable { (type, filter) ->
                                         Filter(type = type,
                                                onMismatch = filter.onMismatch,
                                                onMatch = filter.onMatch,
                                                extra = filter.extra?.toMap())
                                     },
                                     appenders = appenders?.mapMutable { (type, appender) ->
                                         Appender(type = type,
                                                  name = appender.name,
                                                  Layout = appender.layout?.entries?.singleOrNull()?.let { (type, layout) ->
                                                      Layout(type = type, extra = layout.extra?.toMap())
                                                  },
                                                  filters = appender.filters?.mapMutable { (type, filter) ->
                                                      Filter(type = type,
                                                             onMismatch = filter.onMismatch,
                                                             onMatch = filter.onMatch,
                                                             extra = filter.extra?.toMap())
                                                  },
                                                  extra = appender.extra?.toMap())
                                     },
                                     loggers = Loggers(Logger = loggers?.logger?.mapMutable {
                                         Logger(name = it.name,
                                                level = it.level,
                                                additivity = it.additivity,
                                                filter = it.filter?.mapMutable { (type, filter) ->
                                                    Filter(type = type,
                                                           onMismatch = filter.onMismatch,
                                                           onMatch = filter.onMatch,
                                                           extra = filter.extra?.toMap())
                                                },
                                                AppenderRef = it.AppenderRef)
                                     },
                                                       Root = RootLogger(level = loggers?.root?.level,
                                                                         filter = loggers?.root?.filters?.mapMutable { (type, filter) ->
                                                                             Filter(type = type,
                                                                                    onMismatch = filter.onMismatch,
                                                                                    onMatch = filter.onMatch,
                                                                                    extra = filter.extra?.toMap())
                                                                         },
                                                                         appenderRef = loggers?.root?.AppenderRef,
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
                                   script = scripts?.associateMutable { it.name to it },
                                   customLevel = customLevels?.associateMutable { it.name to it },
                                   filter = filter?.associateMutable { filter ->
                                       filter.type to MyFilter(type = null,
                                                               onMismatch = filter.onMismatch,
                                                               onMatch = filter.onMatch,
                                                               extra = filter.extra.toDynamicObject())
                                   },
                                   appenders = appenders?.associateMutable {
                                       it.type to MyAppender(type = null,
                                                             name = it.name,
                                                             layout = mutableMapOf(it.Layout?.type to MyLayout(type = null,
                                                                                                               extra = it.Layout?.extra.toDynamicObject())),
                                                             filters = it.filters?.associateMutable {
                                                                 it.type to MyFilter(type = it.type,
                                                                                     onMismatch = it.onMismatch,
                                                                                     onMatch = it.onMatch,
                                                                                     extra = it.extra.toDynamicObject())
                                                             },
                                                             extra = it.extra.toDynamicObject())
                                   },
                                   loggers = MyLoggers(loggers?.Logger?.mapMutable {
                                       MyLogger(name = it.name,
                                                level = it.level,
                                                additivity = it.additivity,
                                                filter = it.filter?.associateMutable {
                                                    it.type to MyFilter(type = null,
                                                                        onMismatch = it.onMismatch,
                                                                        onMatch = it.onMatch,
                                                                        extra = it.extra.toDynamicObject())
                                                },
                                                AppenderRef = it.AppenderRef,
                                                extra = it.extra.toDynamicObject())
                                   },
                                                       root = MyRootLogger(level = loggers?.Root?.level,
                                                                           filters = loggers?.Root?.filter?.associateMutable { filter ->
                                                                               filter.type to MyFilter(type = null,
                                                                                                       onMismatch = filter.onMismatch,
                                                                                                       onMatch = filter.onMatch,
                                                                                                       extra = filter.extra.toDynamicObject())
                                                                           },
                                                                           AppenderRef = loggers?.Root?.appenderRef,
                                                                           extra = loggers?.Root?.extra.toDynamicObject())),
                                   extra = extra.toDynamicObject())

}