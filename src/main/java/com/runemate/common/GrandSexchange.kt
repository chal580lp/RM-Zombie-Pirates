package com.runemate.common

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.addons.withStageExecutor
import com.runemate.common.item.SetItem
import com.runemate.game.api.hybrid.entities.definitions.ItemDefinition
import com.runemate.game.api.hybrid.input.Keyboard
import com.runemate.game.api.hybrid.local.hud.interfaces.*
import com.runemate.game.api.hybrid.net.GrandExchange
import com.runemate.game.api.hybrid.queries.results.InterfaceComponentQueryResults
import com.runemate.game.api.script.Execution
import java.awt.Color
import kotlin.math.abs

data class GrandExchangeSlot(
    val state: GrandExchangeSlotState,
    val name: String?,
    val id: Int?,
    val parent: InterfaceComponent
)

enum class GrandExchangeSlotState {
    EMPTY,
    IN_PROGRESS,
    ABORTED,
    COMPLETED
}

object GrandSexchange {
    private val log = getLogger("GrandSexchange")

    //2 = View/Abort offer actions
    //18 = itemdef + itemid
    //19 = slot item name text
    //22 = completed/aborted/progress textcolor + getWidth for progress?? 4/10 = 42
    private val COMPLETED_RGB = Color(0, 95, 0) //[465, X, 22]
    private val abortedRGB = Color(143, 0, 0) //[465, X, 22]
    private val inProgressRGB = Color(216, 128, 32) //[465, X, 22]

    private fun getSlotInterfaces(): List<InterfaceComponent?> {
        val slotInterfaces = listOf(
            Interfaces.getAt(465, 7),
            Interfaces.getAt(465, 8),
            Interfaces.getAt(465, 9),
            Interfaces.getAt(465, 10),
            Interfaces.getAt(465, 11),
            Interfaces.getAt(465, 12),
            Interfaces.getAt(465, 13),
            Interfaces.getAt(465, 14),

            )
        return slotInterfaces
    }


    fun test() {
        //val x = Color.
//        Interfaces.newQuery().textContains("Blighted").results().first()?.parentComponent?.getChildren { it. }
//        Interfaces.getAt(465,13)?.getChildren { it.tooltip?.contains("Buying")!! }.first()
    }

    private fun getSellInterfaces(): InterfaceComponentQueryResults =
        Interfaces.newQuery().actions("Create Sell offer").visible().results()

    private fun getBuyInterfaces(): List<InterfaceComponent> {
        val action = "Create Buy offer"
        val matchingChildren = mutableListOf<InterfaceComponent>()
        getSlotInterfaces().forEach { interfaceComponent ->
            interfaceComponent?.children?.filter { it.isVisible && it.actions.contains(action) }
                ?.forEach { matchingChildren.add(it) }
        }
        return matchingChildren.toList()
    }

    //Interfaces.newQuery().actions("Create Buy offer").visible().results()

    fun hasUnusedSlots(): Boolean = getBuyInterfaces().isNotEmpty()

    private fun getFullInterfaces(): InterfaceComponentQueryResults =
        Interfaces.newQuery().actions("View offer").visible().results()

    private fun getAbortInterfaces(): InterfaceComponentQueryResults =
        Interfaces.newQuery().actions("Abort offer").visible().results()

    fun getCompletedOffers(): List<InterfaceComponent> {
        //Interfaces.newQuery().actions("View offer").visible().results().filter { it.isVisible && it.actions.size == 1 }
        val matchingChildren = mutableListOf<InterfaceComponent>()
        getSlotInterfaces().forEach { interfaceComponent ->
            interfaceComponent?.children?.filter { it.isVisible && it.textColor.equals(COMPLETED_RGB) }
                ?.forEach { _ -> matchingChildren.add(interfaceComponent) }
        }
        return matchingChildren.toList()
    }

    fun getInProgressOffers(): List<InterfaceComponent> {
        val action = "Abort offer"
        val matchingChildren = mutableListOf<InterfaceComponent>()
        getSlotInterfaces().forEach { interfaceComponent ->
            if (interfaceComponent?.children?.any { (it.isVisible && it.actions.contains(action)) } == true
                && interfaceComponent.children?.any {
                    it.isVisible && (it.textColor.equals(COMPLETED_RGB) || it.textColor.equals(
                        abortedRGB
                    ))
                } == false) {
                matchingChildren.add(interfaceComponent)
            }
        }
        return matchingChildren.toList()
    }

    fun canCollect(): Boolean {
        getSlotInterfaces().forEach { interfaceComponent ->
            if (interfaceComponent?.children?.any {
                    it.isVisible && (it.textColor.equals(COMPLETED_RGB) || it.textColor.equals(
                        abortedRGB
                    ))
                } == true) {
                return true
            }
        }
        return false
    }

    fun isItemNameInAnySlot(name: String): Boolean {
        getSlotInterfaces().forEach {
            if (it != null && getItemInterface(it)?.name == name) return true
        }
        log.debug("Item '$name' not found in any slot")
        return false
    }

    fun getItemInAnySlot(name: String): InterfaceComponent? {
        getSlotInterfaces().forEach {
            if (it != null && getItemInterface(it)?.name == name) return it
        }
        log.debug("Item '$name' not found in any slot")
        return null
    }

    fun getItemInterface(slot: InterfaceComponent): ItemDefinition? {
        val child = slot.getChildren { it.containedItem != null && it.isVisible }.firstOrNull()
        return child?.containedItem
    }

    fun isItemIDInAnySlot(id: Int): Boolean {
        getSlotInterfaces().forEach {
            if (it != null && getItemID(it) != null && getItemID(it) == id) return true
        }
        log.debug("Item $id not found in any slot")
        return false
    }


    fun getItemID(slot: InterfaceComponent): Int? {
        val x = slot.getChildren { it.containedItem != null && it.isVisible }.firstOrNull()
        return x?.containedItemId
    }
    fun getItemName(slot: InterfaceComponent): String? {
        val x = slot.getChildren { it.containedItem != null && it.isVisible }.firstOrNull()
        return x?.containedItem?.name
    }

    fun getOfferQuantity(id: Int): Int? {
        val x = getSlotInterfaces().find { it != null && it.isVisible && getItemID(it) == id }?.getChild(18)
        return x?.containedItemQuantity
    }
    fun getOfferQuantity(name: String): Int? {
        val x = getSlotInterfaces().find { it != null && it.isVisible && getItemName(it) == name }?.getChild(18)
        return x?.containedItemQuantity
    }

    private fun getTextInterface(): InterfaceComponent? =
        Interfaces.newQuery().textContains("What would you like to buy?").results().firstOrNull()

    fun collectToInventory(): Boolean = collectTo("Collect to inventory")

    fun collectToBank(): Boolean = collectTo("Collect to bank")

    private fun collectTo(action: String): Boolean {
        return Interfaces.newQuery().actions(action).visible().results().firstOrNull()?.let { button ->
            button.interact(action)
            Execution.delay(2000)
            true
        } ?: false
    }

    fun abortAllOffers(): Boolean {
        getAbortInterfaces().forEach { button ->
            runCatching {
                button.interact("Abort offer")
                Execution.delayUntil({ !button.isVisible }, 2000)
            }.onFailure { log.error("Failed to abort offer $it") }
        }
        return getAbortInterfaces().isEmpty()
    }

    fun abortOffer(slotInterface: InterfaceComponent): Boolean {
        val abort = slotInterface.getChildren { it.isVisible && it.actions.contains("Abort offer") }.firstOrNull()
            ?: return false
        //val offer = Interfaces.newQuery().textContains(name).results().firstOrNull() ?: return false
        return abort.interact("Abort offer").also { aborted ->
            if (aborted) Execution.delayUntil({ !abort.isVisible }, 2000)
        }
    }

    private fun placeOffer(
        name: String,
        id: Int,
        amount: Int,
        isBuyOffer: Boolean,
        priceFn: (() -> Int)? = null,
        clickCount: Int? = null
    ): Boolean = withStageExecutor {
        if (clickCount != null && clickCount !in -20..20) {
            log.warn("You've tried to create an offer with an invalid click count! $clickCount")
            return false
        }
        executeStage(0) {
            val offerInterface = if (isBuyOffer) getBuyInterfaces().firstOrNull() else getSellInterfaces().firstOrNull()
            offerInterface ?: return@executeStage false
            if (isBuyOffer) {
                offerInterface.interact(if (isBuyOffer) "Create Buy offer" else "Create Sell offer")
                Execution.delayUntil({ !offerInterface.isVisible }, 2000)
                Execution.delay(600)
                if (Interfaces.newQuery().filter { it.containedItemId == id }.visible().results().isEmpty()) {

                    getTextInterface() ?: return@executeStage false
                    log.debug("Entering item name $name in GE text interface")
                    Keyboard.type(name, false)
                    Execution.delayUntil({ Interfaces.newQuery().texts(name).actions("Select").results().any() }, 4000)
                    Execution.delay(600)
                } else {
                    log.debug("Skipping to stage 2 found interface for $name")
                }
                val itemInterface =
                    Interfaces.newQuery().filter { it.containedItemId == id }.visible().results().firstOrNull()
                if (itemInterface == null) {
                    log.warn("Failed to find item interface for $name")
                    return@executeStage false
                }
                itemInterface.interact("Select")
                Execution.delayUntil({ !itemInterface.isVisible }, 3000)
                Execution.delay(300)
            } else {
                val inventoryItem =
                    Inventory.newQuery().ids(id).results().firstOrNull() ?: Inventory.newQuery().names(name).results()
                        .firstOrNull()
                if (inventoryItem == null) {
                    log.warn("Item $name not found in inventory")
                    return@executeStage false
                }
                if (inventoryItem.interact("Offer")) //Execution.delayUntil({ Interfaces.newQuery().texts("Offer").results().any() }, 3000)
                Execution.delay(300)
            }
            true
        }

        executeStage(1) {
            if (Interfaces.newQuery().filter { it.containedItemId == id }.visible().results().isNullOrEmpty()) {
                log.warn("Incorrect item in ID for $name $id")
                return@executeStage false
            }
            Execution.delay(200)
            true
        }

        executeStage(2) {
            val targetPrice = priceFn?.invoke()
            if (targetPrice != null) {
                val offerPrice = getUnfinishedOfferPrice() ?: return@executeStage false
                if (offerPrice != targetPrice && !setOfferPrice(targetPrice)) {
                    log.warn("Failed to set offer price to $targetPrice")
                    return@executeStage false
                }
            } else if (clickCount != null) {
                if (clickCount != 0 && !clickPriceButton(clickCount, isBuyOffer)) {
                    log.warn("Failed to set offer price using button clicks")
                    return@executeStage false
                }
            }
            Execution.delay(600)
            true
        }

        executeStage(3) {
            val offerQuantity = getUnfinishedOfferQuantity() ?: return@executeStage false
            if (offerQuantity != amount && !setOfferQuantity(amount)) {
                log.warn("Failed to set offer quantity to $amount")
                return@executeStage false
            }
            Execution.delay(600)
            true
        }

        confirmOffer()
    }

    private fun clickPriceButton(clickCount: Int, isBuyOffer: Boolean): Boolean {
        val button = if (isBuyOffer) {
            if (clickCount > 0) get5PercentIncreaseButton() else get5PercentDecreaseButton()
        } else {
            if (clickCount > 0) get5PercentDecreaseButton() else get5PercentIncreaseButton()
        } ?: return false

        val clicks = minOf(clickCount, 20)
        log.debug("Clicking $clicks times on ${if (clickCount > 0) "+5%" else "-5%"} button. Original click count: $clickCount")
        repeat(clicks) {
            val initialPrice = getUnfinishedOfferPrice()
            if (!button.click() || !Execution.delayUntil({ initialPrice != getUnfinishedOfferPrice() }, 4000)) {
                return false
            }
        }
        return true
    }

    fun placeBuyOffer(name: String, id: Int, amount: Int, priceFn: () -> Int): Boolean =
        placeOffer(name, id, amount, isBuyOffer = true, priceFn = priceFn)

    fun placeBuyOfferWith5PercentButton(name: String, id: Int, amount: Int, clickCount: Int): Boolean =
        placeOffer(name, id, amount, isBuyOffer = true, clickCount = clickCount)

    fun placeSellOffer(name: String, id: Int, amount: Int, priceFn: () -> Int): Boolean =
        placeOffer(name, id, amount, isBuyOffer = false, priceFn = priceFn)

    fun placeSellOfferWith5PercentButton(name: String, id: Int, amount: Int, clickCount: Int): Boolean =
        placeOffer(name, id, amount, isBuyOffer = false, clickCount = clickCount)

    private fun correctItemInTitle(name: String): Boolean =
        Interfaces.newQuery().texts(name).results().firstOrNull()?.actions?.isEmpty() == true

    private fun getUnfinishedOfferPrice(): Int? =
        Interfaces.newQuery().textContains("coins").visible().results().firstOrNull()?.let {
            it.rawText?.replace(" coins", "")?.replace(",", "")?.toIntOrNull()
        }

    private fun getUnfinishedOfferQuantity(): Int? =
        Interfaces.newQuery().texts("1").visible().results().lastOrNull()?.rawText?.toIntOrNull()

    private fun get5PercentIncreaseButton(): InterfaceComponent? {
        return Interfaces.newQuery().texts("+5%").visible().results().firstOrNull()
    }

    private fun get5PercentDecreaseButton(): InterfaceComponent? {
        return Interfaces.newQuery().texts("-5%").visible().results().firstOrNull()
    }

    private fun setOfferPrice(amount: Int): Boolean = setOffer("Enter price", amount, ::getUnfinishedOfferPrice)

    private fun setOfferQuantity(amount: Int): Boolean =
        setOffer("Enter quantity", amount, ::getUnfinishedOfferQuantity)

    private fun confirmOffer(): Boolean {
        val button = Interfaces.newQuery().actions("Confirm").visible().results().firstOrNull() ?: return false
        return button.click().also { clicked ->
            if (clicked) Execution.delayUntil({ !button.isVisible }, 4000)
        }
    }

    private inline fun setOffer(action: String, amount: Int, crossinline getValue: () -> Int?): Boolean {
        val button = Interfaces.newQuery().actions(action).visible().results().firstOrNull() ?: return false
        val currentValue = getValue()
        return button.click().also { clicked ->
            if (clicked) {
                Execution.delayUntil({ InputDialog.isOpen() }, 4000)
                runCatching {
                    InputDialog.enterAmount(amount)
                    Execution.delayUntil({ currentValue != getValue() }, 4000)
                }.onFailure { log.error("Failed to set offer $action $it") }
                    .isSuccess
            }
        }
    }

    private fun onOffersScreen(): Boolean =
        Interfaces.newQuery().textContains("Select an offer slot to set up or view an offer").visible().results()
            .isNotEmpty()

    private fun pressBackButton(): Boolean {
        val button = Interfaces.newQuery().actions("Back").visible().results().firstOrNull() ?: return false
        return button.click().also { clicked ->
            if (clicked) Execution.delayUntil({ !button.isVisible }, 4000)
        }
    }
    fun handleFullInventory(itemsToSell: List<SetItem>) {
        if (Inventory.isFull() && !Inventory.containsAnyOf(*itemsToSell.map { it.name }.toTypedArray())) {
            if (!Bank.isOpen()) {
                Bank.open()
            } else {
                Bank.depositInventory()
                Execution.delayUntil({ Inventory.isEmpty() }, 4000)
            }
        }
    }

    fun handleBankOperations() {
        if (Bank.isOpen()) {
            Bank.close()
            Execution.delayUntil({ !Bank.isOpen() }, 2000)
        }
    }

    fun handleGrandExchangeOperations() {
        if (!GrandExchange.isOpen()) {
            GrandExchange.open()
            Execution.delayUntil({ GrandExchange.isOpen() }, 4000)
        }
    }

    fun handleSlotsAvailability(
        handleCompletedOffers: () -> Boolean,
        handleInProgressOffers: () -> Boolean
    ): Boolean {
        if (!hasUnusedSlots()) {
            if (!onOffersScreen()) {
                pressBackButton()
                Execution.delayUntil({ onOffersScreen() }, 3000)
                return false
            }

            log.debug("All slots are in use.")
            if (handleCompletedOffers()) {
                Execution.delay(1000)
                return false
            }

            if (handleInProgressOffers()) {
                return false
            }
        }
        return true
    }

    fun updateItemsInOfferByItemId(
        itemsToProcess: List<SetItem>,
        itemsInOffer: MutableMap<Int, Int>
    ) {
        val items = mutableMapOf<Int, Int>()
        itemsToProcess.forEach { item ->
            val itemId = item.id
            if (isItemIDInAnySlot(itemId)) {
                val quantity = getOfferQuantity(itemId) ?: return@forEach
                items[itemId] = quantity
            }
        }
        updateItemsInOffer(items, itemsInOffer)
    }

    fun updateItemsInOfferByItemName(
        itemsToProcess: List<SetItem>,
        itemsInOffer: MutableMap<String, Int>
    ) {
        val items = mutableMapOf<String, Int>()
        itemsToProcess.forEach { item ->
            val itemName = item.name
            if (isItemNameInAnySlot(itemName)) {
                val quantity = getOfferQuantity(itemName) ?: return@forEach
                items[itemName] = quantity
            }
        }
        updateItemsInOffer(items, itemsInOffer)
    }

    private fun <T> updateItemsInOffer(
        items: Map<T, Int>,
        itemsInOffer: MutableMap<T, Int>
    ) {
        val iterator = itemsInOffer.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val key = entry.key
            val value = entry.value

            if (items.containsKey(key)) {
                val newValue = items[key]
                if (newValue != value) {
                    itemsInOffer[key] = newValue!!
                    log.debug("Item {} was updated in itemsInOffer with quantity {}", key, value)
                }
            } else {
                log.debug("Item {} not in offer, removing", key)
                iterator.remove()
            }
        }

        items.forEach { (key, value) ->
            if (!itemsInOffer.containsKey(key)) {
                itemsInOffer[key] = value
                log.debug("Item {} was added to itemsInOffer with quantity {}", key, value)
            }
        }
    }
}


