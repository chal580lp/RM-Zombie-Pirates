package com.runemate.zombiepirates.state.task.bank

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.Task
import com.runemate.common.framework.core.injected
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.osrs.local.hud.interfaces.LootingBag
import com.runemate.zombiepirates.Bot

class BankLoot : Task {

    val bot : Bot by injected()
    private val log = getLogger("BankLoot")

    override fun validate(): Boolean {
        return Bank.isOpen() && bot.getLootManager().tripLootValue() > 0 || (LootingBag.getUsedSlots() != 0 && Inventory.contains("Looting bag"))
    }

    override fun execute() {
        if (LootingBag.getUsedSlots() != 0 && Inventory.contains("Looting bag")) {
            // calculate price of all items using util.getprice
            val lootingBagValue = LootingBag.getItems().asList().sumOf { lootItem ->
                lootItem.quantity * (util.priceMap[lootItem.definition?.unnotedId] ?: 0)
            }
            val inventoryValue = util.calculateInventoryLootValue()
            log.debug("Looting bag value is $lootingBagValue gp. Inventory value is $inventoryValue gp.")
            log.debug("Total loot value is ${lootingBagValue + inventoryValue} gp. TripManager value is ${bot.getLootManager().tripLootValue()} gp.")
            bot.getBankManager().emptyLootingBag()
        } else {
            log.warn("No looting bag found in inventory yet looting bag is not empty.")
            bot.getLootManager().clearTripLoot()
        }
    }
}