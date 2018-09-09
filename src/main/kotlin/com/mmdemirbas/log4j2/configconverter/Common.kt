package com.mmdemirbas.log4j2.configconverter

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream


abstract class Format {
    fun read(resourceName: String) = read(Format::class.java.getResourceAsStream(resourceName))
    fun writeToString(obj: Configuration) = ByteArrayOutputStream().apply { use { write(obj, it) } }.toString()

    abstract fun read(stream: InputStream): Configuration
    abstract fun write(obj: Configuration, stream: OutputStream)

    internal fun ObjectMapper.configure(): ObjectMapper {
        enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
        enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        return this
    }

    internal fun ObjectMapper.configure2(): ObjectMapper {
        enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        return this
    }

    // todo: ThresholdFilter
    // todo: ConsoleAppender
    // todo: FileAppender
    // todo: RollingFileAppender
}


// todo: declared type immutable iken default value'lar mutable tanımlansa da bunun bir kullanım alanı var mı? Belki java tarafına geçince anlamlı oluyordur.

data class Configuration(var status: Level = Level.ERROR,
                         var dest: String = "",
                         var strict: Boolean = false,
                         var shutdownHook: String = "",
                         var shutdownTimeout: Long = 0,
                         var verbose: String = "",
                         var packages: List<String> = mutableListOf(),
                         var name: String = "",
                         var monitorInterval: Int = 0,
                         var properties: List<Property> = mutableListOf(),
                         var scripts: List<Script> = mutableListOf(),
                         var customLevels: List<CustomLevel> = mutableListOf(),
                         var filter: List<Filter> = mutableListOf(),
                         var appenders: List<Appender> = mutableListOf(),
                         var loggers: Loggers = Loggers(), @get:JsonAnyGetter
                         var extra: MutableMap<String, Any> = mutableMapOf()) {
    @JsonAnySetter
    fun setExtra(name: String, value: Any) = extra.set(name, value)
}


enum class Level { TRACE, DEBUG, INFO, WARN, ERROR }

data class Property(var name: String = "", @get:JacksonXmlText var value: String = "")

data class Script(var type: String = "",
                  var name: String = "",
                  var language: String = "",
                  var text: String = "",
                  var path: String = "")

data class CustomLevel(var name: String = "", @get:JacksonXmlText var value: Int = -1)

enum class FilterDecision { ACCEPT, NEUTRAL, DENY }

data class Filter(var type: String = "",
                  var onMismatch: FilterDecision = FilterDecision.NEUTRAL,
                  var onMatch: FilterDecision = FilterDecision.NEUTRAL, @get:JsonAnyGetter
                  var extra: MutableMap<String, Any> = mutableMapOf()) {
    @JsonAnySetter
    fun setExtra(name: String, value: Any) = extra.set(name, value)
}


data class Appender(var type: String = "",
                    var name: String = "",
                    var layout: Layout = Layout(),
                    var filters: List<Filter> = mutableListOf(), @get:JsonAnyGetter
                    var extra: MutableMap<String, Any> = mutableMapOf()) {
    @JsonAnySetter
    fun setExtra(name: String, value: Any) = extra.set(name, value)
}


data class Layout(var type: String = "", @get:JsonAnyGetter var extra: MutableMap<String, Any> = mutableMapOf()) {
    @JsonAnySetter
    fun setExtra(name: String, value: Any) = extra.set(name, value)
}

data class Loggers(var logger: List<Logger> = mutableListOf(), var root: RootLogger = RootLogger())

data class Logger(var name: String = "",
                  var level: Level = Level.ERROR,
                  var additivity: Boolean = false,
                  var filter: List<Filter> = mutableListOf(),
                  var appenderRef: List<AppenderRef> = mutableListOf(), @get:JsonAnyGetter
                  var extra: MutableMap<String, Any> = mutableMapOf()) {
    @JsonAnySetter
    fun setExtra(name: String, value: Any) = extra.set(name, value)
}


data class RootLogger(var level: Level = Level.ERROR,
                      var filter: List<Filter> = mutableListOf(),
                      var appenderRef: List<AppenderRef> = mutableListOf(), @get:JsonAnyGetter
                      var extra: MutableMap<String, Any> = mutableMapOf()) {
    @JsonAnySetter
    fun setExtra(name: String, value: Any) = extra.set(name, value)
}


data class AppenderRef(var ref: String = "")

