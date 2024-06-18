package com.runemate.zombiepirates.state


import com.runemate.common.Consumeables
import com.runemate.common.inCombatArea
import com.runemate.common.framework.core.TaskState
import com.runemate.common.framework.core.addons.BotState
import com.runemate.common.framework.core.injected
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.state.task.ManagePrayer
import com.runemate.zombiepirates.state.task.advance.BurningAmuletTeleport
import com.runemate.zombiepirates.state.task.advance.TraverseToCombatArea
import com.runemate.common.LoggerUtils.getLogger


class AdvanceState : TaskState() {

    private val log = getLogger("AdvanceState")

    val bot: Bot by injected()

    override fun defineTransitions() {
        addGenericTransition(
                { bot.needToBank() },
                { BotState.RetreatState }
        )
        addGenericTransition(
                { bot.getCombatManager().inCombatArea },
                { BotState.CombatState }
        )
    }

    // Tasks
    override fun defineTasks() {
        addTask(ManagePrayer())
        addTask(BurningAmuletTeleport())
        addTask(TraverseToCombatArea())
    }

    override fun onStart() {
        DefaultUI.setStatus("Traversing to Chaos Temple")
        bot.addListener(Consumeables)
    }
}