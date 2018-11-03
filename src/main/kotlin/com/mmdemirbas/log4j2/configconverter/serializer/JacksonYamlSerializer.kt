package com.mmdemirbas.log4j2.configconverter.serializer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.mmdemirbas.log4j2.configconverter.Config
import com.mmdemirbas.log4j2.configconverter.Serializer
import com.mmdemirbas.log4j2.configconverter.serializer.SnakeYamlSerializer.configToYamlMap
import java.io.Reader
import java.io.Writer

object JacksonYamlSerializer : Serializer(Format.YAML) {
    private val mapper =
            YAMLMapper().disable(WRITE_DOC_START_MARKER).enable(MINIMIZE_QUOTES).setSerializationInclusion(
                    JsonInclude.Include.NON_NULL).enable(JsonParser.Feature.ALLOW_COMMENTS)!!

    override fun deserialize(reader: Reader) =
            JacksonJsonSerializer.readWithMapper(reader, mapper)

    override fun serialize(config: Config, writer: Writer) =
            mapper.writeValue(writer, config.configToYamlMap())
}