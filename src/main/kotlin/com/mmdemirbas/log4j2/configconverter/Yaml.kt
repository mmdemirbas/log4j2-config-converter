package com.mmdemirbas.log4j2.configconverter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.mmdemirbas.log4j2.configconverter.SnakeYaml.configToYamlMap
import java.io.Reader
import java.io.Writer

object Yaml : Format() {
    private val mapper =
            YAMLMapper().disable(WRITE_DOC_START_MARKER).enable(MINIMIZE_QUOTES).setSerializationInclusion(JsonInclude.Include.NON_NULL).enable(
                    JsonParser.Feature.ALLOW_COMMENTS)!!

    override fun load(reader: Reader) = Json.readWithMapper(reader, mapper)
    override fun save(config: Config, writer: Writer) = mapper.writeValue(writer, config.configToYamlMap())
}