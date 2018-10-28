package com.mmdemirbas.log4j2.configconverter

import org.apache.logging.log4j.core.util.Throwables
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
}