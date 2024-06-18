package com.runemate.common.framework.core.bot

import com.runemate.common.*
import com.runemate.common.fx.bank.BankDisplayManager
import com.runemate.common.fx.inventory.InventoryDisplayManager
import com.runemate.common.LootManager
import com.runemate.common.framework.core.TaskMachine

class BotComponents<TSettings : BotConfig>(
    bot: TaskMachine<TSettings>
) {
    val combatManager = CombatManager(bot)
    val lootManager = LootManager(bot)
    val bankManager = BankManager(bot)
    val invManager = InventoryManager()
    val equipmentManager = EquipmentManager()
    val bankDManager = BankDisplayManager(bot)
    val invDManager = InventoryDisplayManager(bot)

}