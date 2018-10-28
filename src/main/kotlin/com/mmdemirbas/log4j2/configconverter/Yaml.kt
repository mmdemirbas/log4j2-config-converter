package com.mmdemirbas.log4j2.configconverter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.mmdemirbas.log4j2.configconverter.Json.writeWithMapper
import java.io.Reader
import java.io.Writer

object Yaml : ConfigFormat() {
    private val mapper =
            YAMLMapper().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).disable(YAMLGenerator.Feature.MINIMIZE_QUOTES).setSerializationInclusion(
                    JsonInclude.Include.NON_NULL)!!

    override fun read(reader: Reader): Config {
        return Json.readWithMapper(reader, mapper)
    }

    override fun write(config: Config, writer: Writer) {
        config.writeWithMapper(writer, mapper)
    }
}