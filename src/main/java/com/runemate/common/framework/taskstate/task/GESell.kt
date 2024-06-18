package com.runemate.common.framework.taskstate.task

import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.GrandSexchange

import com.runemate.common.util
import com.runemate.common.item.Loot
import com.runemate.common.item.SetItem
import com.runemate.common.framework.core.Task
import com.runemate.common.framework.core.TaskMachine
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.hybrid.net.GrandExchange
import com.runemate.game.api.script.Execution
import com.runemate.game.api.script.framework.listeners.InventoryListener
import com.runemate.game.api.script.framework.listeners.events.ItemEvent
import kotlin.math.pow
import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.item.name

class GESell<TSettings : BotConfig>(private val bot: TaskMachine<TSettings>) : Task, InventoryListener {
    private val log = getLogger("GESell")
    private val itemsInGrandExchangeOffer = mutableMapOf<String, Int>()
    private val itemsSold = mutableMapOf<String, Int>()
    private val itemsToSell = mutableListOf<SetItem>()
    private val attemptCounts = mutableMapOf<String, Int>()
    private val removedItems = mutableListOf<SetItem>()
    private val minSellAmount = 10000
    private var finished: Boolean = false

    override fun validate(): Boolean {
        return !finished
    }

    override fun execute() {
        log.debug("Current state of itemsSold: {}", itemsSold)

        if (GrandExchange.isOpen()) {
            checkInProgressOffers()
        }
        if (itemsToSell.isEmpty()) {
            log.debug("No items to sell, setting items to sell")
            setItemsToSell()
            withdrawSellItems()
            Execution.delay(600)
            return
        }

        if (allItemsSold()) {
            finished = true
            return
        }
        val itemNamesArray = itemsToSell.map { it.name }.toTypedArray()
        if (Inventory.newQuery().names(*itemNamesArray).results().isEmpty() && itemsInGrandExchangeOffer.isEmpty()) {
            log.debug("Items to sell: ${itemNamesArray.joinToString(", ")}")
            log.debug("Inventory has no items to sell and no items are in offer")
            setItemsToSell()
            withdrawSellItems()
            Execution.delay(600)
            return
        }

        handleFullInventory()
        handleBankOperations()
        handleGrandExchangeOperations()

        updateItemsInOffer()

        if (!handleSlotsAvailability()) return

        processItemsToSell()
        checkAbortAndLowerPrices()

        if (GrandSexchange.canCollect()) {
            log.debug("Offers need to be collected so we can relist")
            GrandSexchange.collectToInventory()
        }

        Execution.delay(1200)
        log.debug("Exit state of itemsSold: {}", itemsSold)
    }
    private fun checkInProgressOffers() {
        GrandSexchange.getInProgressOffers().forEach { slot ->
            val itemInterface = GrandSexchange.getItemInterface(slot)
            val itemId = itemInterface?.id ?: return@forEach
            val itemName = itemInterface.name

            val initialItem = Loot.ZOMBIE_PIRATES.find { it.id == itemId && it.gameName == itemName }
            if (initialItem != null) {
                log.debug("Found in-progress offer for initial item $itemName id $itemId")
                GrandSexchange.abortOffer(slot)
                Execution.delay(1200)
            }
        }
    }


    private fun handleCompletedOffers(): Boolean {
        val completedOffers = GrandSexchange.getCompletedOffers()
        if (completedOffers.isNotEmpty()) {
            log.debug("Found at least one slot with a completed offer.")
            if (GrandSexchange.collectToInventory()) {
                log.debug("Successfully collected items to inventory.")
                Execution.delayUntil({ GrandSexchange.getCompletedOffers().isEmpty() }, 3000)
                return true
            }
        }
        return false
    }

    private fun handleInProgressOffers(): Boolean {
        val inProgressOffers = GrandSexchange.getInProgressOffers()
        if (inProgressOffers.isNotEmpty()) {
            log.debug("Found at least one slot with an offer in progress.")
            checkAbortAndLowerPrices()
            return true
        }
        return false
    }

    private fun withdrawSellItems() {
        if (Bank.isOpen()) {
            Bank.setWithdrawMode(Bank.WithdrawMode.NOTE)
            Bank.depositAllExcept(*itemsToSell.map { it.name }.toTypedArray())
            itemsToSell.forEach {
                log.debug("Withdrawing ${it.name} x ${it.quantity}")
                Bank.withdraw(it.id, it.quantity)
            }
            Execution.delayUntil({ Inventory.containsAnyOf(*itemsToSell.map { it.name }.toTypedArray()) }, 3000)
        }
    }


    private fun processItemsToSell() {
        val itemsToSellList = getItemsToSell()
        log.debug("Total items to sell: ${itemsToSellList.size}")

        itemsToSellList.forEach { (item, quantity) ->
            log.debug("Selling ${item.name} at $quantity")
            if (!GrandSexchange.hasUnusedSlots()) {
                log.debug("No unused slots available")
                return
            }
            if (removedItems.contains(item)) {
                log.debug("Item ${item.name} was removed, skipping")
                return@forEach
            }
            if (GrandSexchange.isItemIDInAnySlot(item.id)) {
                log.debug("Offer for ${item.name} is already in progress")
                return@forEach
            }
            if (!Inventory.contains(item.name)) {
                log.debug("Inventory does not contain ${item.name}")
                return@forEach
            }
            makeOffer(item, quantity)
            Execution.delay(600)
        }
    }

    private fun allItemsSold(): Boolean {
        return itemsToSell
            .filterNot { removedItems.contains(it) }
            .all { item ->
                val sold = itemsSold[item.name] ?: 0
                //log.debug("Checking if all items sold for ${item.name}: required = ${item.quantity}, sold = $sold, total = $sold")
                sold >= item.quantity
            }
    }

    private fun makeOffer(item: SetItem, quantity: Int): Boolean {
        log.debug("Making sale offer for ${item.name} : $quantity")

        val attempts = attemptCounts.getOrDefault(item.name, 0)
        val price = calculatePrice(item.id, item.name, attempts)

        when {
            price == 0 -> {
                log.debug("Price for ${item.name} is 0, removing from items to sell")
                removedItems.add(item)
                return false
            }

            GrandSexchange.placeSellOfferWith5PercentButton(item.name, item.id, quantity, attempts) -> {
                log.debug("Sale offer for ${item.name} at $price for $quantity was placed successfully")
                itemsInGrandExchangeOffer[item.name] = quantity
                attemptCounts[item.name] = attempts + 1
                return true
            }

            else -> return false
        }
    }

    private fun calculatePrice(itemId: Int, itemName: String, attempts: Int): Int {
        // Calculate the price based on the number of attempts
        // For simplicity, we'll decrease the price by 5% for each attempt
        val basePrice = util.getPrice(itemId, itemName)
        return (basePrice * (0.95.pow(attempts))).toInt()
    }

    private fun getItemsToSell(): List<Pair<SetItem, Int>> {
        return itemsToSell.mapNotNull { item ->
            val inOffer = itemsInGrandExchangeOffer[item.name] ?: 0
            val remaining = item.quantity - inOffer

            if (remaining > 0 && !removedItems.contains(item) && Inventory.contains(item.name)) {
                log.debug("Item ${item.name}: remaining to sell = $remaining")
                item to remaining
            } else {
                null
            }
        }
    }

    private fun setItemsToSell() {
        itemsToSell.clear()
        log.debug("Setting items to sell")
        if (!Bank.isOpen()) {
            Bank.open()
            return
        }
        val items = Loot.ZOMBIE_PIRATES
        if (items.isEmpty()) {
            log.error("Failed to fetch Loot items from Loot class")
            return
        }
        val untradeables = mutableListOf<String>()
        val bankItems = mutableMapOf<String, Int>()
        runCatching {
            items.forEach { lootItem ->
                val bankID = if (!lootItem.noted) lootItem.id else lootItem.id - 1
                val item = Bank.newQuery().ids(bankID).results().first() ?: return@forEach
                var name: String
                item.let {
                    name = it.definition?.name ?: "null ${lootItem.id}"
                    if (it.definition?.isTradeable == false) {
                        if (it.id == 995) return@forEach
                        untradeables.add(it.definition?.name ?: "Null")
                        log.error("Loot item came back as untradeable, this is a bug: ${it.definition?.name} : ${it.definition?.id}")
                        return@forEach
                    }
                }
                log.debug("Checking item $name id ${item.id} from loot item ${lootItem.gameName} id ${lootItem.id}")
                bankItems[name] = item.quantity
                var amount = bankItems[name] ?: 0
                if (bot.getInvManager().inventoryContains(name)) return@forEach
                if (bot.getEquipmentManager().equipmentContains(name)) amount--
                if (amount < 1) return@forEach

                if (util.getPrice(bankID, name).times(amount) > minSellAmount) {
                    val existingItem = itemsToSell.firstOrNull { it.id == lootItem.id }
                    if (existingItem != null) {
                        existingItem.quantity = amount
                    } else {
                        itemsToSell.add(SetItem(item.name ?: "NullER", item.id, amount))
                    }
                }
            }
        }.onFailure {
            log.error("Error getting items to sell: ${it.message}")
        }.onSuccess {
            if (itemsToSell.isEmpty()) {
                log.debug("No items to sell")
                finished = true
            }
            log.debug("Items to sell: {}", itemsToSell)
        }
    }


    private fun checkAbortAndLowerPrices() {
        if (GrandSexchange.getCompletedOffers().isNotEmpty()) {
            handleCompletedOffers()
        }
        if (GrandSexchange.hasUnusedSlots()) return // TODO: check Inventory if we have items to sell, otherwise this will loop
        GrandSexchange.getInProgressOffers().forEach { slot ->
            val itemName = GrandSexchange.getItemInterface(slot)?.name ?: return@forEach
            if (itemsToSell.any { it.name == itemName }) {
                val attempts = attemptCounts.getOrPut(itemName) { 0 } + 1
                attemptCounts[itemName] = attempts
                log.debug("Offer for $itemName was not completed, increasing attempt count to $attempts")
                GrandSexchange.abortOffer(slot)
            }
        }
        Execution.delay(1200)
    }

    override fun onItemAdded(event: ItemEvent?) {
        if (event == null) return
        event.item.definition?.let { updateItemsFromInventoryEvent(it.id, it.name, event.quantityChange) }
            ?: log.warn("${event.item.definition?.name}  ${event.item.id} has no unnoted id")
        // Check if the added item matches any of the items we wanted to sell initially
        val itemId = event.item?.id ?: return
        val itemName = event.item?.definition?.name ?: return
        val quantity = event.quantityChange

        val initialItem = Loot.ZOMBIE_PIRATES.find { it.id == itemId && it.gameName == itemName }
        if (initialItem != null) {
            val existingItem = itemsToSell.find { it.id == itemId && it.name == itemName }
            if (existingItem != null) {
                existingItem.quantity += quantity
                log.debug("Item $itemName id $itemId quantity updated in itemsToSell to ${existingItem.quantity}")
            } else {
                itemsToSell.add(SetItem(initialItem.gameName, initialItem.id, quantity))
                log.debug("Item $itemName id $itemId added back to itemsToSell with quantity $quantity")
            }
        }
    }

    override fun onItemRemoved(event: ItemEvent?) {
        if (event == null) return
        log.debug(
            "Item removed {} id {} change = {} {}",
            event.item?.definition?.name,
            event.item?.id,
            event.quantityChange,
            event.type
        )

        // Update itemsToSell based on the removed item
        val itemId = event.item?.id ?: return
        val itemName = event.item?.definition?.name ?: return
        val quantity = event.quantityChange

        val item = itemsToSell.find { it.id == itemId || it.name == itemName }
        if (item != null) {
            item.quantity -= quantity
            if (item.quantity <= 0 || !Inventory.contains(itemName)) {
                itemsToSell.remove(item)
                log.debug("Item $itemName id $itemId removed from itemsToSell")
            } else {
                log.debug("Item $itemName id $itemId quantity updated in itemsToSell to ${item.quantity}")
            }
        }

        // Check if the removed item was sold and update itemsSold accordingly
        val soldQuantity = itemsSold[itemName] ?: 0
        itemsSold[itemName] = soldQuantity + quantity
        log.debug("Item $itemName id $itemId sold quantity updated to ${itemsSold[itemName]}")
    }

    private fun updateItemsFromInventoryEvent(itemId: Int, itemName: String, quantity: Int) {
        val previousQuantity = itemsSold[itemName] ?: 0
        val newQuantity = previousQuantity + quantity
        log.debug("Updating item $itemName id $itemId: previous = $previousQuantity, change = $quantity, new = $newQuantity")
        itemsSold[itemName] = newQuantity
    }
    // In GEBuy and GESell classes

    private fun handleFullInventory() {
        GrandSexchange.handleFullInventory(itemsToSell)
    }

    private fun handleBankOperations() {
        GrandSexchange.handleBankOperations()
    }

    private fun handleGrandExchangeOperations() {
        GrandSexchange.handleGrandExchangeOperations()
    }

    private fun handleSlotsAvailability(): Boolean {
        return GrandSexchange.handleSlotsAvailability(
            handleCompletedOffers = ::handleCompletedOffers,
            handleInProgressOffers = ::handleInProgressOffers
        )
    }

    private fun updateItemsInOffer() {
        GrandSexchange.updateItemsInOfferByItemName(itemsToSell, itemsInGrandExchangeOffer)
    }
}

