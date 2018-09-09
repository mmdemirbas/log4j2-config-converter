package com.mmdemirbas.log4j2.configconverter

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import java.io.OutputStream

object Json : Format() {
    private val mapper = ObjectMapper().configure()
    private val reader = mapper.reader(Configuration::class.java)
    private val writer = mapper.writer()

    override fun read(stream: InputStream) = reader.readValue<Configuration>(stream)
    override fun write(obj: Configuration, stream: OutputStream) = writer.writeValue(stream, obj)
}