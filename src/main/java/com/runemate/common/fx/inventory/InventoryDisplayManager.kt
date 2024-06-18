package com.runemate.common.fx.inventory

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.bot.BotConfig

import com.runemate.common.item.BaseItem
import com.runemate.common.item.Boost
import com.runemate.common.item.SetItem
import com.runemate.common.settings.AccountSettings
import com.runemate.common.fx.FileManager
import com.runemate.common.framework.core.TaskMachine
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections

class InventoryDisplayManager<TSettings: BotConfig>(val bot: TaskMachine<TSettings>) {

    private val log = getLogger("InventoryDManager")
    private val inventory: MutableList<SetItem> = mutableListOf()

    val requiredStartItems = mutableListOf(
        SetItem("Looting bag", 22586, required = false),
        SetItem("Burning amulet(5)", 21166, required = true),
        SetItem("Ring of dueling(8)", 2552, required = true)
    )
    private val maxCapacity = 28

    val inventoryProperty = SimpleListProperty<BaseItem>(FXCollections.observableArrayList(inventory))

    init {
        requiredStartItems.forEach { addToInventory(it) }
        FileManager.preloadImages(requiredStartItems.map { it.id })
    }
    fun loadInventoryItems(accountSettings: AccountSettings) {
        val inventoryItems = buildList<SetItem> {
            accountSettings.inventoryItems.forEach {
                add(it)
                log.debug("Loaded item: ${it.name} amount: ${it.quantity}")
            }
        }
        if (inventoryItems.sumOf { it.quantity } > 28) {
            log.warn("Last used inventory has more than 28 items? unable to load")
            return
        }
        inventory.clear()
        requiredStartItems.forEach { addToInventory(it) }
        inventoryItems.forEach {
            if (inventory.contains(it)) {
                return@forEach
            }
            addToInventory(it)
        }
    }

    private fun findItemById(id: Int): BaseItem? {
        return inventory.find { it.id == id }
    }

    fun addToInventory(item: BaseItem, fillAll: Boolean = false): Boolean {
        log.debug("Item Name ${item.name}")
        val countToAdd = if (item is SetItem) {
            if (fillAll) maxCapacity - inventory.sumOf { it.quantity }
            else minOf(item.quantity, maxCapacity - inventory.sumOf { it.quantity })
        } else {
            if (fillAll) maxCapacity - inventory.size else 1
        }

        if (countToAdd > 0) {
            if (isBoostItem(item)) {
                removeOtherBoosts(item)
            }
            val existingItem = findItemById(item.id)
            if (existingItem != null && existingItem is SetItem) {
                log.debug("Existing item: $existingItem amount: ${existingItem.quantity}")
                existingItem.quantity += countToAdd
            } else {
                val newItem = if (item is SetItem) {
                    item.copy(quantity = countToAdd)
                } else {
                    SetItem(name = "ERROR",id = item.id, quantity = countToAdd)
                }
                inventory.add(newItem)
            }
            log.info("Added $countToAdd ${item.name}(s) to inventory")
            inventoryProperty.set(FXCollections.observableArrayList(inventory))
            return true
        }
        log.debug("Not enough space to add ${if (fillAll) "all" else countToAdd} ${item.name}(s) to inventory")
        return false
    }
    fun isBoostItem(item: BaseItem): Boolean {
        return Boost.ALL_BOOSTS.any { it.id == item.id }
    }
    fun removeOtherBoosts(currentItem: BaseItem) {
        val boostIds = Boost.ALL_BOOSTS.map { it.id }
        inventory.removeIf {  it.id in boostIds && it.id != currentItem.id }
        Boost.find(currentItem.id)?.let {
            log.debug("Setting boost to ${it.title}")
            //bot.settings().boost = it
        }

    }

    fun removeFromInventory(item: BaseItem, count: Int = 1, removeAll: Boolean = false): Boolean {
        if (item is SetItem && requiredStartItems.contains(item)) {
            log.warn("Cannot remove required item: ${item.name}")
            return false
        }

        val existingItem = findItemById(item.id)
        if (existingItem != null) {
            val countToRemove = if (existingItem is SetItem) {
                if (removeAll) existingItem.quantity else minOf(count, existingItem.quantity)
            } else {
                1
            }

            if (countToRemove > 0) {
                if (existingItem is SetItem) {
                    existingItem.quantity -= countToRemove
                    if (existingItem.quantity <= 0) {
                        inventory.remove(existingItem)
                    }
                } else {
                    inventory.remove(existingItem)
                }
                log.info("Removed $countToRemove ${item.name}(s) from inventory")
                inventoryProperty.set(FXCollections.observableArrayList(inventory))
                return true
            }
        }
        log.debug("Not enough non-required items to remove ${if (removeAll) "all" else count} ${item.name}(s) from inventory")
        return false
    }

    fun getInventory(): List<SetItem> = inventory.toList()
}