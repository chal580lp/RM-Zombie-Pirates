package com.runemate.common

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


object LoggerUtils {
    fun getLogger(name: String): Logger = LogManager.getLogger(name)
    //set log4j so debug gets printed

}
