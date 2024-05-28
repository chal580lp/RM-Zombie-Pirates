package com.runemate.zombiepirates

import com.runemate.common.*
import com.runemate.common.combat.CombatManager
import com.runemate.common.loot.LootManager
import com.runemate.game.api.hybrid.location.Area
import com.runemate.game.api.hybrid.util.Regex
import java.util.regex.Pattern

class BotComponents(
    combatArea: Area,
    npcNames: List<String>
) {
    val combatManager = CombatManager(combatArea, npcNames)
    val lootManager = LootManager(combatArea)
    val bankManager = BankManager()
    val invManager = InventoryManager()
    val antiPKManager = AntiPkManager()
    val equipmentManager = EquipmentManager()

}