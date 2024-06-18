package com.runemate.common.framework.core

import com.runemate.common.framework.core.addons.BotState


abstract class BaseState {
    private val transitions: MutableList<Transition> = mutableListOf()

    open fun defineTransitions() {}

    fun getTransitions(): List<Transition> {
        return transitions
    }

    protected fun addTransition(transition: Transition) {
        this.transitions += transition
    }

    protected fun addGenericTransition(expression: () -> Boolean, state: () -> BotState) {
        val transition = GenericTransition(expression, state)
        addTransition(transition)
    }

    open fun onStart() {}
    open fun execute() {}
    open fun onExit() {}
}