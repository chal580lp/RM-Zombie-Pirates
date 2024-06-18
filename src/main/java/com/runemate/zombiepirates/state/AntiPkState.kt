package com.runemate.zombiepirates.state

import com.runemate.common.Consumeables
import com.runemate.common.LoggerUtils.getLogger

import com.runemate.common.framework.core.TaskState
import com.runemate.common.framework.core.addons.BotState
import com.runemate.common.framework.core.injected
import com.runemate.common.framework.task.PKConsumeables
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.game.api.script.framework.listeners.PlayerListener
import com.runemate.game.api.script.framework.listeners.events.DeathEvent
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.state.task.retreat.TraverseToSafezone

class AntiPkState : TaskState() {

    private val log = getLogger("AntiPkState")
    val bot: Bot by injected()


    override fun defineTransitions() {
        addGenericTransition({ !Wilderness.isInWilderness() }, { BotState.RetreatState }) }

    override fun defineTasks() {
        addTask(PKConsumeables(bot))
        addTask(TraverseToSafezone())

    }

    override fun onStart() {
        log.debug("STARTING ANTI PK STATE")
        DefaultUI.setStatus("AntiPk Handler")
        bot.removeListener(Consumeables)
    }
}