package com.runemate.common.framework.core

import com.runemate.common.framework.core.addons.BotState

interface Transition {
    fun validate(): Boolean
    fun transitionTo(): BotState
}