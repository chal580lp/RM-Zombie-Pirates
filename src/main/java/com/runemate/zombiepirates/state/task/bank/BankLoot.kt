package com.runemate.zombiepirates.state.task.bank

import com.runemate.common.DI
import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank
import com.runemate.game.api.hybrid.local.hud.interfaces.Interfaces
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.osrs.local.hud.interfaces.LootingBag
import com.runemate.game.api.script.Execution
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.getBankManager

class BankLoot : Task {

    val bot : Bot by injected()

    override fun validate(): Boolean {
        return Bank.isOpen() && (LootingBag.getUsedSlots() != 0 && Inventory.contains("Looting bag"))
    }

    override fun execute() {
        bot.getBankManager().emptyLootingBagDI()
    }
}