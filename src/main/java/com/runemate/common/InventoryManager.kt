package com.runemate.common

import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import java.util.regex.Pattern

data class InventoryItem(
    val name: Pattern,
    val id: Int,
    var amount: Int,
    var withdrawRequired: Boolean = true
)
class InventoryManager() {
    var inventory : MutableList<InventoryItem> = mutableListOf()
    val slots = 28

    fun setInventory() {
        val items = Inventory.getItems()
            .filter { it.definition?.name != null }
            .groupBy { it.definition!!.name }
            .map { (name, items) ->
                InventoryItem(Pattern.compile(name), items.first().id, items.sumOf { it.quantity })
            }

        inventory.clear()
        inventory.addAll(items)

        inventory.forEach { item ->
            println("Item: ${item.name} Amount: ${item.amount}")
        }
    }

    fun satisfiedInventoryWithdrawal(): Boolean {
        return inventory.all { item ->
            (!Inventory.contains(item.name) && !Inventory.contains(item.id)) ||
                    (Inventory.getQuantity(item.name) == item.amount && Inventory.getQuantity(item.id) == item.amount)
        }
    }
}