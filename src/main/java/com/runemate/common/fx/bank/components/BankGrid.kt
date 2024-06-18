// BankGrid.kt
package com.runemate.common.fx.bank.components

import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.fx.bank.*
import com.runemate.common.framework.core.TaskMachine
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.GridPane

class BankGrid<TSettings : BotConfig>(val bot: TaskMachine<TSettings>, private val displayEventHandler: DisplayEventHandler<TSettings>) : GridPane() {
    private var selectedItemButton: BankItemButton<TSettings>? = null
    private val buttonsMap = mutableMapOf<Int, Button>()
    private val lootButtonsMap = mutableMapOf<Int, LootItemButton<TSettings>>()


    init {
        hgap = 2.0
        vgap = 3.0
        padding = Insets(10.0, 1.0, 10.0, 35.0)
        //minHeight = Constants.GRID_HEIGHT
        maxHeight = Constants.GRID_HEIGHT
        //minWidth = Constants.GRID_WIDTH
        maxWidth = Constants.GRID_WIDTH
    }

    fun updateBank() {
        children.clear()
        buttonsMap.clear()

        var currentRow = 0

        bot.botComponents.bankDManager.getBankItems().forEachIndexed { listIndex, list ->
            list.forEachIndexed { index, item ->
                val row = currentRow + index / Constants.COLS
                val col = index % Constants.COLS
                val slot = BankSlot()

                val button = buttonsMap.computeIfAbsent(listIndex * 1000 + index) {
                    BankItemButton(bot,item, slot).apply {
                        setOnAction {
                            if (selectedItemButton != this) {
                                selectedItemButton?.isSelected = false
                                selectedItemButton = this
                                isSelected = true
                            } else {
                                isSelected = false
                                selectedItemButton = null
                            }
                        }
                    }
                }

                if (bot.botComponents.bankDManager.getRecommendedItemIds().contains(item.id)) slot.addPopularEffect()
                slot.children.add(button)
                add(slot, col, row)
            }

            currentRow += (list.size + Constants.COLS - 1) / Constants.COLS
        }
    }
    fun updateBankForLoot() {
        children.clear()
        lootButtonsMap.clear() // Clear the previous mappings

        val lootItems = bot.botComponents.bankDManager.getLootItems()
        lootItems.forEachIndexed { index, item ->
            val row = index / Constants.COLS
            val col = index % Constants.COLS
            val slot = LootSlot()
            val price = bot.botComponents.bankDManager.itemPrices[item] ?: 0
            val button = LootItemButton(bot,item, slot, bot.botComponents.bankDManager.getLootItemIds(), price)
            lootButtonsMap[item.id] = button // Store button in the map
            Platform.runLater {
                slot.alignment = Pos.CENTER
                slot.children.add(button)
                add(slot, col, row)
            }
        }
    }
    fun updateLootItemPrice(itemId: Int, price: Int) {
        lootButtonsMap[itemId]?.updatePrice(price)
    }

    fun updateLootItemsStyle() {
        children.forEach { node ->
            if (node is LootSlot) {
                val button = node.children.find { it is LootItemButton<*> } as? LootItemButton<*>
                button?.updateStyleBasedOnValue()
            }
        }
    }
}