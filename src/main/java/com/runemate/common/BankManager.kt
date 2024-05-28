package com.runemate.common

import com.runemate.common.state.di.injected
import com.runemate.game.api.hybrid.entities.Item
import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.local.hud.interfaces.*
import com.runemate.game.api.hybrid.region.GameObjects
import com.runemate.game.api.osrs.local.hud.interfaces.LootingBag
import com.runemate.game.api.script.Execution
import com.runemate.game.api.script.framework.listeners.events.MenuInteractionEvent
import com.runemate.zombiepirates.Bot
import java.util.logging.Logger
import java.util.regex.Pattern


class BankManager() {

    fun needToBank(): Boolean {

        return false
    }

    val bot : Bot by injected()

    private val log: RMLogger = RMLogger.getLogger(this::class.java)

    fun withdrawInventory(items: List<InventoryItem>): Boolean {
        log.info("Withdrawing items: ${items.size}")

        items.forEach { item ->
            val name = item.name

            log.debug("Checking inventory and equipment for item: ${name.pattern()}")

            // Check if the item is in the inventory
            val inInventory = Inventory.contains(name)
            // Check if the item is in the equipment
            val inEquipment = Equipment.contains(name)
            // Get the quantity of the item in the inventory
            val quantityInInventory = Inventory.getQuantity(name)

            log.debug("Item ${name.pattern()} is in inventory: $inInventory")
            log.debug("Item ${name.pattern()} is in equipment: $inEquipment")
            log.debug("Item ${name.pattern()} quantity in inventory: $quantityInInventory")

            // Determine if we need to withdraw the item
            val needsWithdrawal = !inInventory && !inEquipment || (item.amount > 1 && quantityInInventory < item.amount)

            if (needsWithdrawal) {
                // Find the bank item, considering numbered items
                val bankItem = findBankItem(item)

                // If no bank item is found, handle the failure case
                if (bankItem == null) {
                    if (!item.withdrawRequired) {
                        log.info("Can't find ${name.pattern()} but it is not required.")
                        return@forEach
                    }
                    log.info("Failed to find ${name.pattern()} in bank.")
                    bot.pause("Failed to find ${name.pattern()} in bank.")
                    return false
                }

                // Calculate the amount to withdraw
                val amountToWithdraw = if (item.amount > 1) item.amount - quantityInInventory else 1

                // Attempt to withdraw the item from the bank
                log.debug("Withdrawing ${bankItem.definition?.name} AMOUNT: $amountToWithdraw")
                Bank.withdraw(bankItem, amountToWithdraw)

                // Verify the item was successfully withdrawn
                val itemWithdrawn = Execution.delayUntil({
                    Inventory.contains(name) && Inventory.getQuantity(name) >= item.amount
                }, 1200)

                if (!itemWithdrawn) {
                    log.debug("Failed to withdraw ${name.pattern()}, attempting once more")
                    // If the first attempt fails, try withdrawing again
                    if (!Bank.withdraw(bankItem, amountToWithdraw)) return false
                }
            }
        }
        return true
    }


    private fun findBankItem(item: InventoryItem): SpriteItem? {
        // Handle items with patterns like "Blighted super restore.*\\(.*\\)"
        if (isNumberedItem(item.name)) {
            // Query the bank for items matching the pattern directly from item.name
            val matchingItems = Bank.newQuery().names(item.name).results().toList() // Convert to List
            val highestOrLowestNumberedItem = findHighestOrLowestNumberedItem(matchingItems, item.name)
            if (highestOrLowestNumberedItem != null) {
                return highestOrLowestNumberedItem
            }
        }

        // Query the bank for the item by ID DEBUG PURPOSES
        val tempById = Bank.newQuery().ids(item.id).results().firstOrNull().also {
            if (it == null) log.debug("Failed to find ${item.name} in bank with ID query.")
        }

        // Query the bank for the item by name DEBUG PURPOSES
        val tempByName = Bank.newQuery().names(item.name).placeholder(false).results().firstOrNull().also {
            if (it == null) log.debug("Failed to find ${item.name.pattern()} in bank with Name query.")
        }

        // Return the bank item found by ID or name
        return tempById ?: tempByName
    }

    private fun findHighestOrLowestNumberedItem(items: List<SpriteItem>, pattern: Pattern, findHighest: Boolean = true): SpriteItem? {
        val matchingItems = items.filter {
            it.definition?.name?.let { name -> pattern.matcher(name).matches() } == true
        }

        // Log all matching items for debugging
        matchingItems.forEach { spriteItem ->
            log.debug("Matching item found: ${spriteItem.definition?.name}")
        }

        if (matchingItems.isNotEmpty()) {
            val highestOrLowestNumberedItem = if (findHighest) {
                // Select the item with the highest number in parentheses
                matchingItems.maxByOrNull { spriteItem ->
                    val matchResult = "\\((\\d+)\\)".toRegex().find(spriteItem.definition?.name ?: "")
                    matchResult?.groupValues?.get(1)?.toInt() ?: 0
                }
            } else {
                // Select the item with the lowest number in parentheses
                matchingItems.minByOrNull { spriteItem ->
                    val matchResult = "\\((\\d+)\\)".toRegex().find(spriteItem.definition?.name ?: "")
                    matchResult?.groupValues?.get(1)?.toInt() ?: 0
                }
            }

            if (highestOrLowestNumberedItem != null) {
                log.debug("Selected ${if (findHighest) "highest" else "lowest"} numbered item: ${highestOrLowestNumberedItem.definition?.name}")
                return highestOrLowestNumberedItem
            }
            log.debug("Failed to find ${if (findHighest) "highest" else "lowest"} numbered item for pattern: $pattern")
        } else {
            log.debug("Failed to find any items matching pattern: $pattern")
        }
        return null
    }

    private fun isNumberedItem(itemName: Pattern): Boolean {
        // Check if the item name matches a pattern with numbers in parentheses
        return Pattern.compile(".*\\(.*\\).*").matcher(itemName.pattern()).find()
    }


    private fun checkWithdrawalSuccess(inventoryManager: InventoryManager): Boolean {
        return inventoryManager.inventory.all { item ->
            Inventory.contains(item.name) && Inventory.getQuantity(item.name) == item.amount
        }
    }

    fun shouldDepositInventory(inventoryManager: InventoryManager): Boolean {
        val requiredItems = inventoryManager.inventory.map { it.name to it.id }.toSet()

        // Check if there are items in the inventory that shouldn't be there
        Inventory.getItems().forEach { item ->
            val nameMatches = requiredItems.any { (name, _) -> name.matcher(item.definition?.name ?: "").matches() }
            val idMatches = requiredItems.any { (_, id) -> id == item.id }

            if (!nameMatches && !idMatches) {
                log.debug("Item ${item.definition?.name} with ID ${item.id} shouldn't be in the inventory.")
                return true
            }
        }

        // Check if the required items are present in excess quantities
        inventoryManager.inventory.forEach { requiredItem ->
            if (!requiredItem.withdrawRequired) {
                return@forEach
            }

            val matchingItemsByName = Inventory.getItems().filter { it.definition?.name?.let { name -> requiredItem.name.matcher(name).matches() } == true }
            val matchingItemsById = Inventory.getItems().filter { it.id == requiredItem.id }
            val totalQuantity = matchingItemsByName.sumOf { it.quantity } + matchingItemsById.sumOf { it.quantity }

            // If the total quantity is more than required, return true
            if (totalQuantity > requiredItem.amount) {
                return true
            }
        }
        return false
    }

    fun checkAndEquipItems(equipmentManager: EquipmentManager): Boolean {
        val missingItems = equipmentManager.equipment.filterNot { Equipment.contains(it.name) }

        if (missingItems.isEmpty()) {
            log.debug("All equipment items are already equipped.")
            return true
        }

        if (!Bank.open()) {
            log.debug("Failed to open bank.")
            return false
        }

        // Convert missing EquipmentItems to InventoryItems
        val inventoryItemsToWithdraw = missingItems.map {
            InventoryItem(it.name, it.id, 1, withdrawRequired = true)
        }

        // Use withdrawInventory to withdraw the missing equipment items
        if (!withdrawInventory(inventoryItemsToWithdraw)) {
            log.debug("Failed to withdraw some equipment items.")
            return false
        }

        missingItems.forEach { item ->
            val inventoryItem = Inventory.newQuery().names(item.name).results().firstOrNull()
            util.equip(inventoryItem)
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
        val lootingBag = Inventory.getItems("Looting bag").firstOrNull() ?: run {
            log.debug("Failed to find looting bag in inventory.")
            return false
        }
        lootingBag.interact("View")
        Execution.delayUntil({ LootingBag.isOpen() }, 1200)
        val x = Interfaces.getAt(15,6) ?: run {
            log.debug("Failed to find looting bag Deposit loot interface.")
            return false
        }
        //DI.send(MenuAction.forInterfaceComponent(x,"Deposit loot"))
        x.interact("Deposit loot")
        Execution.delayUntil({ LootingBag.isEmpty()}, 1200)
        val y = Interfaces.getAt(15,8) ?: run {
            log.debug("Failed to find looting bag Deposit loot interface.")
            return false
        }
        y.interact("Dismiss")
        Execution.delay(600)
        return true
    }
}