package com.mmdemirbas.log4j2.configconverter

import java.io.StringWriter

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
                  var packages: String? = null,
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

}

fun Config.toString(serializer: Serializer) = StringWriter().use { writer ->
    serializer.serialize(this, writer)
    writer.toString()
}

fun String.toConfig(serializer: Serializer) = reader().use { reader ->
    serializer.deserialize(reader)
}

enum class Level { all, trace, debug, info, warn, error, fatal, off }

data class Property(var name: String? = null, var value: String? = null)

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


data class Layout(var type: String? = null,
                  var extra: MutableMap<String, Any?>? = null)

data class Loggers(var Logger: MutableList<Logger>? = null,
                   var Root: RootLogger? = null)

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

data class AppenderRef(var alias: String? = null,
                       var ref: String? = null,
                       var filter: MutableList<Filter>? = null)
