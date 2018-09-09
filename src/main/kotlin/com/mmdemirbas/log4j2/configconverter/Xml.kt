package com.mmdemirbas.log4j2.configconverter

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import java.io.InputStream
import java.io.OutputStream

object Xml : Format() {
    private val mapper = XmlMapper(JacksonXmlModule()).configure2()
    private val reader = mapper.reader(Configuration::class.java)
    private val writer = mapper.writer()

    override fun read(stream: InputStream) = reader.readValue<Configuration>(stream)
    override fun write(obj: Configuration, stream: OutputStream) = writer.writeValue(stream, obj)
}