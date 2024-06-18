package com.runemate.common

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.item.Loot
import com.runemate.common.item.Restore
import com.runemate.game.api.hybrid.entities.GameObject
import com.runemate.game.api.hybrid.entities.Npc
import com.runemate.game.api.hybrid.entities.Player
import com.runemate.game.api.hybrid.entities.status.OverheadIcon
import com.runemate.game.api.hybrid.entities.status.OverheadIcon.SkullType
import com.runemate.game.api.hybrid.local.Camera
import com.runemate.game.api.hybrid.local.VarpID
import com.runemate.game.api.hybrid.local.Varps
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.game.api.hybrid.local.hud.interfaces.*
import com.runemate.game.api.hybrid.location.navigation.cognizant.ScenePath
import com.runemate.game.api.hybrid.net.GrandExchange
import com.runemate.game.api.hybrid.queries.results.LocatableEntityQueryResults
import com.runemate.game.api.hybrid.region.GameObjects
import com.runemate.game.api.hybrid.region.Npcs
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.osrs.local.hud.interfaces.ControlPanelTab
import com.runemate.game.api.osrs.local.hud.interfaces.Prayer
import com.runemate.game.api.script.Execution

import java.util.concurrent.RejectedExecutionException
import java.util.regex.Pattern
import kotlin.streams.asSequence
import kotlin.system.measureTimeMillis
import kotlin.time.times

object util {
    private val log = getLogger("util")
    var priceMap = mutableMapOf(995 to 1)

    fun equip(item: SpriteItem?): Boolean {
        if (item != null) {
            log.debug("Equipping ${item.definition?.name}")
            item.interact(Inventory.EQUIP_ACTIONS)
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

    fun inCombat(): Boolean {
        val p = Players.getLocal() ?: return false
        val anyTargeting = Npcs.newQuery().filter { n: Npc -> n.target == p }.actions("Attack").results().size > 0
        return anyTargeting
    }

    fun getPlayersAttackingMe(): List<Player> {
        val p = Players.getLocal() ?: return emptyList()
        return Players.newQuery().filter { n -> n.target == p }.results().toList()
    }

    private fun filterGameObjectsByPlane(gameObjects: LocatableEntityQueryResults<GameObject>): LocatableEntityQueryResults<GameObject> {
        val playerPlane = Players.getLocal()?.position?.plane
        val filtered = if (playerPlane != null) {
            gameObjects.filter { it.position?.plane == playerPlane }
        } else {
            log.debug("Player plane is null")
            emptyList()
        }
        return LocatableEntityQueryResults(filtered)
    }

    fun findNearestGameObject(name: String): GameObject? {
        val gameObjects = try {
            GameObjects.newQuery()
                .names(name)
                .filter { it.position?.plane == Players.getLocal()?.position?.plane }
                .results()
        } catch (e: RejectedExecutionException) {
            log.error("findNearestGameObject: Task submission rejected: ${e.message}")
            return null
        }

        val filteredObjects = filterGameObjectsByPlane(gameObjects)
        return filteredObjects.nearest()
    }

    fun findGameObjects(names: List<String>): LocatableEntityQueryResults<GameObject>? {
        val gameObjects = try {
            GameObjects.newQuery()
                .names(*names.toTypedArray())
                .results()
        } catch (e: RejectedExecutionException) {
            log.error("findGameObjects: Task submission rejected: ${e.message}")
            return null
        }

        return filterGameObjectsByPlane(gameObjects)
    }

    private fun findVisibleGameObjects(names: List<String>): LocatableEntityQueryResults<GameObject>? {
        val gameObjects = try {
            GameObjects.newQuery()
                .names(*names.toTypedArray())
                .visible()
                .results()
        } catch (e: RejectedExecutionException) {
            log.error("findVisibleGameObjects: Task submission rejected: ${e.message}")
            return null
        }

        return filterGameObjectsByPlane(gameObjects)
    }

    fun isGameObjectVisible(names: List<String>): Boolean {
        val gameObjects = findVisibleGameObjects(names) ?: return false

        return gameObjects.any()
    }

    fun isBankObjectReachable(): Boolean {
        val bankObjects = GameObjects.newQuery().names("Bank booth", "Bank chest").reachable().results()
        return bankObjects.isNotEmpty()
    }

    fun isBankerReachable(): Boolean {
        val bankers = Npcs.newQuery().names("Banker").results()
        return bankers.isNotEmpty()
    }

    fun isBankObjectVisibleReachable(): Boolean {
        val bankObjects = GameObjects.newQuery().names("Bank booth", "Bank chest").visible().reachable().results()
        return bankObjects.isNotEmpty()
    }

    fun isBankerVisibleReachable(): Boolean {
        val bankers = Npcs.newQuery().names("Banker").visible().results()
        return bankers.isNotEmpty()
    }

    fun isBankReachable(): Boolean {
        return isBankObjectReachable() || isBankerReachable()
    }

    fun isBankVisibleReachable(): Boolean {
        return isBankObjectVisibleReachable() || isBankerVisibleReachable()
    }

    fun walkToNearbyBank(): Boolean {
        if (!isBankReachable()) return false
        val bankCoords = findGameObjects(listOf("Bank booth", "Bank chest"))
            ?.flatMap { it.area?.surroundingCoordinates ?: emptyList() }
            ?.toMutableList() ?: mutableListOf()

        // Find banker NPCs
        val bankerCoords = Npcs.newQuery().names("Banker").results()
            ?.flatMap { it.area?.surroundingCoordinates ?: emptyList() }

        bankerCoords?.let { bankCoords.addAll(it) }

        bankCoords.forEach { coord ->
            val path = ScenePath.buildTo(coord)
            if (path != null) {
                log.debug("Found Bank or Banker, walking to it.")
                path.step()
                return true
            }
        }
        return false
    }

    fun walkToNearbyGE(): Boolean {
        val coords = Npcs.newQuery().names("Grand Exchange Clerk").results()
            ?.flatMap { it.area?.surroundingCoordinates ?: emptyList() }


        coords?.forEach { coord ->
            val path = ScenePath.buildTo(coord)
            if (path != null) {
                log.debug("Found Grand Exchange Clerk, walking to it.")
                path.step()
                return true
            }
        }
        return false
    }

    fun healthIsFull(): Boolean {
        return Health.getCurrent() == Health.getMaximum()
    }

    fun prayerIsFull(): Boolean {
        return Prayer.getPoints() == Prayer.getMaximumPoints()
    }

    fun isItemInInventoryOrEquipment(pattern: Pattern): Boolean {
        return Inventory.getItems(pattern).isNotEmpty() || Equipment.getItems(pattern).isNotEmpty()
    }

    fun isTeleBlocked(): Boolean {
        val varpValue = Varps.getAt(2741).value
        return varpValue != 0
    }

    fun consume(item: SpriteItem?, action: String): Boolean {
        if (item == null) return false
        val def = item.definition ?: return false
        if (def.isNoted) return false
        try {
            log.debug("consuming: ${def.name}")
            val slot = item.index
            val it = Inventory.getItemIn(slot) ?: return true
            if (it.id == item.id) {
                log.debug("clicking: {} slot {}", Inventory.getItemIn(slot)?.definition?.name, slot)
                if (item.interact(action)) {
                    Execution.delay(10, 65)
                    return true
                } else {
                    log.debug("Failed to interact with item {}", Inventory.getItemIn(slot)?.definition?.name)
                    return false
                }
            }
            return false
        } catch (e: Exception) {
            log.error("Consume", e)
            return false
        }
    }

    fun consume(itemId: Int, action: String): Boolean {
        return consume(Inventory.newQuery().ids(itemId).unnoted().results().firstOrNull(), action)
    }

    fun consume(itemName: String, action: String): Boolean {
        return consume(Inventory.newQuery().names(itemName).unnoted().results().firstOrNull(), action)
    }

    fun consume(itemName: Pattern, action: String): Boolean {
        return consume(Inventory.newQuery().names(itemName).unnoted().results().firstOrNull(), action)
    }

    fun restorePrayer(): Boolean {
        val points = Prayer.getPoints()
        for (b in Restore.PRAYER_RESTORES) {
            if (blightedCheck(b.gameName)) {
                if (consume(b.gameName, "Drink")) {
                    Execution.delayUntil({ Prayer.getPoints() > points }, 600)
                    return true
                }
            }
        }
        return false
    }

    fun blightedCheck(itemName: Pattern): Boolean {
        val isBlighted = itemName.toString().contains("Blighted")
        return Wilderness.isInWilderness() || !isBlighted
    }

    fun blightedCheck(name: String): Boolean {
        return Wilderness.isInWilderness() || !name.contains("Blighted", ignoreCase = true)
    }

    fun isAutoRetaliateEnabled(): Boolean {
        return Varps.getAt(VarpID.AUTO_RETALIATE.id).value == 0
    }

    fun toggleAutoRetaliate(): Boolean {
        log.debug("toggling auto retaliate")
        if (!ControlPanelTab.COMBAT_OPTIONS.open()) {
            return false
        }
        val component = Interfaces.newQuery()
            .containers(593)
            .types(InterfaceComponent.Type.CONTAINER)
            .actions("Auto retaliate")
            .grandchildren(false)
            .results()
            .firstOrNull()
        return component != null && component.interact("Auto retaliate")
    }

    fun getPrice(id: Int, itemName: String): Int {
        return priceMap[id] ?: lookupAndCachePrice(id, itemName)
    }

    fun getPrices(ids: List<Int>, itemNames: List<String>): List<Int> {
        require(ids.size == itemNames.size) { "IDs and itemNames lists must have the same size." }

        val prices = mutableListOf<Int>()
        measureTimeMillis {
            ids.forEachIndexed { index, id ->
                val price = getPrice(id, itemNames[index])
                prices.add(price)
            }
        }
        return prices
    }

    private fun lookupAndCachePrice(id: Int, itemName: String): Int {
        val gePrice = GrandExchange.lookup(id)?.price
        return if (gePrice != null) {
            //log.debug("Determined price of $itemName to be $gePrice")
            priceMap[id] = gePrice
            gePrice
        } else {
            log.debug("$itemName returned an error when fetching the price, setting price to 1 gp")
            priceMap[id] = 1
            1
        }
    }
    fun createPatternFromItemName(itemName: String): Pattern {
        //log.debug("Item name: '{}'", itemName)
        val digitsEscaped = itemName.replace(Regex("\\d+"), "\\\\d+")
        //log.debug("String: '{}' ", digitsEscaped)
        val parenthesesEscaped = digitsEscaped.replace("(", "\\(").replace(")", "\\)")
        //log.debug("Creating pattern from string: '{}'", parenthesesEscaped)
        return Pattern.compile(parenthesesEscaped)
    }
    fun isNumbered(itemName: String): Boolean {
        return itemName.contains(Regex("\\d+"))
    }

    fun setCamera(): Boolean {
        if (Camera.isZoomLocked()) return false
        if (Camera.getZoom() == 0.0) return false
        Camera.setZoom(0.0, 0.0)
        return true
    }
    fun calculateInventoryLootValue(): Int {
        kotlin.runCatching {
            Inventory.newQuery()
                .names(*Loot.gameNames.toTypedArray())
                .results()
                .sumOf { lootItem ->
                    lootItem.quantity.times(lootItem.definition?.let { getPrice(it.unnotedId, it.name) } ?: 0)
                }
        }.onSuccess { return it } .onFailure { log.warn("Failed to calculate inventory loot value", it)}
        return 0
    }

}