package com.mmdemirbas.log4j2.configconverter.serializer

import com.mmdemirbas.log4j2.configconverter.Config
import com.mmdemirbas.log4j2.configconverter.Serializer
import com.mmdemirbas.log4j2.configconverter.Serializer.Format.XML
import com.mmdemirbas.log4j2.configconverter.util.mapOfNonEmpty
import com.mmdemirbas.log4j2.configconverter.util.toConfig
import org.apache.logging.log4j.core.util.Throwables
import org.w3c.dom.Attr
import org.w3c.dom.CDATASection
import org.w3c.dom.Comment
import org.w3c.dom.Document
import org.w3c.dom.DocumentFragment
import org.w3c.dom.DocumentType
import org.w3c.dom.Element
import org.w3c.dom.Entity
import org.w3c.dom.EntityReference
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.NodeList
import org.w3c.dom.Notation
import org.w3c.dom.ProcessingInstruction
import org.w3c.dom.Text
import org.xml.sax.InputSource
import java.io.Reader
import java.io.StringReader
import java.io.Writer
import javax.xml.parsers.DocumentBuilderFactory

object XmlSerializer : Serializer(XML) {
    private const val XINCLUDE_FIXUP_LANGUAGE =
            "http://apache.org/xml/features/xinclude/fixup-language"
    private const val XINCLUDE_FIXUP_BASE_URIS =
            "http://apache.org/xml/features/xinclude/fixup-base-uris"

    override fun deserialize(reader: Reader): Config {
        val buffer = reader.readText()
        val root = parseXml(buffer)
        val map = root.domNodeToMap() as Map<String, Any>
        return map.toConfig()
    }

    private fun parseXml(xmlString: String) = try {
        parseXml(xmlString, true)
    } catch (e: Exception) {
        // LOG4J2-1127
        if (Throwables.getRootCause(e) is UnsupportedOperationException) {
            parseXml(xmlString, false)
        } else {
            throw e
        }
    }

    private fun parseXml(xmlString: String, enableXInclude: Boolean) =
            DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true

                if (true) {
                    isValidating = false
                    isExpandEntityReferences = false
                    setFeature("http://xml.org/sax/features/external-general-entities",
                               false)
                    setFeature("http://xml.org/sax/features/external-parameter-entities",
                               false)
                    setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
                               false)
                }

                if (enableXInclude) {
                    isXIncludeAware = true
                    setFeature(XINCLUDE_FIXUP_BASE_URIS, true)
                    setFeature(XINCLUDE_FIXUP_LANGUAGE, true)
                }
            }.newDocumentBuilder().parse(InputSource(StringReader(xmlString))).documentElement!!

    private fun org.w3c.dom.Node.domNodeToMap(): Any? {
        return when (this) {
            is Element               -> {
                val all = attributes.toList() + childNodes.toList()
                val filtered = all.mapNotNull {
                    when (it) {
                        is Element, is Attr -> it.nodeName to it.domNodeToMap()
                        else                -> null
                    }
                }
                val text =
                        all.filter { it is Text }
                            .joinToString("") { it.nodeValue }.trim()
                when {
                    filtered.isEmpty() -> text
                    else               -> {
                        val selected =
                                if (text.isEmpty()) filtered else filtered + ("value" to text)
                        val names = selected.map { it.first }
                        val unique = names.distinct().size == names.size
                        when {
                            unique -> selected.toMap()
                            else   -> selected.map { mapOf(it) }
                        }
                    }
                }
            }
            is Attr                  -> value
            is Text                  -> wholeText
            is CDATASection          -> TODO()
            is EntityReference       -> TODO()
            is Entity                -> TODO()
            is ProcessingInstruction -> TODO()
            is Comment               -> null
            is Document              -> TODO()
            is DocumentType          -> TODO()
            is DocumentFragment      -> TODO()
            is Notation              -> TODO()
            else                     -> TODO()
        }
    }

    private fun NamedNodeMap.toList() = (0 until length).map(this::item)
    private fun NodeList.toList() = (0 until length).map(this::item)


    override fun serialize(config: Config, writer: Writer) {
        config.configToXmlElement().write(writer)
    }

    private fun Config.configToXmlElement(): XmlElement {
        return XmlElement("Configuration",
                          attributes = mapOfNonEmpty("advertiser" to advertiser,
                                                     "dest" to dest,
                                                     "monitorInterval" to monitorIntervalSeconds?.toString(),
                                                     "status" to status?.name,
                                                     "strict" to strict?.toString(),
                                                     "name" to name,
                                                     "packages" to packages?.joinToString(),
                                                     "schema" to schemaResource,
                                                     "shutdownHook" to isShutdownHookEnabled?.toString(),
                                                     "shutdownTimeout" to shutdownTimeoutMillis?.toString(),
                                                     "verbose" to verbose),
                          elements = listOf(XmlElement("Properties",
                                                       elements = properties?.map { property ->
                                                           XmlElement("Property",
                                                                      attributes = mapOfNonEmpty(
                                                                              "name" to property.name),
                                                                      value = property.value)
                                                       }),
                                            XmlElement("Scripts"),
                                            XmlElement("CustomLevels"),
                                            XmlElement("Filter"),
                                            XmlElement("Appenders"),
                                            XmlElement("Loggers")))
    }

    data class XmlElement(val name: String,
                          val attributes: Map<String, String>? = null,
                          val value: String? = null,
                          val elements: List<XmlElement>? = null) {
        fun write(writer: Writer, level: Int = 0) {
            writer.apply {
                if (level == 0) writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")

                val padding = "    ".repeat(level)
                write("$padding<$name")
                attributes?.forEach { key, value -> write(" $key=\"$value\"") }

                when {
                    !elements.orEmpty().isEmpty() -> {
                        write(">\n")
                        if (!value.isNullOrEmpty()) write("$padding$value")
                        elements?.forEach { element ->
                            element.write(writer, level + 1)
                        }
                        write("$padding</$name>\n")
                    }
                    value.isNullOrEmpty()         -> write("/>\n")
                    else                          -> write(">$value</$name>\n")
                }
            }
        }
    }
}