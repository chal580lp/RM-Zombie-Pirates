package com.runemate.zombiepirates.state

import com.runemate.common.RMLogger
import com.runemate.common.combat.inCombatArea
import com.runemate.common.state.GenericTransition
import com.runemate.common.state.TaskState
import com.runemate.common.state.di.injected
import com.runemate.game.api.hybrid.region.Players
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.getCombatManager
import com.runemate.zombiepirates.state.task.advance.BurningAmuletTeleport
import com.runemate.zombiepirates.state.task.advance.TraverseToCombatArea

class Advance : TaskState() {

    private val log: RMLogger = RMLogger.getLogger(this::class.java)

    val bot: Bot by injected()

    init {
        checkGlobalTasks = true
    }

    override fun defineTransitions() {
        addTransition(
            GenericTransition(
                { bot.needToBank() },
                { Retreat() }
            )
        )
        addTransition(
            GenericTransition(
                { bot.getCombatManager().inCombatArea() },
                { Fight() }
            )
        )
    }

    // Tasks
    override fun defineTasks() {
        addTask(BurningAmuletTeleport())
        addTask(TraverseToCombatArea())
    }

    override fun onStart() {
        DefaultUI.setStatus("Traversing to Chaos Temple")
        log.debug("TaskState: Advance")
    }
}