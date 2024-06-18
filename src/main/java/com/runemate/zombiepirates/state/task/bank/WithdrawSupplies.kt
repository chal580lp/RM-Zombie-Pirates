package com.runemate.zombiepirates.state.task.bank

import com.runemate.common.framework.core.Task
import com.runemate.common.framework.core.injected
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.addons.BotState


class WithdrawSupplies : Task {

    val bot : Bot by injected()
    private val log = getLogger("WithdrawSupplies")

    override fun validate(): Boolean {
        return Bank.isOpen()
    }

    override fun execute() {
        DefaultUI.setStatus("Withdrawing supplies")
        if (!bot.getEquipmentManager().equipWithdrawnItems()) return

        if (bot.getBankManager().shouldDepositInventory(bot.getInvManager())) {
            bot.getBankManager().depositUnwantedItems(bot.getInvManager())
        }
        val missingEquipment = bot.getBankManager().checkMissingEquipment(bot.getEquipmentManager().getMissingEquipment())
        if (missingEquipment.isNotEmpty()) {
            // Switch to the buying state to acquire the missing equipment items
            if (bot.getEquipmentManager().shouldActivateGETask(missingEquipment)) {
                bot.setCurrentState(BotState.GEState)
                log.debug("Items are missing from bank & inv/equip. Activating GE Task.")
                return
            }
        }

        val unequippedItems = bot.getBankManager().getMissingEquipment(bot.getEquipmentManager().getMissingEquipment())
        if (unequippedItems.isNotEmpty()) {
            log.debug("Items to equip found in bank")
            bot.getBankManager().withdrawEquipmentItems(unequippedItems)
        } else {
            log.debug("All equipment items are equipped")
        }

        if (!bot.getEquipmentManager().equipWithdrawnItems()) return

        if (!bot.getBankManager().withdrawInventory(bot.getInvManager().inventory)){
            log.debug("Failed to withdraw inventory items")
            return
        }

        Bank.close()
        log.debug("All withdrawals completed, moving to next state")
        bot.setCurrentState(bot.defaultState())
    }
}