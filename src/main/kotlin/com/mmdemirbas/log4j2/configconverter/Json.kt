package com.mmdemirbas.log4j2.configconverter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BigIntegerNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.databind.node.FloatNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.LongNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.databind.node.ShortNode
import com.fasterxml.jackson.databind.node.TextNode
import java.io.Reader
import java.io.Writer

object Json : ConfigFormat() {
    private val mapper =
            ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    override fun read(reader: Reader): Config {
        return readWithMapper(reader, mapper)
    }

    override fun write(config: Config, writer: Writer) {
        config.writeWithMapper(writer, mapper)
    }

    fun readWithMapper(reader: Reader, objectMapper: ObjectMapper): Config {
        val foundRoot = objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true).readTree(reader)
        val logicalRoot = foundRoot.singleOrNull() ?: foundRoot
        return (logicalRoot.jsonNodeToMap() as Map<String, Any>).toConfig()
    }

    fun Config.writeWithMapper(writer: Writer, objectMapper: ObjectMapper) {
        objectMapper.writeValue(writer, mapOf("Configuration" to this))
    }

    private fun JsonNode.jsonNodeToMap(): Any? {
        return when (this) {
            is ArrayNode      -> elements().asSequence().map { it.jsonNodeToMap() }.toList()
            is ObjectNode     -> fields().asSequence().associate { (k, v) -> k to v.jsonNodeToMap() }
            is NullNode       -> null
            is MissingNode    -> TODO()
            is TextNode       -> asText()
            is IntNode        -> asInt()
            is ShortNode      -> TODO()
            is LongNode       -> asLong()
            is DoubleNode     -> asDouble()
            is FloatNode      -> TODO()
            is BigIntegerNode -> TODO()
            is DecimalNode    -> TODO()
            is BooleanNode    -> asBoolean()
            is BinaryNode     -> TODO()
            is POJONode       -> TODO()
            else              -> TODO()
        }
    }
}