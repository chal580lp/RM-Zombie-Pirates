package com.runemate.common.loot

import com.runemate.common.DI
import com.runemate.common.RMLogger
import com.runemate.game.api.hybrid.entities.GroundItem
import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.input.direct.MenuOpcode
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.hybrid.location.Area
import com.runemate.game.api.hybrid.net.GrandExchange
import com.runemate.game.api.hybrid.queries.GroundItemQueryBuilder
import com.runemate.game.api.hybrid.region.GroundItems
import com.runemate.game.api.osrs.local.hud.interfaces.LootingBag
import com.runemate.game.api.script.Execution
import com.runemate.ui.DefaultUI

data class LootItem(
    val itemId: Int,
    val itemName: String,
    val stack: Int
)
class LootManager(private val lootArea: Area) {

    private val log: RMLogger = RMLogger.getLogger(this::class.java)

    private var priceMap = mutableMapOf(995 to 1)
    private var minPrice = 4000

    val shouldLoot
        get() =
            isAnyLoot()

    private val lootQuery: GroundItemQueryBuilder
        get() =
            GroundItems.newQuery()
                .within(lootArea)
                .reachable()
                .filter { isPriceValid(it) }
    private fun isPriceValid(item: GroundItem): Boolean {
        if (item.definition?.name == "Looting bag") return true

        val minPrice = minPrice
        val unnotedId = item.definition?.unnotedId ?: return false

        if (minPrice == 0) {
            return true
        }
        log.debug("Debug: minPrice is $minPrice")
        val cachedPrice = priceMap[unnotedId]
        if (cachedPrice != null) {
            log.debug("Using cached price for ${item.definition!!.name}: $cachedPrice")
            return cachedPrice * item.quantity >= minPrice
        }

        val gePrice = GrandExchange.lookup(unnotedId)?.price
        if (gePrice != null) {
            log.debug("Determined price of ${item.definition!!.name} to be $gePrice")
            priceMap[unnotedId] = gePrice
            return gePrice * item.quantity >= minPrice
        } else {
            log.debug("${item.definition!!.name} returned an error when fetching the price, setting price to 1 gp")
            priceMap[unnotedId] = 1
            return false
        }
    }
    fun manageLooting(): List<LootItem> {
        if (!shouldLoot) return emptyList()
        return lootDI()
    }

    private fun isAnyLoot() = lootQuery.results().any()

    private fun getAllLoot() = lootQuery.results().toList()

    private fun foodOrJunk(): Boolean {
        return Inventory.newQuery().actions("Eat").results().any()
    }
    private fun removeFoodOrJunk() {
        val item = Inventory.newQuery().actions("Eat").results().first() ?: return
        DI.send(MenuAction.forSpriteItem(item,"Eat"))
        Execution.delayUntil({ !item.isValid },600)
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
        DI.send(MenuAction.forSpriteItem(lootingBag, "Open"))
        Execution.delayUntil({ lootingBag.id != 11941}, 600)
    }

    private fun lootDI(): List<LootItem> {
        val lootItems = getAllLoot()
        if (lootItems.isEmpty() || (Inventory.isFull() && !foodOrJunk())) return emptyList()

        log.debug("Looting ${lootItems.joinToString ( ", " ) { it.definition?.name ?: "Null" }}")

        val lootedItems = mutableListOf<LootItem>()
        DefaultUI.setStatus("Attempting to loot")
        for (item: GroundItem in lootItems) {

            if (hasLootingBag() && isLootingBagClosed()) {
                openLootingBag()
            }
            if (Inventory.isFull() && (!hasLootingBag() || LootingBag.isFull())) {
                if (!foodOrJunk()) break
                removeFoodOrJunk()
            }

            //val inventoryBefore = Inventory. { it.stack }
            if (!DI.send(MenuAction.forGroundItem(item,MenuOpcode.GROUND_ITEM_THIRD_OPTION) )) continue
            if (Execution.delayUntil( { !item.isValid }, 2000 )) continue

            val lootItem = LootItem(
                itemId = item.id,
                itemName = item.definition?.name ?: "Null",
                stack = item.quantity,
            )
            lootedItems.add(lootItem)
        }
        return lootedItems
    }

}