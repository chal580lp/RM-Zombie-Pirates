package com.runemate.zombiepirates

import com.runemate.common.AntiPkManager
import com.runemate.common.BankManager
import com.runemate.common.EquipmentManager
import com.runemate.common.InventoryManager
import com.runemate.common.combat.CombatManager
import com.runemate.common.loot.LootManager


fun Bot.getCombatManager(): CombatManager {
    return this.botComponents.combatManager
}

fun Bot.getLootManager(): LootManager {
    return this.botComponents.lootManager
}

fun Bot.getBankManager(): BankManager {
    return this.botComponents.bankManager
}

fun Bot.getInventoryManager(): InventoryManager {
    return this.botComponents.invManager
}
fun Bot.getEquipmentManager(): EquipmentManager {
    return this.botComponents.equipmentManager
}

fun Bot.getAntiPkManager(): AntiPkManager {
    return this.botComponents.antiPKManager
}