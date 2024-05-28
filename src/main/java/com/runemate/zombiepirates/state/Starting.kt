package com.runemate.zombiepirates.state

import com.runemate.common.RMLogger
import com.runemate.common.combat.inCombatArea
import com.runemate.common.state.di.injected
import com.runemate.common.state.GenericTransition
import com.runemate.common.state.TaskState
import com.runemate.game.api.hybrid.local.Camera
import com.runemate.zombiepirates.*


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
        log.info("Starting Task")
        setInventoryAndEquipment()
    }

    fun setInventoryAndEquipment() {
        bot.getInventoryManager().inventory = BotConfig.inventoryItems
        bot.getEquipmentManager().setEquipment()
        Camera.setZoom(0.1, 0.2)
        log.info("Inventory and Equipment set")
    }
}