package com.mmdemirbas.log4j2.configconverter

import com.mmdemirbas.log4j2.configconverter.Serializer.Format.JSON
import com.mmdemirbas.log4j2.configconverter.Serializer.Format.PROPERTIES
import com.mmdemirbas.log4j2.configconverter.Serializer.Format.XML
import com.mmdemirbas.log4j2.configconverter.Serializer.Format.YAML
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested

@DisplayName("configuration")
class ConfigTest {
    @Nested
    inner class advertiser : TestBase(strings = mapOf(PROPERTIES to "advertiser=123\n",
                                                      YAML to "Configuration:\n  advertiser: '123'\n",
                                                      XML to "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Configuration advertiser=\"123\"/>\n",
                                                      JSON to "{\n  \"Configuration\" : {\n    \"advertiser\" : \"123\"\n  }\n}"),
                                      config = Config(advertiser = "123"))

}