package com.mmdemirbas.log4j2.configconverter

import java.io.Reader
import java.io.Writer

abstract class ConfigFormat {
    abstract fun read(reader: Reader): Config
    abstract fun write(config: Config, writer: Writer)
}