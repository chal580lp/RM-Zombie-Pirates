package com.runemate.common.fx.inventory

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.game.api.hybrid.util.Resources

import com.runemate.common.framework.core.TaskMachine
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.layout.*

class InventoryDisplay<TSettings : BotConfig>(private val bot: TaskMachine<TSettings>) : VBox() {

    val slotSize = 42.0
    val cols = 4
    val rows = (28 + cols - 1) / cols
    private val log = getLogger("InventoryDisplay")


    private val inventoryGrid = GridPane().apply {
        hgap = 1.0
        vgap = 2.0
        style = """

            -fx-border-color: white;
            -fx-border-width: 0;
        """.trimIndent()
        padding = Insets(1.0,0.0,1.0,40.0)
        background = Background(
            BackgroundImage(
                Image(Resources.getAsStream("images/inv2.png")),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize(245.0, 300.0, false, false, false, false)
            )
        )
        minHeight = 290.0
        //minWidth = cols * slotSize + (cols - 1) * (hgap + 6)
        minWidth = 245.0
        //maxHeight = fixedHeight
    }
    private var selectedItemButton: InventoryItemButton<TSettings>? = null

    init {
        log.debug("InventoryDisplay init")
        style = "-fx-background-color: transparent; -fx-padding: 10; -fx-border-color: gray; -fx-border-width: 0;"
        val headerBar = HBox().apply {
            spacing = 0.0
            //style = "-fx-background-color: #1f1f1f; -fx-padding: 0;"
            children.add(StackPane().apply {
                children.add(Button("").apply {
                    style = "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: red; -fx-border-width: 0;"
                    StackPane.setAlignment(this, Pos.TOP_LEFT)
                })
            })
            children.add(StackPane().apply {
                children.add(Label("").apply {
                    style = "-fx-font-size: 14px; -fx-text-fill: white; -fx-border-color: red; -fx-border-width: 0;"
                    StackPane.setAlignment(this, Pos.TOP_CENTER)
                })
                HBox.setHgrow(this, Priority.ALWAYS)
            })
            children.add(StackPane().apply {
                children.add(Button("").apply {
                    style = "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: red; -fx-border-width: 0;"
                    StackPane.setAlignment(this, Pos.TOP_RIGHT)
                })
            })
            minHeight = 40.0
            background = Background(
                BackgroundImage(
                    Image(Resources.getAsStream("images/inv_top.png")),
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.DEFAULT,
                    BackgroundSize(245.0, 40.0, false, false, false, false)
                )
            )
        }

        children.add(headerBar)
        setupGrid()
        updateInventory()
        bot.botComponents.invDManager.inventoryProperty.addListener { _, _, _ ->
            Platform.runLater {
                updateInventory()
                bot.botComponents.invManager.updateInventory(bot.botComponents.invDManager.getInventory())
            }
        }
        val footerBar = HBox().apply {
            spacing = 0.0
            //style = "-fx-background-color: #1f1f1f; -fx-padding: 0;"
            children.add(StackPane().apply {
                children.add(Button("").apply {
                    style = "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: red; -fx-border-width: 0;"
                    StackPane.setAlignment(this, Pos.TOP_LEFT)
                })
            })
            children.add(StackPane().apply {
                children.add(Label("").apply {
                    style = "-fx-font-size: 14px; -fx-text-fill: white; -fx-border-color: red; -fx-border-width: 0;"
                    StackPane.setAlignment(this, Pos.TOP_CENTER)
                })
                HBox.setHgrow(this, Priority.ALWAYS)
            })
            children.add(StackPane().apply {
                children.add(Button("").apply {
                    style = "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: red; -fx-border-width: 0;"
                    StackPane.setAlignment(this, Pos.TOP_RIGHT)
                })
            })
            minHeight = 40.0
            background = Background(
                BackgroundImage(
                    Image(Resources.getAsStream("images/inv_bot3.png")),
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.DEFAULT,
                    BackgroundSize(245.0, 40.0, false, false, false, false)
                )
            )
        }
        children.add(footerBar)

        // Set fixed height
        val fixedHeight = 400.0
        //prefHeight = fixedHeight
        minHeight = fixedHeight
        maxHeight = fixedHeight
    }

    private fun setupGrid() {

        children.add(inventoryGrid)
        alignment = Pos.CENTER
    }

    private fun updateInventory() {
        if (bot.botComponents.invDManager.getInventory().isEmpty()) return
        inventoryGrid.children.clear()
        val list = bot.botComponents.invDManager.getInventory()

        // Iterate over the items in the list
        var index = 0
        list.forEach { item ->
            // Iterate over each item's amount
            for (i in 0 until item.quantity) {
                val row = index / cols
                val col = index % cols
                val slot = InventorySlot()
                val button = InventoryItemButton(bot,item, slot)

                button.setOnAction {
                    if (selectedItemButton != button) {
                        selectedItemButton?.isSelected = false
                        selectedItemButton = button
                        button.isSelected = true
                    } else {
                        button.isSelected = false
                        selectedItemButton = null
                    }
                }
                slot.children.add(button)
                inventoryGrid.add(slot, col, row)
                index++
            }
        }
    }
}


