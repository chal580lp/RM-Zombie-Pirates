package com.runemate.zombiepirates.state

import com.runemate.common.LoggerUtils.getLogger

import com.runemate.common.inCombatArea
import com.runemate.common.framework.core.TaskMachine
import com.runemate.common.framework.core.injected
import com.runemate.common.framework.core.TaskState
import com.runemate.common.framework.core.addons.BotState
import com.runemate.game.api.hybrid.local.Camera
import com.runemate.zombiepirates.*


class StartState : TaskState() {

    private val log = getLogger("StartState")
    val bot : Bot by injected()

    override fun defineTransitions() {
        log.debug("Defining transitions for StartState")
        addGenericTransition(
            {
                val result = bot.needToBank()
                log.debug("Condition bot.needToBank() = $result")
                result
            },
            { BotState.RetreatState }
        )
        addGenericTransition(
            {
                val result = !bot.getCombatManager().inCombatArea
                log.debug("Condition !bot.getCombatManager().inCombatArea() = $result")
                result
            },
            { BotState.AdvanceState }
        )
        addGenericTransition(
            {
                true
            },
            { BotState.CombatState }
        )
    }

    override fun onStart() {
        log.info("Starting Task")
        Camera.setZoom(0.1, 0.2)
    }

    override fun execute() {
        log.debug("StartState execute")

        // Check each transition condition and log the result
        getTransitions().forEach { transition ->
            log.debug("Checking transition to ${transition.transitionTo()::class.simpleName}: ${transition.validate()}")
        }

        // Ensure to call super.execute to execute the tasks
        super.execute()

        // After executing tasks, check if a transition is needed
        val transitions = getTransitions()
        transitions.find { it.validate() }?.let { transition ->
            log.debug("Transition found to state: ${transition.transitionTo()::class.simpleName}")
            (bot as TaskMachine<*>).setCurrentState(transition.transitionTo())
            return
        }
    }
}