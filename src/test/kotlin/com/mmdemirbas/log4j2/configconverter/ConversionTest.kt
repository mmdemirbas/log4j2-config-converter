package com.mmdemirbas.log4j2.configconverter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.StringReader

class ConversionTest {
    @Test
    fun `properties is reversible`() {
        Properties.assertReadWriteGivesSameResult("/com/mmdemirbas/log4j2/configconverter/prod.properties")
    }

    @Test
    fun `properties to yaml is reversible`() {
        val propConfig = log("propConfig") {
            Properties.readResource("/com/mmdemirbas/log4j2/configconverter/prod.properties")
        }
        val yamlString = log("yamlString") { propConfig.toString(SnakeYaml) }
        val yamlConfig = log("yamlConfig") {
            SnakeYaml.read(StringReader(yamlString))
        }
        Assertions.assertEquals(propConfig, yamlConfig)

        val propString = log("propString") { yamlConfig.toString(Properties) }
        val propConfigBack = log("propConfigBack") {
            Properties.read(StringReader(propString))
        }
        Assertions.assertEquals(propConfig, propConfigBack)
    }

    private fun <T> log(title: String, fn: () -> T): T {
        println("===[ $title ]=====================================================================================================================")
        return fn().also {
            println(it)
            println()
        }
    }
}
