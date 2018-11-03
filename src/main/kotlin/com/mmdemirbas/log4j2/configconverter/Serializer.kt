package com.mmdemirbas.log4j2.configconverter

import java.io.Reader
import java.io.Writer

abstract class Serializer(val format: Format) {
    abstract fun serialize(config: Config, writer: Writer)
    abstract fun deserialize(reader: Reader): Config

    enum class Format {
        YAML, JSON, XML, PROPERTIES
    }
}
