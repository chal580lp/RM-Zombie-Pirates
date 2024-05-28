package com.runemate.common
import java.util.logging.Logger
import java.util.logging.Level


class RMLogger(private val logger: Logger) {

    fun debug(msg: String) {
        logger.log(Level.FINE, msg)
        println("DEBUG: $msg")
    }

    fun info(msg: String) {
        logger.log(Level.INFO, msg)
        println("INFO: $msg")
    }

    fun warn(msg: String) {
        logger.log(Level.WARNING, msg)
        println("WARN: $msg")
    }

    fun error(msg: String) {
        logger.log(Level.SEVERE, msg)
        println("ERROR: $msg")
    }

    companion object {
        fun getLogger(forClass: Class<*>): RMLogger {
            val logger = Logger.getLogger(forClass.name)
            logger.level = Level.ALL // Ensure all levels are logged
            return RMLogger(logger)
        }
    }
}