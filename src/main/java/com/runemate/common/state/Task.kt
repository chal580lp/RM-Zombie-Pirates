package com.runemate.common.state


interface Task {
    fun validate(): Boolean
    fun execute()
}