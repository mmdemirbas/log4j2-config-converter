package com.mmdemirbas.log4j2.configconverter

import java.io.Reader
import java.io.Writer

abstract class Format {
    abstract fun load(reader: Reader): Config
    abstract fun save(config: Config, writer: Writer)
}