package com.mmdemirbas.log4j2.configconverter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.io.Reader
import java.io.Writer

object Json : Format() {
    private val mapper =
            ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    override fun read(reader: Reader) = readWithMapper(reader, mapper)
    override fun write(config: Config, writer: Writer) = config.writeWithMapper(writer, mapper)
}