package com.runemate.common

import com.runemate.game.api.hybrid.entities.GameObject
import com.runemate.game.api.hybrid.entities.GroundItem
import com.runemate.game.api.hybrid.entities.Npc
import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.local.hud.interfaces.Equipment
import com.runemate.game.api.hybrid.local.hud.interfaces.Health
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem
import com.runemate.game.api.hybrid.region.GameObjects
import com.runemate.game.api.hybrid.region.Npcs
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.osrs.local.hud.interfaces.Prayer
import java.util.regex.Pattern

object util {

    fun take(gi: GroundItem?) {
        if (gi == null || !gi.isValid) {
            println("null / invalid in take")
            return
        }
        println(String.format("looting: %s", gi.definition!!.name))
        DI.send(MenuAction.forGroundItem(gi, "Take"))
    }

    fun equip(item: SpriteItem?): Boolean {
        if (item == null) return false
        return DI.send(MenuAction.forSpriteItem(item, Inventory.EQUIP_ACTIONS))
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
        val hasHealthBar = p.healthGauge != null
        if (hasHealthBar) return true
        val anyTargeting = Npcs.newQuery().filter { n: Npc -> n.target == p }.actions("Attack").results().size > 0
        return anyTargeting
    }

    fun isEquippable(item: SpriteItem?): Boolean {
        if (item == null) return false
        val def = item.definition ?: return false
        return def.inventoryActions.stream().anyMatch { s: String? ->
            Inventory.EQUIP_ACTIONS.matcher(
                s
            ).find()
        }
    }

    fun findNearestGameObject(name: String): GameObject? {
        return GameObjects.newQuery().names(name).results().nearest()
    }
    fun isGameObjectVisible(names: List<String>): Boolean {
        return !GameObjects.newQuery()
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

//    fun restorePrayer(): Boolean {
//        items.prayerRestores.forEach { b ->
//            if (consume(b, "Drink")) {
//                return true
//            }
//        }
//        return false
//    }

}