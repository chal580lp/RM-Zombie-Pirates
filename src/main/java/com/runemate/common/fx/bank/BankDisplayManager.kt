package com.runemate.common.fx.bank

import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.item.BaseItem
import com.runemate.common.item.Loot
import com.runemate.common.item.SetItem
import com.runemate.common.fx.FileManager
import com.runemate.common.fx.FxUtil
import com.runemate.common.framework.core.TaskMachine
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections

class BankDisplayManager<TSettings: BotConfig>(val bot: TaskMachine<TSettings>) {
    private val bank: MutableList<BaseItem> = mutableListOf()
    val bankProperty = SimpleListProperty<BaseItem>(FXCollections.observableArrayList(bank))


    var listsOfBankItems: List<List<BaseItem>> = emptyList()
    private var recommendedItemIds: List<Int> = emptyList()
    private var lootItems: Set<Loot> = emptySet()
    var itemPrices: MutableMap<Loot, Int> = mutableMapOf()
    private var lootItemIds: MutableList<Int> = mutableListOf()


    fun withdrawFromBank(item: BaseItem, count: Int): Boolean {
        if (item is SetItem) {
            item.quantity = count
            return bot.botComponents.invDManager.addToInventory(item)
        }
        return false
    }

    fun withdrawAllFromBank(item: BaseItem): Boolean {
        if (item is SetItem) {
            return bot.botComponents.invDManager.addToInventory(item, fillAll = true)
        }
        return false
    }
    fun getBank(): List<BaseItem> = bank.toList()

    fun setBankItems(lists: List<List<BaseItem>>) {
        listsOfBankItems = lists
        val ids = lists.flatten().map { it.id }
        preloadImages(ids)
    }

    fun getBankItems(): List<List<BaseItem>> = listsOfBankItems

    fun setRecommendedItemIds(ids: List<Int>) {
        recommendedItemIds = ids
    }

    fun getRecommendedItemIds(): List<Int> = recommendedItemIds

    fun <TSettings : BotConfig> setLootItems(bankDisplay: BankDisplay<TSettings>, set: List<Loot>) {
        lootItems = set.toSet()
        updateItemPrices(bankDisplay)
        val ids = set.map { it.id }
        preloadImages(ids)
    }
    private fun preloadImages(ids: List<Int>) {
        FileManager.preloadImages(ids)
    }

    fun getLootItems(): Set<Loot> = lootItems

    private fun <TSettings : BotConfig> updateItemPrices(bankDisplay: BankDisplay<TSettings>) {
        itemPrices = FxUtil.getLootPrices(bankDisplay, lootItems)
    }

    private fun updateLootItemIds() {
        lootItemIds = FileManager.loadLootItemIds().toMutableList()
    }

    fun getLootItemIds(): List<Int> = lootItemIds.toList()

    fun saveLootItemIds() {
        FileManager.saveLootItemIds(lootItemIds)
    }

    fun setMinLootValue() {
        updateLootItemIds()
    }
}