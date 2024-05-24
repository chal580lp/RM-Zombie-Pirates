package com.runemate.common
import java.util.logging.Logger
import java.util.logging.Level


class RMLogger(private val logger: Logger) {

    fun debug(msg: String) {
        logger.log(Level.FINE, msg)
    }

    fun info(msg: String) {
        logger.log(Level.INFO, msg)
    }

    fun warn(msg: String) {
        logger.log(Level.WARNING, msg)
    }

    fun error(msg: String) {
        logger.log(Level.SEVERE, msg)
    }

    companion object {
        fun getLogger(forClass: Class<*>): RMLogger {
            val logger = Logger.getLogger(forClass.name)
            logger.level = Level.ALL // Ensure all levels are logged
            return RMLogger(logger)
        }
    }
}