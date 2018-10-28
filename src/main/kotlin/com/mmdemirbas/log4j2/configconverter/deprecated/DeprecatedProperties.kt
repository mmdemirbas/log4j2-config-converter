package com.mmdemirbas.log4j2.configconverter.deprecated

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
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
import com.mmdemirbas.log4j2.configconverter.mapMutable
import com.mmdemirbas.log4j2.configconverter.toDynamicObject
import com.mmdemirbas.log4j2.configconverter.valuesList
import java.io.Reader
import java.io.Writer

object DeprecatedProperties : Format() {
    private val mapper = JavaPropsMapper().configure(unwrapRootValue = false)
    private val reader = mapper.readerFor(MyConfig::class.java)
    private val writer = mapper.writer()

    override fun read(reader: Reader) = DeprecatedProperties.reader.readValue<MyConfig>(reader).toCommon()
    override fun write(config: Config, writer: Writer) = DeprecatedProperties.writer.writeValue(writer, config.toMine())

    data class MyConfig(var status: Level? = null,
                        var dest: String? = null,
                        var shutdownHook: String? = null,
                        var shutdownTimeout: Long? = null,
                        var verbose: String? = null,
                        var packages: MutableList<String?>? = null,
                        var name: String? = null,
                        var monitorInterval: Int? = null,
                        var property: MutableMap<String?, String?>? = null,
                        var script: MutableMap<String?, Script>? = null,
                        var customLevel: MutableMap<String?, CustomLevel>? = null,
                        var filter: MutableMap<String?, MyFilter>? = null,
                        var appender: MutableMap<String?, MyAppender>? = null,
                        var logger: MutableMap<String?, MyLogger>? = null,
                        var rootLogger: MyRootLogger? = null,
                        @JsonIgnore
                        var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = extra?.toMap()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            lateinitDynamicObject(::extra)[name] = value
        }
    }

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
                          var filter: MutableMap<String?, MyFilter>? = null,
                          @JsonIgnore
                          var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = extra?.toMap()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            lateinitDynamicObject(::extra)[name] = value
        }
    }

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
                        var filters: MutableMap<String?, MyFilter>? = null,
                        var appenderRef: MutableMap<String?, AppenderRef>? = null,
                        @JsonIgnore
                        var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = extra?.toMap()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            lateinitDynamicObject(::extra)[name] = value
        }
    }

    data class MyRootLogger(var level: Level? = null,
                            var filters: MutableMap<String?, MyFilter>? = null,
                            var appenderRef: MutableMap<String?, AppenderRef>? = null,
                            @JsonIgnore
                            var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = extra?.toMap()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            lateinitDynamicObject(::extra)[name] = value
        }
    }

    fun MyConfig.toCommon() = Config(status = status,
                                     dest = dest,
                                     isShutdownHookEnabled = shutdownHook?.toBoolean(),
                                     shutdownTimeoutMillis = shutdownTimeout,
                                     verbose = verbose,
                                     packages = packages,
                                     name = name,
                                     monitorIntervalSeconds = monitorInterval,
                                     properties = property?.mapMutable {
                                         Property(it.key, it.value)
                                     },
                                     scripts = script?.valuesList,
                                     customLevels = customLevel?.valuesList,
                                     filter = filter?.values?.mapMutable {
                                         Filter(type = it.type,
                                                onMismatch = it.onMismatch,
                                                onMatch = it.onMatch,
                                                extra = it.extra?.toMap())
                                     },
                                     appenders = appender?.values?.mapMutable {
                                         Appender(type = it.type,
                                                  name = it.name,
                                                  Layout = Layout(type = it.layout?.type,
                                                                  extra = it.layout?.extra?.toMap()),
                                                  filters = it.filter?.values?.mapMutable {
                                                      Filter(type = it.type,
                                                             onMismatch = it.onMismatch,
                                                             onMatch = it.onMatch,
                                                             extra = it.extra?.toMap())
                                                  },
                                                  extra = it.extra?.toMap())
                                     },
                                     loggers = Loggers(Logger = logger?.values?.mapMutable {
                                         Logger(name = it.name,
                                                level = it.level,
                                                additivity = it.additivity,
                                                filter = it.filters?.values?.mapMutable {
                                                    Filter(type = it.type,
                                                           onMismatch = it.onMismatch,
                                                           onMatch = it.onMatch,
                                                           extra = it.extra?.toMap())
                                                },
                                                AppenderRef = it.appenderRef?.valuesList)
                                     },
                                                       Root = RootLogger(level = rootLogger?.level,
                                                                         filter = rootLogger?.filters?.values?.mapMutable {
                                                                             Filter(type = it.type,
                                                                                    onMismatch = it.onMismatch,
                                                                                    onMatch = it.onMatch,
                                                                                    extra = it.extra?.toMap())
                                                                         },
                                                                         appenderRef = rootLogger?.appenderRef?.valuesList,
                                                                         extra = rootLogger?.extra?.toMap())),
                                     extra = extra?.toMap())

    fun Config.toMine() = MyConfig(status = status,
                                   dest = dest,
                                   shutdownHook = isShutdownHookEnabled?.toString(),
                                   shutdownTimeout = shutdownTimeoutMillis,
                                   verbose = verbose,
                                   packages = packages,
                                   name = name,
                                   monitorInterval = monitorIntervalSeconds,
                                   property = properties?.associateMutable { it.name to it.value },
                                   script = scripts?.associateMutable { it.name to it },
                                   customLevel = customLevels?.associateMutable { it.name to it },
                                   filter = filter?.associateMutable {
                                       it.type to MyFilter(type = it.type,
                                                           onMismatch = it.onMismatch,
                                                           onMatch = it.onMatch,
                                                           extra = it.extra?.toDynamicObject())
                                   },
                                   appender = appenders?.associateMutable {
                                       it.name to MyAppender(type = it.type,
                                                             name = it.name,
                                                             layout = MyLayout(type = it.Layout?.type,
                                                                               extra = it.Layout?.extra.toDynamicObject()),
                                                             filter = it.filters?.associateMutable {
                                                                 it.type to MyFilter(type = it.type,
                                                                                     onMismatch = it.onMismatch,
                                                                                     onMatch = it.onMatch,
                                                                                     extra = it.extra.toDynamicObject())
                                                             },
                                                             extra = it.extra.toDynamicObject())
                                   },
                                   logger = loggers?.Logger?.associateMutable {
                                       it.name to MyLogger(name = it.name,
                                                           level = it.level,
                                                           additivity = it.additivity,
                                                           filters = it.filter?.associateMutable {
                                                               it.type to MyFilter(type = it.type,
                                                                                   onMismatch = it.onMismatch,
                                                                                   onMatch = it.onMatch,
                                                                                   extra = it.extra.toDynamicObject())
                                                           },
                                                           appenderRef = it.AppenderRef?.associateMutable { it.ref to it },
                                                           extra = it.extra.toDynamicObject())
                                   },
                                   rootLogger = MyRootLogger(level = loggers?.Root?.level,
                                                             filters = loggers?.Root?.filter?.associateMutable {
                                                                 it.type to MyFilter(type = it.type,
                                                                                     onMismatch = it.onMismatch,
                                                                                     onMatch = it.onMatch,
                                                                                     extra = it.extra.toDynamicObject())
                                                             },
                                                             appenderRef = loggers?.Root?.appenderRef?.associateMutable { it.ref to it },
                                                             extra = loggers?.Root?.extra.toDynamicObject()),
                                   extra = extra.toDynamicObject())
}