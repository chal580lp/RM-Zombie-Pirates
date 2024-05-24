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
import com.runemate.zombiepirates.state.task.fight.AttackNpc
import com.runemate.zombiepirates.state.task.fight.FindNpc
import com.runemate.zombiepirates.state.task.fight.Loot


class Fight : TaskState() {

    private val log: RMLogger = RMLogger.getLogger(this::class.java)

    val bot: Bot by injected()

    init {
        checkGlobalTasks = true
    }

    // Transitions
    override fun defineTransitions() {
        addTransition(
            GenericTransition(
                { bot.needToBank() },
                { Retreat() }
            )
        )
        addTransition(
            GenericTransition(
                { !bot.getCombatManager().inCombatArea()},
                { Advance() }
            )
        )
    }

    // Tasks
    override fun defineTasks() {
        addTask(Loot())
        addTask(FindNpc())
        addTask(AttackNpc())
    }

    override fun onStart() {
        DefaultUI.setStatus("Killing Pirate Zombies")
        log.debug("TaskState: Fight")
    }
}

