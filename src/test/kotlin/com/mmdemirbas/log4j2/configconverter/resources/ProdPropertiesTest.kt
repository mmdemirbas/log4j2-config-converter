package com.mmdemirbas.log4j2.configconverter.resources

import com.mmdemirbas.log4j2.configconverter.Config
import com.mmdemirbas.log4j2.configconverter.Serializer.Format.PROPERTIES
import com.mmdemirbas.log4j2.configconverter.TestBase
import org.junit.jupiter.api.DisplayName

@DisplayName("prod.properties")
object ProdPropertiesTest : TestBase(PROPERTIES,
                                     readResource("/com/mmdemirbas/log4j2/configconverter/prod.properties"),
                                     Config())