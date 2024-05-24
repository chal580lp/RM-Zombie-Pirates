package com.runemate.common.state

import kotlin.collections.ArrayList

abstract class State() {
    private var transitions: List<Transition> = ArrayList()

    open fun defineTransitions() {
        println("Define transitions")
    }


    fun getTransitions(): List<Transition> {
        return transitions
    }

    protected fun addTransition(transition: Transition) {
        this.transitions += transition
    }

    open fun onStart() {}
    open fun execute() {}
    open fun onExit() {}
}