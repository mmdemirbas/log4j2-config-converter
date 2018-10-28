package com.mmdemirbas.log4j2.configconverter

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

object Xml : Format() {
    private const val XINCLUDE_FIXUP_LANGUAGE = "http://apache.org/xml/features/xinclude/fixup-language"
    private const val XINCLUDE_FIXUP_BASE_URIS = "http://apache.org/xml/features/xinclude/fixup-base-uris"

    override fun read(reader: Reader): Config {
        val buffer = reader.readText()
        val root = parseXml(buffer)
        val map = root.domNodeToMap() as Map<String, Any>
        return map.toConfig()
    }

    override fun write(config: Config, writer: Writer) {
        TODO("not implemented")
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

    private fun parseXml(xmlString: String, enableXInclude: Boolean) = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true

        if (true) {
            isValidating = false
            isExpandEntityReferences = false
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
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
                val text = all.filter { it is Text }.joinToString("") { it.nodeValue }.trim()
                when {
                    filtered.isEmpty() -> text
                    else               -> {
                        val selected = if (text.isEmpty()) filtered else filtered + ("value" to text)
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
}