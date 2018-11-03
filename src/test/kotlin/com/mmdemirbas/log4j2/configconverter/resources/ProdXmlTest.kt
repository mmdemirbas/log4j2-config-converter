package com.mmdemirbas.log4j2.configconverter.resources

import com.mmdemirbas.log4j2.configconverter.Config
import com.mmdemirbas.log4j2.configconverter.Serializer.Format.XML
import com.mmdemirbas.log4j2.configconverter.TestBase
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName

@Disabled
@DisplayName("prod.xml")
object ProdXmlTest : TestBase(XML,
                              readResource("/com/mmdemirbas/log4j2/configconverter/prod.xml"),
                              Config())
