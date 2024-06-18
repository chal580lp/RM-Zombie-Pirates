package com.runemate.common.framework.core


interface Task {
    fun validate(): Boolean
    fun execute()
}