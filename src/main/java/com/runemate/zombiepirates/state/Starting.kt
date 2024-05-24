package com.runemate.zombiepirates.state

import com.runemate.common.RMLogger
import com.runemate.common.combat.inCombatArea
import com.runemate.common.state.di.injected
import com.runemate.common.state.GenericTransition
import com.runemate.common.state.TaskState
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.getAntiPkManager
import com.runemate.zombiepirates.getCombatManager


class Starting : TaskState() {

    private val log: RMLogger = RMLogger.getLogger(this::class.java)
    val bot : Bot by injected()

    override fun defineTransitions() {
        addTransition(
            GenericTransition(
                { bot.getAntiPkManager().beingAttacked() },
                { AntiPk() }
            )
        )
        addTransition(
            GenericTransition(
                { bot.needToBank() },
                { Retreat() }
            )
        )
        addTransition(
            GenericTransition(

                { !bot.getCombatManager().inCombatArea() },
                { Advance() }
            )
        )
        addTransition(
            GenericTransition(
                { true },
                { Fight() }
            )
        )
    }

    override fun onStart() {
        log.debug("Starting Task")
    }
}