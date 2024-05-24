package com.runemate.common.state

interface Transition {
    fun validate(): Boolean
    fun transitionTo(): State
}