package com.runemate.zombiepirates.state.task.bank

import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.getBankManager
import com.runemate.zombiepirates.getEquipmentManager
import com.runemate.zombiepirates.getInventoryManager

class WithdrawSupplies : Task {

    val bot : Bot by injected()

    override fun validate(): Boolean {
        return Bank.isOpen()
    }

    override fun execute() {
        DefaultUI.setStatus("Withdrawing supplies")
        if (bot.getBankManager().shouldDepositInventory(bot.getInventoryManager())) Bank.depositInventory()
        bot.getBankManager().checkAndEquipItems(bot.getEquipmentManager())
        bot.getBankManager().withdrawInventory(bot.getInventoryManager().inventory)
        Bank.close()
    }
}