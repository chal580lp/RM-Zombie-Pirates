package com.runemate.common.framework.core

interface Condition: () -> Boolean {
    override operator fun invoke(): Boolean
}