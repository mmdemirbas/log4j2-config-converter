package com.mmdemirbas.log4j2.configconverter

import org.apache.logging.log4j.LogManager

/**
 * @author Muhammed Demirbaş
 * @since 2018-09-08 16:27
 */
object Main {
    val logger = LogManager.getLogger(Main::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("Hello!")
    }
}