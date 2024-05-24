package com.runemate.common

import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank
import com.runemate.game.api.hybrid.local.hud.interfaces.Equipment
import com.runemate.game.api.hybrid.local.hud.interfaces.Interfaces
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.hybrid.region.GameObjects
import com.runemate.game.api.osrs.local.hud.interfaces.LootingBag
import com.runemate.game.api.script.Execution
import java.util.logging.Logger


class BankManager() {

    fun needToBank(): Boolean {

        return false
    }
    private val log: RMLogger = RMLogger.getLogger(this::class.java)

    fun withdrawInventory(inventoryManager: InventoryManager): Boolean {
        Bank.openFirstTab()
        inventoryManager.inventory.forEach { item ->
            if (!Inventory.contains(item.name) && !Equipment.contains(item.name)) {
                val bankItem = Bank.newQuery().ids(item.id).placeholder(false).results().firstOrNull() ?: return false
                println("Withdrawing ${bankItem.definition?.name} AMOUNT: ${item.amount}")
                Bank.withdraw(item.id, item.amount)
                if (!Execution.delayUntil({ Inventory.contains(item.name) }, 600)) {
                    println("Failed to withdraw ${item.name}")
                    return false
                }
            }
        }
        return checkWithdrawalSuccess(inventoryManager)
    }

    fun checkWithdrawalSuccess(inventoryManager: InventoryManager): Boolean {
        return inventoryManager.inventory.all { item ->
            Inventory.contains(item.name) && Inventory.getQuantity(item.name) == item.amount
        }
    }

    fun checkAndEquipItems(equipmentManager: EquipmentManager): Boolean {
        equipmentManager.setEquipment()
        val missingItems = equipmentManager.equipment.filterNot { Equipment.contains(it.name) }

        if (missingItems.isEmpty()) {
            log.debug("All equipment items are already equipped.")
            return true
        }

        if (!Bank.open()) {
            log.debug("Failed to open bank.")
            return false
        }

        missingItems.forEach { item ->
            val bankItem = Bank.newQuery().ids(item.id).results().firstOrNull()
            if (bankItem != null) {
                log.debug("Withdrawing ${bankItem.definition?.name}")
                Bank.withdraw(item.id, 1)
                if (!Execution.delayUntil({ Inventory.contains(item.name) }, 600)) {
                    log.debug("Failed to withdraw ${item.name}")
                    return false
                }
            } else {
                log.debug("${item.name} is not in the bank.")
                return false
            }
        }

        missingItems.forEach { item ->
            val inventoryItem = Inventory.newQuery().names(item.name).results().firstOrNull()
            if (inventoryItem != null) {
                log.debug("Equipping ${inventoryItem.definition?.name}")
                inventoryItem.interact("Wear", "Equip")
                if (!Execution.delayUntil({ Equipment.contains(item.name) }, 600)) {
                    log.debug("Failed to equip ${item.name}")
                    return false
                }
            } else {
                log.debug("${item.name} is not in the inventory.")
                return false
            }
        }

        return true
    }

    fun withdrawGear() {

    }

    fun equipGear() {

    }

    fun anyBanksVisible() : Boolean {
        return !GameObjects.newQuery()
            .names("Bank booth", "Bank chest")
            .visible()
            .results()
            .any()
    }

    fun emptyLootingBagDI() : Boolean {
        if (!Inventory.contains("Looting bag")) return false
        val x = Interfaces.getAt(15,6)
        if (x != null) DI.send(MenuAction.forInterfaceComponent(x,"Deposit loot"))
        Execution.delayUntil({ LootingBag.isEmpty()}, 1200)
        return true
    }
}