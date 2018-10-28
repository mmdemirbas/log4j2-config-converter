package com.mmdemirbas.log4j2.configconverter.deprecated

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
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
import com.mmdemirbas.log4j2.configconverter.lateinitDynamicObject
import com.mmdemirbas.log4j2.configconverter.mapMutable
import com.mmdemirbas.log4j2.configconverter.toDynamicObject
import org.apache.logging.log4j.core.jackson.Log4jXmlObjectMapper
import java.io.Reader
import java.io.Writer

object DeprecatedXml : Format() {
    private val mapper = Log4jXmlObjectMapper().configure(unwrapRootValue = false)
    private val reader = mapper.readerFor(MyConfig::class.java)
    private val writer = mapper.writer()

    override fun read(reader: Reader) = DeprecatedXml.reader.readValue<MyConfig>(reader).toCommon()
    override fun write(config: Config, writer: Writer) = DeprecatedXml.writer.writeValue(writer, config.toMine())

    @JsonRootName("Configuration")
    data class MyConfig(@field:JacksonXmlProperty(isAttribute = true)
                        var status: Level? = null,
                        @field:JacksonXmlProperty(isAttribute = true)
                        var strict: Boolean? = null,
                        @field:JacksonXmlProperty(isAttribute = true)
                        var dest: String? = null,
                        @field:JacksonXmlProperty(isAttribute = true)
                        var shutdownHook: String? = null,
                        @field:JacksonXmlProperty(isAttribute = true)
                        var shutdownTimeout: Long? = null,
                        @field:JacksonXmlProperty(isAttribute = true)
                        var verbose: String? = null,
                        @field:JacksonXmlProperty(isAttribute = false)
                        var packages: MutableList<String?>? = null,
                        @field:JacksonXmlProperty(isAttribute = true)
                        var name: String? = null,
                        @field:JacksonXmlProperty(isAttribute = true)
                        var monitorInterval: Int? = null,
                        @JacksonXmlElementWrapper
                        var properties: MutableList<Property>? = null, var script: MutableList<Script>? = null,
                        var customLevel: MutableList<CustomLevel>? = null,
                        var filter: MutableList<MyFilter>? = null,
                        @JacksonXmlElementWrapper
                        var appenders: MutableList<MyAppender>? = null,
                        var loggers: MyLoggers? = null,
                        @JsonIgnore
                        var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = extra?.toMap()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            lateinitDynamicObject(::extra)[name] = value
        }
    }

    @JsonRootName("Filter")
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
    data class MyAppender(var type: String? = null, var name: String? = null, var layout: MyLayout? = null,
                          @JacksonXmlElementWrapper
                          var filters: MutableList<MyFilter>? = null,
                          @JsonIgnore
                          var extra: DynamicObject? = null) {
        @JsonAnyGetter
        fun getAny() = extra?.toMap()

        @JsonAnySetter
        fun setAny(name: String, value: DynamicObject) {
            lateinitDynamicObject(::extra)[name] = value
        }
    }

    @JsonRootName("Loggers")
    data class MyLoggers(var logger: MutableList<MyLogger>? = null, var root: MyRootLogger? = null)

    @JsonRootName("Layout")
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

    @JsonRootName("Logger")
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
            lateinitDynamicObject(::extra)[name] = value
        }
    }

    @JsonRootName("Root")
    data class MyRootLogger(var level: Level? = null,
                            var filter: MutableList<MyFilter>? = null,
                            var appenderRef: MutableList<AppenderRef>? = null,
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
                                     strict = strict,
                                     dest = dest,
                                     isShutdownHookEnabled = shutdownHook?.toBoolean(),
                                     shutdownTimeoutMillis = shutdownTimeout,
                                     verbose = verbose,
                                     packages = packages,
                                     name = name,
                                     monitorIntervalSeconds = monitorInterval,
                                     properties = properties,
                                     scripts = script,
                                     customLevels = customLevel,
                                     filter = filter?.mapMutable {
                                         Filter(type = it.type,
                                                onMismatch = it.onMismatch,
                                                onMatch = it.onMatch,
                                                extra = it.extra?.toMap())
                                     },
                                     appenders = appenders?.mapMutable {
                                         Appender(type = it.type,
                                                  name = it.name,
                                                  Layout = Layout(type = it.layout?.type,
                                                                  extra = it.layout?.extra?.toMap()),
                                                  filters = it.filters?.mapMutable {
                                                      Filter(type = it.type,
                                                             onMismatch = it.onMismatch,
                                                             onMatch = it.onMatch,
                                                             extra = it.extra?.toMap())
                                                  },
                                                  extra = it.extra?.toMap())
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
                                                                         filter = loggers?.root?.filter?.mapMutable {
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
                                   properties = properties,
                                   script = scripts,
                                   customLevel = customLevels,
                                   filter = filter?.mapMutable {
                                       MyFilter(type = it.type,
                                                onMismatch = it.onMismatch,
                                                onMatch = it.onMatch,
                                                extra = it.extra.toDynamicObject())
                                   },
                                   appenders = appenders?.mapMutable {
                                       MyAppender(type = it.type,
                                                  name = it.name,
                                                  layout = MyLayout(type = it.Layout?.type,
                                                                    extra = it.Layout?.extra.toDynamicObject()),
                                                  filters = it.filters?.mapMutable {
                                                      MyFilter(type = it.type,
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
                                                                           filter = loggers?.Root?.filter?.mapMutable {
                                                                               MyFilter(type = it.type,
                                                                                        onMismatch = it.onMismatch,
                                                                                        onMatch = it.onMatch,
                                                                                        extra = it.extra.toDynamicObject())
                                                                           },
                                                                           appenderRef = loggers?.Root?.appenderRef,
                                                                           extra = loggers?.Root?.extra.toDynamicObject())),
                                   extra = extra.toDynamicObject())

}