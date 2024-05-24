package com.runemate.common.state

class GenericTransition(val expression: ()-> Boolean, val state: () -> State): Transition {
    override fun validate(): Boolean {
        return expression()
    }

    override fun transitionTo(): State {
        return state()
    }
}