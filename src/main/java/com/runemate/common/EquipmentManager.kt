package com.runemate.common

import com.runemate.game.api.hybrid.local.hud.interfaces.Equipment

data class EquipmentItem(
    val name : String,
    val id : Int,
    val tradeable : Boolean
)
class EquipmentManager {
    val equipment : MutableList<EquipmentItem> = mutableListOf()
    val slots = 14

    fun setEquipment() {
        val items = Equipment.getItems()
            .filter { it.definition?.name != null }
            .groupBy { it.definition!!.name }
            .map { (name, items) ->
                EquipmentItem(name, items.first().id, items.first().definition?.isTradeable == true)
            }

        equipment.clear()
        equipment.addAll(items)
    }

    fun satisfiedEquipmentWithdrawal(): Boolean {
        return equipment.all { item ->
            (Equipment.contains(item.name) || Equipment.contains(item.id))
        }
    }
    fun isChargedItem(item: EquipmentItem): Boolean {
        return ChargedItem.fromItemName(item.name) != null
    }
}