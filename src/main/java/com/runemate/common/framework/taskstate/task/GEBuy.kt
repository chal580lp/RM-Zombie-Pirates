package com.runemate.common.framework.taskstate.task

import com.runemate.common.BankManager
import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.GrandSexchange

import com.runemate.common.item.SetItem
import com.runemate.common.framework.core.*
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank
import com.runemate.game.api.hybrid.net.GrandExchange
import com.runemate.game.api.script.Execution
import com.runemate.game.api.script.framework.listeners.ChatboxListener
import com.runemate.game.api.script.framework.listeners.InventoryListener
import com.runemate.game.api.script.framework.listeners.events.ItemEvent
import com.runemate.game.api.script.framework.listeners.events.MessageEvent
import kotlin.math.pow
import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.item.BaseItem
import com.runemate.common.item.items
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.hud.interfaces.Chatbox

class GEBuy<TSettings : BotConfig>(private val bot: TaskMachine<TSettings>) : Task, InventoryListener, ChatboxListener {
    private val inventoryManager = bot.botComponents.invManager
    private val log = getLogger("GEBuy")
    private val itemsInGrandExchangeOffer = mutableMapOf<Int, Int>()
    private val itemsPurchased = mutableMapOf<Int, Int>()
    private val itemsToBuy = mutableListOf<SetItem>()
    private val attemptCounts = mutableMapOf<Int, Int>()
    private val removedItems = mutableListOf<SetItem>()

    override fun validate(): Boolean {
        return true
    }

    override fun execute() {
        log.debug("Current state of itemsPurchased: {}", itemsPurchased)

        //TODO: Decant potions if needed before we set items to buy

        if (itemsToBuy.isEmpty()) {
            setItemsToBuy()
            return
        }

        if (checkIfAllItemsPurchased() && itemsToBuy.isNotEmpty()) {
            log.debug("All items have been purchased.")
            bot.setCurrentState(bot.defaultState())
            return
        }

        //Inv Full, Bank or Ge not open. etc...
        handleUtilityOperations()
        /*
        Our main logic in deciding what to buy and how much to buy
        We update items sold & purchased based on checking items in offer
         */
        updateItemsInOffer()
        Execution.delay(100)
        //Safety check in case we have an item in offer that we don't want to buy anymore
        checkAndCancelUnwantedOffers()

        //Our main logic in buying items
        processItemsToBuy()

        handleInProgressOffers()

        if (GrandSexchange.canCollect()) {
            log.debug("Can collect! Collecting items to inventory.")
            GrandSexchange.collectToInventory()
        }
        Execution.delay(1200)
        log.debug("Exit state of itemsPurchased: {}", itemsPurchased)
    }

    private fun processItemsToBuy() {
        val itemsToBuyList = getItemsToBuy()
        log.debug("Total items to buy: ${itemsToBuyList.size}")

        itemsToBuyList.forEach { (item, quantity) ->
            log.debug("Buying ${item.name} at $quantity")
            if (!GrandSexchange.hasUnusedSlots()) {
                log.debug("No unused slots available")
                return@forEach
            }
            if (GrandSexchange.isItemIDInAnySlot(item.id)) {
                log.debug("Offer for ${item.name} is already in progress")
                return@forEach
            }
            makeOffer(item, quantity)
            Execution.delay(600)
        }
    }

    private fun getItemsToBuy(): List<Pair<SetItem, Int>> {
        return itemsToBuy.filter { item ->
            if (removedItems.contains(item)) {
                log.debug("Item ${item.name} was removed, skipping in getItemsToBuy()")
            }
            val purchasedQuantity = itemsPurchased[item.id] ?: 0
            val inOfferQuantity = itemsInGrandExchangeOffer[item.id] ?: 0
            val totalQuantity = purchasedQuantity + inOfferQuantity
            when (item.quantity) {
                purchasedQuantity -> log.debug("PURCHASED: ${item.name} (ID: ${item.id}), Quantity: ${item.quantity}")
                purchasedQuantity + inOfferQuantity -> log.debug("PENDING: ${item.name} (ID: ${item.id}), Bought: $purchasedQuantity, InOffer: $inOfferQuantity")
                inOfferQuantity -> log.debug("INOFFER: ${item.name} (ID: ${item.id}), Quantity: ${item.quantity}")
                else -> log.debug("INCOMPLETE: ${item.name} (ID: ${item.id}), Quantity: ${item.quantity}, Bought: $purchasedQuantity, InOffer: $inOfferQuantity")
            }
            totalQuantity < item.quantity
        }.map { item ->
            val remaining = item.quantity - (itemsPurchased[item.id] ?: 0) - (itemsInGrandExchangeOffer[item.id] ?: 0)
            log.debug("Item ${item.name} ${item.id}: remaining to buy = $remaining")
            item to remaining
        }
    }

    private fun setItemsToBuy() {
        log.debug("Setting items to buy")
        if (!Bank.isOpen()) {
            Bank.open()
            return
        }
        val inventoryManagerItems = inventoryManager.inventory
        if (inventoryManagerItems.isEmpty()) {
            log.error("Set inventory is empty? Bot shouldn't be able to start in this state")
            return
        }
        val untradeables = mutableListOf<String>()
        val bankItems = mutableMapOf<String, Int>()
        /*
        Check for Equipment items to buy first as they are more important.
        they also will decide how many Inventory items we can afford
        TODO: Add a check for the amount of GP available to buy items
         */
        itemsToBuy.addAll(checkForEquipmentToBuy(bot.getBankManager()))
        runCatching {
            inventoryManagerItems.forEach { inventoryItem ->
                /*
                We use IDS here because we want the exact items we set in our inventory.
                Checking for (3) and so on is not needed here as we are setting the max amount in the first place
                whereas for equipment someone might set a Ring of wealth (2) for example
                 */
                val item = Bank.newQuery().ids(inventoryItem.id).results().first()
                var name: String
                item.let {
                    name = it?.definition?.name ?: "Null ${inventoryItem.id}"
                    /*
                    Check if the item is untradeable and add it to the untradeables list
                    TODO: avoid Bank Manager from activating this state if it was an untradeable item
                     */
                    if (it?.definition?.isTradeable == false) {
                        untradeables.add(name)
                        return@forEach
                    }
                }
                bankItems[name] = Bank.newQuery().ids(inventoryItem.id).results().sumOf { it.quantity }
                val amount = bankItems[name] ?: 0

                /*
                Calculate the amount needed to buy from the Grand Exchange
                Ideally we want to scale up how much we buy based on GP available
                we also need to remove super low amounts as they waste too much time
                 */
                if (amount < inventoryItem.quantity * bot.settings().geAmountMultiplier) {
                    val neededAmount = (inventoryItem.quantity * bot.settings().geAmountMultiplier) - amount
                    if (neededAmount < inventoryItem.quantity * 2 && 2 < bot.settings().geAmountMultiplier) {
                        log.debug("Amount needed for $name is too low, skipping. Amount needed: ${neededAmount}")
                        return@forEach
                    }
                    val existingItem = itemsToBuy.firstOrNull { it.id == inventoryItem.id }
                    if (existingItem != null) {
                        existingItem.quantity = neededAmount
                    } else {
                        log.debug("Adding $name to items to buy with amount $neededAmount (bank has $amount) multiplied by ${bot.settings().geAmountMultiplier}")
                        itemsToBuy.add(SetItem(inventoryItem.name, inventoryItem.id, neededAmount))
                    }
                }
            }
        }.onSuccess {
            if (itemsToBuy.isEmpty()) {
                log.warn("No items to buy! Possible error.")
                bot.setCurrentState(bot.defaultState())

            }
        }.onFailure {
            log.error("Error getting items to buy: ${it.message}")
        }
        if (!Bank.contains(items.ringOfWealth)) {
            itemsToBuy.add(SetItem("Ring of wealth (5)", 11980, 1))
        }
    }

    private fun makeOffer(item: SetItem, quantity: Int): Boolean {
        log.debug("Making offer for ${item.name} at $quantity")

        val attempts = attemptCounts.getOrDefault(item.id, 0)
        val price = calculatePrice(item.id, attempts)

        if (price == 0) {
            log.debug("Price for ${item.name} is 0, removing from items to buy")
            removedItems.add(item)
            return false
        }

        if (GrandSexchange.placeBuyOfferWith5PercentButton(
                item.purchaseString,
                item.id,
                quantity,
                attempts
            )
        ) {
            log.debug("Offer for ${item.name} at $price for $quantity was placed successfully")
            itemsInGrandExchangeOffer[item.id] = quantity
            attemptCounts[item.id] = attempts + 1
            return true
        }
        return false
    }

    private fun calculatePrice(itemId: Int, attempts: Int): Int {
        // Calculate the price based on the number of attempts
        // For simplicity, we'll increase the price by 5% for each attempt
        val basePrice = GrandExchange.lookup(itemId)?.price ?: 0
        return (basePrice * (1.05.pow(attempts)).toInt())
    }

    private fun checkForEquipmentToBuy(bankManager: BankManager<TSettings>): List<SetItem> {
        /*
        Check for missing equipment items using the same method BankManager uses.
        This is because we need the exact same logic to determine if an item is missing.
        otherwise we will be stuck in a loop with BankManager looking for different item names.
        TODO: Confirm Bank Manager is using the same logic for missing equipment items (IDEALLY HAVE THE FUNCTION IN EQUIPMENT MANAGER)
         */
        val missingItems = bankManager.checkMissingEquipment(bot.getEquipmentManager().getMissingEquipment())
        if (missingItems.isNotEmpty()) {
            log.debug("Missing equipment items: {}", missingItems)
            val itemsToBuy = missingItems.filter { item ->
                //TODO: Update this with actual slot prices
                val slotPrice = 100_000
                util.getPrice(item.id, item.name) < slotPrice
            }
            if (itemsToBuy.isNotEmpty()) {
                log.debug("Purchasing missing equipment items from the Grand Exchange")
                return itemsToBuy
            } else {
                log.debug("Missing equipment items are not below the price threshold. Skipping purchase.")
                //TODO: either update equipment so it doesn't activate ge state or set fall ::update wtf is set fall
            }
        }
        return emptyList()
    }

    override fun onItemAdded(event: ItemEvent?) {
        if (event == null) return
        log.debug("Item added {} {} {}", event.item?.id, event.quantityChange, event.type)
        event.item.definition?.unnotedId?.let { updateItemsPurchased(it, event.quantityChange) }
            ?: log.warn("Item ${event.item.id} has no unnoted id")
    }

    override fun onItemRemoved(event: ItemEvent?) {
        if (event == null) return
        log.debug("Item removed {} {} {}", event.item?.id, event.quantityChange, event.type)
        //updateItemsFromInventoryEvent(event.item.id, -event.quantityChange)
    }

    override fun onMessageReceived(p0: MessageEvent?) {
        if (p0 == null || p0.type == Chatbox.Message.Type.PUBLIC_CHAT) return
        log.debug("{} : {}", p0.message, p0.type)
        if (p0.message?.contains("You haven't got enough") == true) {
            log.debug("Not enough coins to buy item")
            bot.warningAndPause("Not enough coins to buy item, Pausing script")
        }
    }

    private fun updateItemsPurchased(itemId: Int, quantity: Int) {
        val previousQuantity = itemsPurchased[itemId] ?: 0
        val newQuantity = previousQuantity + quantity
        log.debug("Updating item $itemId: previous quantity = $previousQuantity, quantity change = $quantity, new quantity = $newQuantity")
        itemsPurchased[itemId] = newQuantity
        log.debug("Item $itemId was updated in itemsPurchased with quantity ${itemsPurchased[itemId]}")
    }
    private fun checkIfAllItemsPurchased(): Boolean {
        return itemsToBuy
            .filterNot { removedItems.contains(it) }
            .all { item ->
                val purchased = itemsPurchased[item.id] ?: 0
                val total = purchased
                log.debug("Checking if all items purchased for ${item.name}: required = ${item.quantity}, purchased = $purchased, total = $total")
                total >= item.quantity
            }
    }

    private fun checkAndCancelUnwantedOffers() {
        GrandSexchange.getInProgressOffers().forEach { slot ->
            val itemId = GrandSexchange.getItemID(slot)
            if (itemsToBuy.any { it.id == itemId } && itemsToBuy.first { it.id == itemId }.quantity == 0) {
                // If the item is in itemsToBuy and the quantity is 0, cancel the offer
                GrandSexchange.abortOffer(slot)
                Execution.delay(1000)
            }
        }
    }

    private fun checkAbortAndRaisePrices() {
        GrandSexchange.getInProgressOffers().forEach { slot ->
            val itemId = GrandSexchange.getItemID(slot) ?: return
            if (itemsToBuy.any { it.id == itemId }) {
                val attempts = attemptCounts.getOrDefault(itemId, 0)
                attemptCounts[itemId] = attempts + 1
                log.debug("Offer for $itemId was not completed, increasing attempt count to ${attemptCounts[itemId]}")
                GrandSexchange.abortOffer(slot)
            }
        }
        Execution.delay(1200)
    }
    private fun handleInProgressOffers(): Boolean {
        val inProgressOffers = GrandSexchange.getInProgressOffers()
        if (inProgressOffers.isNotEmpty()) {
            log.debug("Found at least one slot with an offer in progress.")
            checkAbortAndRaisePrices()
            return true
        }
        return false
    }
    private fun handleUtilityOperations() {
        GrandSexchange.handleFullInventory(itemsToBuy)
        GrandSexchange.handleBankOperations()
        GrandSexchange.handleGrandExchangeOperations()
    }

    private fun updateItemsInOffer() {
        GrandSexchange.updateItemsInOfferByItemId(itemsToBuy, itemsInGrandExchangeOffer)
    }
}