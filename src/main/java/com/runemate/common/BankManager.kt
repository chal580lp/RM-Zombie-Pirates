package com.runemate.common

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.TaskMachine
import com.runemate.common.framework.core.addons.BotState
import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.item.*
import com.runemate.game.api.hybrid.local.hud.interfaces.*
import com.runemate.game.api.osrs.local.hud.interfaces.LootingBag
import com.runemate.game.api.script.Execution

enum class EquipmentStatus {
    ALL_EQUIPPED,
    ITEMS_TO_EQUIP,
    ITEMS_MISSING
}

class BankManager<TSettings : BotConfig>(var bot: TaskMachine<TSettings>) {

    private val log = getLogger("BankManager")

    fun withdrawInventory(items: List<SetItem>): Boolean {
        log.info("Withdrawing items: ${items.size}")
        setWithdrawModeToItem()

        val bankItemMap = getBankItemMap(items) ?: return false

        val missingItems = getMissingItems(items, bankItemMap)
        if (missingItems.isNotEmpty()) {
            log.debug("Missing required items")
            failedToFindAdequateItem(*missingItems.toTypedArray())
            return false
        }

        return withdrawItems(bankItemMap)
    }

    private fun setWithdrawModeToItem() {
        if (Bank.getWithdrawMode() == Bank.WithdrawMode.NOTE) {
            log.debug("Setting withdraw mode to ITEM")
            Bank.setWithdrawMode(Bank.WithdrawMode.ITEM)
        }
    }

    private fun getBankItemMap(items: List<SetItem>): Map<SetItem, SpriteItem?>? {
        return items.associateWith { item ->
            findBankItem(item)?.also { bankItem ->
                if (bankItem.name?.let { Restore.isRestore(it) } == true) {
                    if (!Restore.isMaxDose(bankItem.name ?: "")) {
                        log.debug("Restore ${bankItem.name} is not max dosage in bank.")
                        failedToFindAdequateItem(item)
                        return null
                    }
                }
            }
        }
    }

    private fun getMissingItems(items: List<SetItem>, bankItemMap: Map<SetItem, SpriteItem?>): List<SetItem> {
        return items.filter { item ->
            item.required && item.name !in InventoryManager.unRequiredItems && bankItemMap[item] == null
        }
    }

    private fun withdrawItems(bankItemMap: Map<SetItem, SpriteItem?>) : Boolean {
        bankItemMap.forEach { (setItem, spriteItem) ->
            if (bot.isPaused) return false
            if (spriteItem == null) return@forEach
            if (Inventory.isFull()) return false

            val name = spriteItem.name ?: return@forEach
            log.debug("Checking inventory and equipment for item: $name")
            val quantityInInventory = getQuantityInInventory(setItem)
            val amountToWithdraw = if (setItem.quantity > 1) setItem.quantity - quantityInInventory else 1
            val needsWithdrawal =
                    !isItemInInventoryOrEquipment(setItem)
                    || amountToWithdraw > 1

            if (needsWithdrawal) {
                if (amountToWithdraw == 0) {
                    log.warn("You are trying to withdraw 0 of item: $name")
                    return@forEach
                }
                log.debug("Withdrawing ${spriteItem.name} Quantity: $amountToWithdraw")
                Bank.withdraw(spriteItem, amountToWithdraw)

                val itemWithdrawn = Execution.delayUntil({
                    Inventory.contains(spriteItem.id) && Inventory.getQuantity(spriteItem.id) >= setItem.quantity
                }, 3000)

                if (!itemWithdrawn) {
                    log.debug("Failed to withdraw $name, attempting once more")
                    if (!Bank.withdraw(spriteItem, amountToWithdraw)) return false
                }
            }
        }
        return true
    }

    private fun failedToFindAdequateItem(vararg item: SetItem): Boolean {
        item.forEach { log.debug("Failed to find adequate item: ${it.name}") }
        log.debug("Activating Ge State")
        bot.setCurrentState(BotState.GEState)
        return false
    }

    private fun isItemInInventoryOrEquipment(item: SetItem): Boolean {
        val pattern = item.invPattern
        val equipPattern = item.bankPattern
        return Inventory.contains(pattern)|| Equipment.contains(equipPattern)
    }

    private fun getQuantityInInventory(item: SetItem): Int {
        val pattern = item.invPattern
        val x = Inventory.getQuantity(pattern)
        val y = Inventory.getQuantity(item.name)
        if (x != y) {
            log.debug("Pattern '{}' doesn't match '{}' quantity in inventory. {}-{}", pattern, item.name,x,y)
        }
//        log.debug("Item $pattern quantity in inventory: $x")
        return maxOf(x, y)
    }

    private fun findBankItem(item: SetItem): SpriteItem? {
        // Handle items with patterns like "Blighted super restore.*\\(.*\\)"
        if (item.isNumbered) {
            log.debug("Item '{}' is numbered (ID:{})", item.name, item.id)
            // Create a pattern to match the item name with any number in parentheses
            val pattern = item.numberPattern
            //log.debug("Generated pattern for item '{}': '{}'", item.name, pattern)
            val allItems = Bank.newQuery().names(pattern).results().toList()
            if (allItems.isEmpty()) {
                log.debug("Failed to find any items matching pattern: '{}'", pattern)
                return null
            }
            val findHighest: Boolean = item.isRestore || item.isBoost
            log.debug("Pattern '{}' found: {} '{}'", pattern, allItems.size, allItems.map { it.name })
            return findHighestOrLowestNumberedItem(allItems, findHighest)
        }

        // Query the bank for the item by ID and name
        val itemById = Bank.newQuery().ids(item.id).placeholder(false).results().firstOrNull()
        val itemByName = Bank.newQuery().names(item.name).placeholder(false).results().firstOrNull()

        // Return the bank item found by ID or name
        return itemById ?: itemByName ?: run {
            log.debug("Failed to find ${item.name} or ID ${item.id} in bank.")
            null
        }
    }

    fun findHighestOrLowestNumberedItem(
        items: List<SpriteItem>,
        findHighest: Boolean = true
    ): SpriteItem? {

        return when {
            items.isEmpty() -> {
                log.warn("FUNCTIONAL FLOW ERROR: Failed to find any numbered items")
                null
            }
            findHighest -> {
                val highestNumberedItem = items.maxByOrNull { spriteItem ->
                    val matchResult = "\\d+".toRegex().find(spriteItem.name ?: "")
                    matchResult?.groupValues?.get(0)?.toInt() ?: 0
                }
                log.debug("Selected highest numbered item: ${highestNumberedItem?.name}")
                highestNumberedItem
            }
            else -> {
                val lowestNumberedItem = items.minByOrNull { spriteItem ->
                    val matchResult = "\\d+".toRegex().find(spriteItem.name ?: "")
                    matchResult?.groupValues?.get(0)?.toInt() ?: 0
                }
                log.debug("Selected lowest numbered item: ${lowestNumberedItem?.name}")
                lowestNumberedItem
            }
        }
    }

    fun shouldDepositInventory(inventoryManager: InventoryManager): Boolean {
        return getItemsToDeposit(inventoryManager).isNotEmpty()
    }

    fun depositUnwantedItems(inventoryManager: InventoryManager): Boolean {
        val itemsToDeposit = getItemsToDeposit(inventoryManager)

        if (itemsToDeposit.isEmpty()) {
            log.debug("No items to deposit.")
            return true
        }

        log.debug("Depositing unwanted items: {}", itemsToDeposit.map { Inventory.getItems(it.first) })
        if (itemsToDeposit.size == Inventory.getItems().size) {
            log.debug("All items in inventory are unwanted.")
            return Bank.depositInventory()
        }

        itemsToDeposit.forEach { (itemId, quantity) ->
            if (bot.isPaused) return false
            if (!Bank.deposit(itemId, quantity)) {
                log.debug("Failed to deposit item: {}", Inventory.getItems(itemId))
                return false
            }
        }

        log.debug("Deposited unwanted items successfully.")
        return true
    }

    private fun getItemsToDeposit(inventoryManager: InventoryManager): List<Pair<Int, Int>> {
        val inventoryItems = inventoryManager.inventory

        val itemsToDeposit = mutableListOf<Pair<Int, Int>>()

        Inventory.getItems().forEach { item ->
            val itemName = item.name.orEmpty()
            val isInvItem = inventoryItems.any {
                    it.invPattern.matcher(itemName).matches()
                    || it.id == item.id
            }
            if (!isInvItem) {
                itemsToDeposit.add(item.id to item.quantity)
            } else {
                val invItem = inventoryManager.inventory.find { it.invPattern.equals(itemName) || it.id == item.id }
                if (invItem != null && item.quantity > invItem.quantity) {
                    val excessQuantity = item.quantity - invItem.quantity
                    itemsToDeposit.add(item.id to excessQuantity)
                }
            }
        }
        return itemsToDeposit
    }

    fun checkMissingEquipment(missingEquipment: List<SetItem>): List<SetItem> {
        return missingEquipment.filter { findBankItem(it) == null }
    }

    fun getMissingEquipment(missingEquipment: List<SetItem>): Map<SetItem, SpriteItem> {
        return missingEquipment.mapNotNull { item ->
            findBankItem(item)?.let { bankItem ->
                item to bankItem
            } ?: run {
                log.debug("Failed to find item: {}", item)
                null
            }
        }.toMap()
    }

    fun withdrawEquipmentItems(items: Map<SetItem, SpriteItem?>): Boolean {
        log.info("Withdrawing equipment items: {}", items)
        setWithdrawModeToItem()

        items.forEach { (setItem, spriteItem) ->
            spriteItem?.let {
                Bank.withdraw(it, setItem.quantity)
                Execution.delayUntil({ Inventory.contains(it.id) }, 2000)
            }
        }

        log.debug("Every equipment item has been withdrawn from bank!")
        return true
    }

    fun emptyLootingBag(): Boolean {
        if (!Inventory.contains("Looting bag")) return false
        val lootingBag = Inventory.getItems("Looting bag").firstOrNull() ?: run {
            log.debug("Failed to find looting bag in inventory.")
            return false
        }
        lootingBag.interact("View")
        Execution.delayUntil({ LootingBag.isOpen() }, 1200)
        val lootingBagInterface = Interfaces.getAt(15, 6)
        val dismissInterface = Interfaces.getAt(15, 8)

        if (lootingBagInterface == null || dismissInterface == null) {
            log.debug("Failed to find looting bag interfaces.")
            return false
        }

        lootingBagInterface.interact("Deposit loot")
        Execution.delayUntil({ LootingBag.isEmpty() }, 2400)

        dismissInterface.interact("Dismiss")
        Execution.delay(600)
        return true
    }
}
