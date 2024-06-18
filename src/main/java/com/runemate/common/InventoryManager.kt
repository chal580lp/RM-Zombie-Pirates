package com.runemate.common

import com.runemate.common.item.*


class InventoryManager() {
    var inventory: MutableList<SetItem> = mutableListOf()

    companion object {
        val unRequiredItems = listOf("Looting bag")
    }

    fun inventoryContains(itemName: String): Boolean {
        return inventory.any { it.name == itemName }
    }

    fun updateInventory(baseItems: List<BaseItem>) {
        val updatedInventory = mutableListOf<SetItem>()
        baseItems.forEach { baseItem ->
            if (baseItem is SetItem) {
                val existingItem = inventory.find { it.id == baseItem.id }
                if (existingItem != null) {
                    existingItem.quantity = baseItem.quantity
                    updatedInventory.add(existingItem)
                } else {
                    updatedInventory.add(baseItem)
                }
            }
        }
        inventory = updatedInventory
        inventory.forEach { item ->
            println("Item: ${item.name} Amount: ${item.quantity}")
        }
    }

    private fun getFood(): Map<Food, Int> {
        val foodMap = mutableMapOf<Food, Int>()
        inventory.forEach { item ->
            Food.ALL_FOODS.find { it.id == item.id }?.let { food ->
                foodMap[food] = foodMap.getOrDefault(food, 0) + item.quantity
            }
        }
        return foodMap
    }
    fun getFoodAmount(): Int {
        return getFood().values.sum()
    }

    private fun getRestores(): Map<Restore, Int> {
        val restoreMap = mutableMapOf<Restore, Int>()
        inventory.forEach { item ->
            Restore.PRAYER_RESTORES.find { it.id == item.id }?.let { restore ->
                restoreMap[restore] = restoreMap.getOrDefault(restore, 0) + item.quantity
            }
        }
        return restoreMap
    }
    fun getRestoreAmount(): Int {
        return getRestores().values.sum()
    }

//    fun getBuyString(item: SetItem): String {
//        val name = when {
//            Food.ALL_FOODS.any { it.id == item.id } -> Food.ALL_FOODS.find { it.id == item.id }?.gameName
//            Restore.ALL_RESTORES.any { it.id == item.id } -> Restore.ALL_RESTORES.find { it.id == item.id }?.title
//            Boost.ALL_BOOSTS.any { it.id == item.id } -> Boost.ALL_BOOSTS.find { it.id == item.id }?.title
//            else -> item.name
//        }
//        return name ?: item.name
//    }

}