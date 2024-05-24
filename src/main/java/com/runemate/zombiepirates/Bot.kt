package com.runemate.zombiepirates

import com.runemate.common.*
import com.runemate.common.state.GlobalTaskManager
import com.runemate.common.state.TaskMachine
import com.runemate.common.state.di.DIContainer
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.zombiepirates.state.Starting
import com.runemate.zombiepirates.state.task.ConsumeFood
import com.runemate.zombiepirates.state.task.ConsumePrayer
import com.runemate.zombiepirates.state.task.ManagePrayer


class Bot : TaskMachine() {
    val botComponents = BotComponents(BotConfig.CHAOS_TEMPLE_AREA, listOf("Zombie pirate"))
    private val globalTaskManager = GlobalTaskManager()
    private val log: RMLogger = RMLogger.getLogger(this::class.java)

    fun needToBank(): Boolean {
        return Inventory.getQuantity(BotConfig.food) <= 3 || !Inventory.newQuery().names(BotConfig.restorePotion).unnoted().results().any()
    }

    override fun onStart(vararg arguments: String?) {
        log.debug("Bot started")
        globalTaskManager.addGlobalTask(ManagePrayer())
        globalTaskManager.addGlobalTask(ConsumePrayer())
        globalTaskManager.addGlobalTask(ConsumeFood())
        DIContainer.register(this)
    }

    override fun setDefaultState(): com.runemate.common.state.State {
        return Starting()
    }
}