package com.runemate.zombiepirates.state

import com.runemate.common.DI
import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.PrayerFlicker

import com.runemate.common.inCombatArea
import com.runemate.common.framework.core.TaskState
import com.runemate.common.framework.core.addons.BotState
import com.runemate.common.framework.core.injected
import com.runemate.common.item.EquipmentSlot
import com.runemate.common.util
import com.runemate.game.api.hybrid.entities.GroundItem
import com.runemate.game.api.hybrid.local.hud.interfaces.Equipment
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.script.framework.listeners.GroundItemListener
import com.runemate.game.api.script.framework.listeners.events.GroundItemSpawnedEvent
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.state.task.ManagePrayer
import com.runemate.zombiepirates.state.task.combat.AttackNpc
import com.runemate.zombiepirates.state.task.combat.FindNpc
import com.runemate.zombiepirates.state.task.combat.Loot


class CombatState : TaskState(), GroundItemListener {

    private val log = getLogger("CombatState")
    val bot: Bot by injected()


    // Transitions
    override fun defineTransitions() {
        addGenericTransition({ bot.needToBank() }, { BotState.RetreatState })
        addGenericTransition({ !bot.getCombatManager().inCombatArea }, { BotState.AdvanceState })
    }

    // Tasks
    override fun defineTasks() {
        if (!bot.settings().prayerFlick) addTask(ManagePrayer())
        addTask(Loot())
        addTask(FindNpc())
        addTask(AttackNpc())
    }

    override fun onStart() {
        if (Players.getLocal()?.getWornItem(Equipment.Slot.WEAPON) == null) {
            log.warn("No weapon equipped")
            bot.setCurrentState(BotState.RetreatState)
            return
        }
        DefaultUI.setStatus("Killing Pirate Zombies")
        if (util.isAutoRetaliateEnabled()) util.toggleAutoRetaliate()
        bot.getLootManager().clearTripLoot()
        bot.addListener(DI)
        bot.addListener(PrayerFlicker)
        bot.addListener(this)

    }

    override fun onExit() {
        bot.removeListener(DI)
        bot.removeListener(PrayerFlicker)
        bot.removeListener(this)
    }

    override fun onGroundItemSpawned(event: GroundItemSpawnedEvent?) {
        event.let {
            if (event?.groundItem?.ownership == GroundItem.Ownership.SELF) {
                event.let { bot.getLootManager().addLoot(it.groundItem) }
            }
        }
    }
}

