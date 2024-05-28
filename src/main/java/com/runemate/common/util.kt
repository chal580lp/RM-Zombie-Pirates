package com.runemate.common

import com.runemate.game.api.hybrid.entities.GameObject
import com.runemate.game.api.hybrid.entities.GroundItem
import com.runemate.game.api.hybrid.entities.Npc
import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.local.Varps
import com.runemate.game.api.hybrid.local.hud.interfaces.Equipment
import com.runemate.game.api.hybrid.local.hud.interfaces.Health
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem
import com.runemate.game.api.hybrid.region.GameObjects
import com.runemate.game.api.hybrid.region.Npcs
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.osrs.local.hud.interfaces.Prayer
import com.runemate.game.api.script.Execution
import java.util.regex.Pattern

object util {
    private val log: RMLogger = RMLogger.getLogger(this::class.java)

    fun takeDI(gi: GroundItem?) {
        if (gi == null || !gi.isValid) {
            println("null / invalid in take")
            return
        }
        println(String.format("looting: %s", gi.definition!!.name))
        DI.send(MenuAction.forGroundItem(gi, "Take"))
    }

    fun equip(item: SpriteItem?): Boolean {
        if (item != null) {
            log.debug("Equipping ${item.definition?.name}")
            item.interact("Wear")
            if (!Execution.delayUntil({ Equipment.contains(item.id) }, 1100)) {
                log.debug("Failed to equip ${item.definition?.name}")
                return false
            }
        } else {
            log.debug("Trying to equip NULL item is not in the inventory.")
            return false
        }
        return true
    }

    fun equip(p: String?): Boolean {
        val item = Inventory.getItems(p).first()
        return equip(item)
    }

    fun equip(p: Pattern?): Boolean {
        val item = Inventory.getItems(p).first()
        return equip(item)
    }
    fun inventoryEquipmentSource(): List<SpriteItem> {
        val inventory = Inventory.getItems()
        val equ = Equipment.getItems()
        val h = ArrayList(inventory)
        h.addAll(equ)
        return h
    }

    fun isInCombat(): Boolean {
        val p = Players.getLocal() ?: return false
        val anyTargeting = Npcs.newQuery().filter { n: Npc -> n.target == p }.actions("Attack").results().size > 0
        return anyTargeting
    }

    fun findNearestGameObject(name: String): GameObject? {
        return GameObjects.newQuery().names(name).results().nearest()
    }
    fun isGameObjectVisible(names: List<String>): Boolean {
        return GameObjects.newQuery()
            .names(*names.toTypedArray())
            .visible()
            .results()
            .any()
    }
    fun healthIsFull(): Boolean {
        return Health.getCurrent() == Health.getMaximum()
    }
    fun prayerIsFull(): Boolean {
        return Prayer.getPoints() == Prayer.getMaximumPoints()
    }

    fun isItemInInventoryOrEquipment(pattern: Pattern) : Boolean {
        return Inventory.getItems(pattern).isNotEmpty() || Equipment.getItems(pattern).isNotEmpty()
    }

    fun isTeleBlocked(): Boolean {
        val varpValue = Varps.getAt(2741).value
        return varpValue != 0
    }



//    fun restorePrayer(): Boolean {
//        items.prayerRestores.forEach { b ->
//            if (consume(b, "Drink")) {
//                return true
//            }
//        }
//        return false
//    }

}