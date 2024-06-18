package com.runemate.common

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.framework.core.TaskMachine
import com.runemate.common.framework.core.addons.BotState
import com.runemate.common.item.Food
import com.runemate.common.item.name
import com.runemate.game.api.hybrid.entities.GroundItem
import com.runemate.game.api.hybrid.local.Camera
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.hybrid.location.Area
import com.runemate.game.api.hybrid.queries.GroundItemQueryBuilder
import com.runemate.game.api.hybrid.region.GroundItems
import com.runemate.game.api.osrs.local.hud.interfaces.LootingBag
import com.runemate.game.api.script.Execution
import com.runemate.ui.DefaultUI

data class LootItem(
    val itemName: String,
    val unnotedId: Int,
    val quantity: Int
)
class LootManager<TSettings : BotConfig>(private val bot: TaskMachine<TSettings>) {

    private val lootArea: Area
        get() = bot.settings().combatArea

    private val log = getLogger("LootManager")
    private var lootOnlyMyKills: Boolean = true // New variable to indicate if only own kills should be looted

    private var totalLoot = mutableMapOf<Int, LootItem>()
    private var tripLoot = mutableMapOf<Int, LootItem>()
    var validatedTripLoot : Boolean = false
    private val itemsToCheckPrices = mutableSetOf<Pair<Int, String>>() // Collect IDs and names that need price checks


    fun addLoot(loot: GroundItem) {
        if (!isPriceValid(loot)) return
        log.debug("Adding loot: ${loot.definition?.name}")
        loot.definition?.unnotedId?.let { unnotedId ->
            val newItem = LootItem(
                itemName = loot.definition?.name ?: "Null: $unnotedId",
                unnotedId = unnotedId,
                quantity = loot.quantity
            )

            tripLoot.merge(unnotedId, newItem) { oldItem, newItem2 ->
                oldItem.copy(quantity = oldItem.quantity + newItem2.quantity)
            }

            val newLoot = mutableMapOf(unnotedId to newItem)
            totalLoot.putAll(newLoot)
        }
    }
    fun validateTripLoot() : Boolean  {
        val invLoot = Inventory.getItems().filter { invItem -> tripLoot.any { it.value.itemName == invItem.name } }.toMutableList()
        if (Inventory.contains("Looting bag")) {
            val lootingBag = Inventory.getItems("Looting bag").firstOrNull() ?: return false
            lootingBag.interact("Open")
            Execution.delayUntil({ !LootingBag.isEmpty() }, 600)
            val lootingBagItems = LootingBag.getItems()
            if (lootingBagItems.isEmpty()) return false
            invLoot.addAll(lootingBagItems)
        }
        val invLootValue = invLoot.sumOf { it.quantity * (util.priceMap[it.id] ?: 0) }
        if (invLootValue > tripLootValue()) {
            log.debug("Trip loot value is less than inventory loot value")
        } else if (invLootValue < tripLootValue()) {
            log.debug("Trip loot value is greater than inventory loot value")
        } else {
            log.debug("Trip loot value is equal to inventory loot value")
        }
        tripLoot = invLoot.map { LootItem(it.name ?: "Null", it.id, it.quantity) }.associateBy { it.unnotedId }.toMutableMap()
        if (invLootValue > bot.settings().minTripValue) return true
        return false
    }
    fun tripLootValue(): Int {
        return tripLoot.values.sumOf { lootItem ->
            lootItem.quantity * (util.priceMap[lootItem.unnotedId] ?: 0)
        }
    }

    fun clearTripLoot() {
        log.debug("Clearing trip loot")
        tripLoot.clear()
    }

    val shouldLoot
        get() =
            isAnyLoot()

    private val lootQuery: GroundItemQueryBuilder
        get() =
            GroundItems.newQuery()
                .within(lootArea)
                .reachable()
                .filter { !lootOnlyMyKills || it.ownership != GroundItem.Ownership.GROUP && it.ownership != GroundItem.Ownership.OTHER}
                .filter { isPriceValid(it) }

    private fun isPriceValid(item: GroundItem): Boolean {
        if (item.definition?.name == "Looting bag") return true
        val unnotedId = item.definition?.unnotedId ?: return false

        if (bot.settings().minLootValue == 0) {
            return true
        }

        // Check and use cached price
        val cachedPrice = util.priceMap[unnotedId]
        if (cachedPrice != null) {
            return cachedPrice * item.quantity >= bot.settings().minLootValue
        }

        // Collect IDs and names for batch price lookup
        itemsToCheckPrices.add(unnotedId to item.definition!!.name)

        // Temporarily return false until prices are fetched
        return false
    }
    private fun performBatchPriceLookup() {
        if (itemsToCheckPrices.isNotEmpty()) {
            val (ids, itemNames) = itemsToCheckPrices.unzip()
            val prices = util.getPrices(ids, itemNames)
            prices.forEachIndexed { index, price ->
                util.priceMap[ids[index]] = price
            }
            itemsToCheckPrices.clear() // Clear after fetching prices
        }
    }


    fun manageLooting(): List<LootItem> {
        if (!shouldLoot) return emptyList()
        val loot = loot()
        log.debug("Trip Loot Value: {}k",tripLootValue() / 1000)
        return loot
    }

    private fun isAnyLoot() = lootQuery.results().any()

    private fun getAllLoot() = lootQuery.results().toList()

    private fun foodOrJunk(): Boolean {
        return removeFoodOrJunk()
    }
    private fun removeFoodOrJunk() : Boolean {
        return Food.eatAny()
    }

    private fun hasLootingBag() : Boolean {
        return Inventory.contains("Looting bag")
    }
    private fun isLootingBagClosed() : Boolean {
        return Inventory.contains(11941)
    }
    private fun openLootingBag() {
        log.debug("Opening looting bag")
        val lootingBag = Inventory.getItems(11941).first() ?: return
        //DI.send(MenuAction.forSpriteItem(lootingBag, "Open"))
        lootingBag.interact("Open")
        Execution.delayUntil({ lootingBag.id != 11941}, 600)
    }

    private fun loot(): List<LootItem> {
        performBatchPriceLookup()
        val lootItems = getAllLoot()
        if (lootItems.isEmpty() || (Inventory.isFull() && !foodOrJunk())) return emptyList()

        log.debug("Looting ${lootItems.joinToString ( ", " ) { (it.definition?.name + " " + it.ownership)}}")

        val lootedItems = mutableListOf<LootItem>()
        DefaultUI.setStatus("Attempting to loot")
        for (item: GroundItem in lootItems) {
            if (hasLootingBag() && isLootingBagClosed()) {
                openLootingBag()
            }
            if (Inventory.isFull()) {
                if (!foodOrJunk())  {
                    log.warn("Inventory is full")
                    bot.setCurrentState(BotState.RetreatState)
                    break
                }
                removeFoodOrJunk()
            }

            if (!item.isVisible) {
                Camera.turnTo(item)
                Execution.delayUntil({ item.isVisible }, 1000);
            }
            if (!item.take()) continue
            Execution.delayUntil( { !item.isValid }, 2000 )
            if (!item.isValid) {
                val lootItem = LootItem(
                    itemName = item.definition?.name ?: "Null" ,
                    unnotedId = item.definition?.unnotedId ?: 995,
                    quantity = item.quantity,
                )
                lootedItems.add(lootItem)
            }
        }
        return lootedItems
    }

}