package com.runemate.common.state
import com.runemate.common.state.di.DIContainer
import com.runemate.game.api.script.framework.LoopingBot
import java.util.*


abstract class TaskMachine : LoopingBot() {

    private var currentState: com.runemate.common.state.State? = null
    fun getCurrentState(): com.runemate.common.state.State? {
        return currentState
    }

    fun setCurrentState(value: com.runemate.common.state.State?) {
        currentState?.onExit()
        eventDispatcher.getListeners().stream()
            .filter { listener: EventListener? -> listener is com.runemate.common.state.State }
            .forEach { listener: EventListener? ->
                if (listener != null) {
                    eventDispatcher.removeListener(listener)
                }
            }
        currentState = value

        if (value is EventListener) eventDispatcher.addListener(value)

        currentState?.defineTransitions()
        (currentState as? TaskState)?.defineTasks()
        currentState?.onStart()
    }

    open override fun onLoop() {
        if (currentState == null) {
            setCurrentState(setDefaultState())
        }

        val transitions: List<Transition> = currentState!!.getTransitions()


        for (transition in transitions) {
            if (transition.validate()) {
                setCurrentState(transition.transitionTo())
                return
            }
        }

        currentState!!.execute()
    }

    open override fun onStart(vararg arguments: String?) {
        println {"Register this"}
        DIContainer.register(this)
        super.onStart(*arguments)
    }


    abstract fun setDefaultState(): com.runemate.common.state.State
}