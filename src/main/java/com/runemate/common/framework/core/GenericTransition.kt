package com.runemate.common.framework.core

import com.runemate.common.framework.core.addons.BotState

class GenericTransition(val expression: ()-> Boolean, val state: () -> BotState): Transition {
    override fun validate(): Boolean {
        return expression()
    }

    override fun transitionTo(): BotState {
        return state()
    }
}