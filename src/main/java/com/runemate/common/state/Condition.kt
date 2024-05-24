package com.runemate.common.state

interface Condition: () -> Boolean {
    override operator fun invoke(): Boolean
}