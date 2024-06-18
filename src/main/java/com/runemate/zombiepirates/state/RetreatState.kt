package com.runemate.zombiepirates.state


import com.runemate.common.Consumeables
import com.runemate.common.framework.core.TaskState
import com.runemate.common.framework.core.addons.BotState
import com.runemate.common.framework.core.injected
import com.runemate.common.framework.task.DeathHandle
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.state.task.retreat.*

class RetreatState : TaskState() {
    val bot: Bot by injected()



    override fun defineTransitions() {
        addGenericTransition(
                { !Wilderness.isInWilderness() && util.isBankVisibleReachable() },
                { BotState.BankState }
        )
    }

    override fun defineTasks() {
        addTask(TeleportOut())
        addTask(TraverseToSafezone())
        addTask(DeathHandle())
        addTask(TraverseToBank())
    }

    override fun onStart() {
        DefaultUI.setStatus("Traversing to Bank")
    }

    override fun onExit() {
        bot.removeListener(Consumeables)
    }
}