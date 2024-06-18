package com.runemate.common.framework.taskstate

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.bot.BotConfig

import com.runemate.common.framework.core.*
import com.runemate.common.framework.taskstate.task.GEBuy
import com.runemate.common.framework.taskstate.task.GESell
import com.runemate.common.framework.taskstate.task.TraverseToGrandExchange
import com.runemate.game.api.script.framework.listeners.InventoryListener
import com.runemate.game.api.script.framework.listeners.PlayerListener
import com.runemate.ui.DefaultUI

class GEState<TSettings : BotConfig>(private val bot: TaskMachine<TSettings>) : TaskState(), InventoryListener, PlayerListener {
    private val log = getLogger("GEState")
    private var geBuy = GEBuy(bot)

    override fun defineTasks() {
        addTask(TraverseToGrandExchange())
        addTask(GESell(bot))
        addTask(geBuy)


    }

    override fun onStart() {
        DefaultUI.setStatus("GE Handler")
        bot.addListener(geBuy)
    }

    override fun onExit() {
        bot.removeListener(geBuy)

    }
}
