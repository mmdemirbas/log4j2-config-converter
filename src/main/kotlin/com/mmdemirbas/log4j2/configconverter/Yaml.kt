package com.mmdemirbas.log4j2.configconverter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.InputStream
import java.io.OutputStream

object Yaml : Format() {
    private val mapper = ObjectMapper(YAMLFactory()).configure()
    private val reader = mapper.reader(Configuration::class.java)
    private val writer = mapper.writer()

    override fun read(stream: InputStream) = reader.readValue<Configuration>(stream)
    override fun write(obj: Configuration, stream: OutputStream) = writer.writeValue(stream, obj)
}