package com.runemate.common

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.WarningAndPause
import com.runemate.common.framework.core.publish
import com.runemate.common.item.*
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank
import com.runemate.game.api.hybrid.local.hud.interfaces.Equipment
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem
import com.runemate.game.api.script.Execution

class EquipmentManager {
    private val log = getLogger("EquipmentManager")
    private val equipment: MutableList<EquipmentSetItem> = mutableListOf()
    private val originalEquipment: MutableList<SetItem> = mutableListOf()
    private val slotPrices: Map<EquipmentSlot, Int> = mapOf(
        EquipmentSlot.HEAD to 100_000,
        EquipmentSlot.CAPE to 100_000,
        EquipmentSlot.NECK to 100_000,
        EquipmentSlot.SHIELD to 100_000,
        EquipmentSlot.BODY to 100_000,
        EquipmentSlot.LEGS to 100_000,
        EquipmentSlot.HANDS to 100_000,
        EquipmentSlot.FEET to 100_000,
        EquipmentSlot.RING to 100_000,
        EquipmentSlot.AMMO to 100_000
    )
    private val testEquipment: List<EquipmentSetItem> = listOf(
        EquipmentSetItem(SetItem("Berserker helm", 3751	, 1), EquipmentSlot.HEAD,""),

        EquipmentSetItem(SetItem("Monk's robe top", 544, 1), EquipmentSlot.RING,""),

        EquipmentSetItem(SetItem("Monk's robe", 542, 1), EquipmentSlot.RING,""),

        EquipmentSetItem(SetItem("Dragon scimitar", 4587, 1), EquipmentSlot.RING,""),

        EquipmentSetItem(SetItem("Salve amulet", 4081, 1), EquipmentSlot.RING,""),

        EquipmentSetItem(SetItem("Ring of wealth (4)", 2572, 1), EquipmentSlot.RING,""),

        )

    //TODO: update this to return amount for future proofing GE
    fun equipmentContains(itemName: String): Boolean {
        return equipment.any { it.name == itemName }
    }
    fun setEquipment() {
        val items = Equipment.getItems()
            .filter { it.definition?.name != null }
            .groupBy { it.definition!!.name }
            .map { (name, items) ->
                EquipmentSetItem(SetItem(name, items.first().id, items.first().quantity),EquipmentSlot.NECK,"")
            }

        equipment.clear()
        equipment.addAll(testEquipment)
        log.debug(equipment.toString())
        if (equipment.isEmpty()) {
            publish(WarningAndPause("No equipment found"))
        }
    }

    fun satisfiedEquipmentWithdrawal(): Boolean {
        return equipment.all { item ->
            (Equipment.contains(item.name) || Equipment.contains(item.id))
        }
    }

    fun equipItems(items: List<SetItem>): Boolean {
            items.forEach { item ->
                Inventory.newQuery().names(item.invPattern).results().firstOrNull().let {
                    if (it == null) {
                        log.debug("Failed to find item {} in inventory using pattern {}", item.name, item.invPattern)
                        return@forEach
                    }
                    util.equip(it)
                    Execution.delayUntil({ Equipment.contains(item.name) }, 600, 1200)
                }
            }
            if (items.all { Equipment.contains(it.id) }) {
                log.debug("Successfully equipped all items")
                return true // Exit and return true if all items are successfully equipped
            }
        // TODO: Pause the bot if this fails?
//        publish(WarningAndPause("Failed to equip all items after 3 attempts"))
        return false
    }
    fun shouldActivateGETask(missingItems: List<SetItem>): Boolean {
        missingItems.forEach{ item ->
            if (!item.required) return@forEach
            val price = util.getPrice(item.id,item.name)
            if (price > 100_000 || price == 0) return@forEach
            return true
        }
        return false
    }

//    fun updateEquipmentWithFallback(playerLevels: Map<Skill, Int>) {
//        equipment.forEach { item ->
//            val slot = getSlotForItem(item.name) ?: return@forEach
//            val fallbackItem = getFallbackItem(slot, playerLevels)
//            if (fallbackItem != null && fallbackItem.name != item.name) {
//                if (Inventory.contains(fallbackItem.name) || Equipment.contains(fallbackItem.name)) {
//                    // Swap to the fallback item
//                    log.debug("Swapping ${item.name} with fallback item ${fallbackItem.name}")
//                    equipment.remove(item)
//                    equipment.add(fallbackItem)
//                } else {
//                    // Add the fallback item to the list of items to buy
//                    log.debug("Adding fallback item ${fallbackItem.name} to the list of items to buy")
//                    equipment.add(fallbackItem)
//                }
//            }
//        }
//    }

    fun getMissingEquipment(): List<SetItem> {
        val missingItems = equipment.filterNot { item ->
            Equipment.contains(item.setItem.invPattern) || Inventory.contains(item.setItem.invPattern)
        }

        if (missingItems.isEmpty()) {
            log.debug("All equipment items are already present in equipment or inventory. find")
            return emptyList()
        }
        return missingItems.toSetItemList()
    }

    fun checkOriginalEquipment() {
        originalEquipment.forEach { item ->
            if (Inventory.contains(item.name) || Equipment.contains(item.name)) {
                // Swap back to the original item
                log.debug("Swapping back to original item ${item.name}")
                equipment.removeIf { it.name == item.name }
                //equipment.add(item)
            }
        }
    }
    fun equipWithdrawnItems(): Boolean {
        val itemsToEquip = equipment.filter { item ->
            Inventory.contains(item.setItem.invPattern)
        }
        if (itemsToEquip.isEmpty()) return true
        return equipItems(itemsToEquip.toSetItemList())
    }

//    private fun getSlotForItem(itemName: String): EquipmentSlot {
//        return Armor.entries.find { it.gameName == itemName }?.equipmentSlot ?: EquipmentSlot.HEAD
//    }
//
//    private fun getFallbackItem(slot: EquipmentSlot, playerLevels: Map<Skill, Int>): EquipmentSetItem? {
//        val fallbackItems = Armor.entries.filter { armor ->
//            armor.equipmentSlot == slot &&
//                    armor.requiredStats.all { (skill, level) -> playerLevels[skill] ?: 0 >= level }
//        }
//        return fallbackItems.maxByOrNull { armor ->
//            armor.requiredStats.values.sum()
//        }?.let { EquipmentSetItem(SetItem(it.gameName, it.id, 1),EquipmentSlot.AMMO,"S") }
//    }
}