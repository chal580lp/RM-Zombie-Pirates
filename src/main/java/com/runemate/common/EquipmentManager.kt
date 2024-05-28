package com.runemate.common

import com.runemate.game.api.hybrid.local.hud.interfaces.Equipment
import java.util.regex.Pattern

data class EquipmentItem(
    val name: Pattern,
    val id: Int,
    val tradeable: Boolean
)

class EquipmentManager {
    val equipment: MutableList<EquipmentItem> = mutableListOf()

    fun setEquipment() {
        val items = Equipment.getItems()
            .filter { it.definition?.name != null }
            .groupBy { it.definition!!.name }
            .map { (name, items) ->
                val regexName = if (name.contains("\\(\\d+\\)".toRegex())) {
                    name.replace("\\(\\d+\\)".toRegex(), "\\(.*\\)")
                } else {
                    name
                }
                EquipmentItem(Pattern.compile(regexName), items.first().id, items.first().definition?.isTradeable == true)
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
        return ChargedItem.fromItemName(item.name.pattern()) != null
    }
}