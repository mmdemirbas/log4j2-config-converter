package com.mmdemirbas.log4j2.configconverter

import java.io.Reader
import java.io.Writer

object SnakeYaml : Format() {
    // todo: unwrapIfSingle özelliğinin çalıştığından emin olmak için test yazılabilir. Benzer şekilde farklı feature'lar için testler yazılmalı

    // todo: unwrapIfSingle özelliği, generate edilen map'lerde kullanılmalı mı? Kullanılacaksa mümkün olan her yerde mi kullanılsa? Okuma kısmı nasıl olacak?

    // todo: extra'lar genelde key-value pair'ler olduğu için composite entry'lerden önce dump edilebilir. Ya da simple olan kısımları önce, composite olanlar sonra yapılabilir.

    // todo: equalsIgnoreCase kullanılmalı

    // todo: parsing işlemi olabildiğince toleranslı yapılsın. Parse edilemeyen kısımla ilgili warning verilsin ama işlem iptal edilmesin.

    override fun read(reader: Reader) =
            (org.yaml.snakeyaml.Yaml().load(reader) as Map<String, Any>).map("Configuration")!!.toConfig()

    override fun write(config: Config, writer: Writer) =
            writer.write(org.yaml.snakeyaml.Yaml().dumpAsMap(config.configToYamlMap()))

    private fun Config.configToYamlMap() = mapOfNonEmpty("Configuration" to mapOfNonEmpty("advertiser" to advertiser,
                                                                                          "dest" to dest,
                                                                                          "monitorInterval" to monitorIntervalSeconds,
                                                                                          "name" to name,
                                                                                          "packages" to packages?.joinToString(),
                                                                                          "schema" to schemaResource,
                                                                                          "shutdownHook" to isShutdownHookEnabled,
                                                                                          "status" to status?.name,
                                                                                          "strict" to strict,
                                                                                          "shutdownTimeout" to shutdownTimeoutMillis,
                                                                                          "verbose" to verbose,
                                                                                          "properties" to mapOfNonEmpty(
                                                                                                  "property" to properties.orEmpty().map {
                                                                                                      mapOf("name" to it.name,
                                                                                                            "value" to it.value)
                                                                                                  }.unwrapIfSingle()),
                                                                                          "script" to scripts,
                                                                                          "customLevel" to customLevels) + filter.filters() + mapOf(
            "appenders" to appenders?.groupBy { it.type }?.entries?.associate { (type, appenders) ->
                // todo: burada olduğu gibi associate kullanılan diğer yerlerde de key'lerin birbirini ezmediğinden emin ol.
                type to appenders.map { appender ->
                    mapOfNonEmpty("name" to appender.name,
                            // todo: alias'ı sırf properties ile reversible yapabilmek için koydum, non-standard property. belki bir flag'e bağlanabilir. başka yerlerde de aynı pattern var.
                                  "alias" to appender.alias) + appender.extra.orEmpty() + appender.Layout?.type?.let {
                        mapOf(it to appender.Layout?.extra)
                    }.orEmpty() + mapOfNonEmpty("Filters" to appender.filters.filters())

                }.unwrapIfSingle()
            },
            "Loggers" to mapOf("logger" to loggers?.Logger?.map {
                mapOfNonEmpty("name" to it.name,
                              "alias" to it.alias,
                              "level" to it.level?.name,
                              "additivity" to it.additivity) + it.filter.filters() + mapOf("AppenderRef" to it.AppenderRef.appenderRefs()) + it.extra.orEmpty()
            }.unwrapIfSingle(), "Root" to loggers?.Root?.let {
                mapOfNonEmpty("level" to it.level?.name) + it.filter.filters() + mapOf("AppenderRef" to it.appenderRef.appenderRefs()) + it.extra.orEmpty()
            })))

    private fun Iterable<AppenderRef>?.appenderRefs() = this?.map {
        mapOfNonEmpty("ref" to it.ref, "alias" to it.alias) + it.filter.filters()
    }.unwrapIfSingle()

    private fun Iterable<Filter>?.filters() = this?.associate {
        it.type to mapOfNonEmpty("alias" to it.alias,
                                 "onMismatch" to it.onMismatch,
                                 "onMatch" to it.onMatch) + it.extra.orEmpty()
    }.orEmpty()
}