package com.runemate.common.fx.inventory

import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.item.BaseItem
import com.runemate.common.item.SetItem
import com.runemate.common.fx.FileManager
import com.runemate.common.fx.FxUtil
import com.runemate.common.framework.core.TaskMachine
import javafx.animation.PauseTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.effect.DropShadow
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Popup
import javafx.util.Duration


class InventoryItemButton<TSettings : BotConfig>(
    val bot: TaskMachine<TSettings>,
    val item: BaseItem,
    val slot: InventorySlot) : Button() {
    var isSelected = false


    init {
        val image = FileManager.getImageForItem(item.id)
        val imageView = ImageView(image).apply {
            isPreserveRatio = true
            isSmooth = true
            fitWidth = slot.prefWidth - 2
            fitHeight = slot.prefHeight - 2
        }
        graphic = imageView
        style = "-fx-background-color: transparent;"
        alignment = Pos.CENTER

        val popup = Popup()
        val label1 = Label("Deposit-1 ").apply {
            font = FxUtil.smallFont17
            style = "-fx-text-fill: white; -fx-font-smoothing-type: gray;"
            effect = DropShadow(5.0, Color.BLACK)

        }

        // Create the second label
        val label2 = Label(item.name).apply {
            font = FxUtil.smallFont17
            textFill = FxUtil.DARK_ORANGE_INTERFACE_TEXT
            effect = DropShadow(5.0, Color.BLACK)
        }

        // Create an HBox to hold the labels
        val hbox = HBox(2.0, label1, label2).apply {
            padding = Insets(3.0)

            style =
                "-fx-background-color: rgba(71,63,53, 0.7); -fx-border-color: rgba(41,38,31,0.5); -fx-border-width: 1;"
        }

        // Add the HBox to the Popup
        popup.content.add(hbox)

        // Position the Popup relative to the button and mouse movement
        setOnMouseMoved { event ->
            val screenPos = localToScreen(event.x, event.y)
            popup.show(this, screenPos.x + 10, screenPos.y + 20) // Position the popup near the mouse
        }

        setOnMouseEntered {
            val screenPos = localToScreen(boundsInLocal.minX, boundsInLocal.maxY)
            popup.show(this, screenPos.x + 10, screenPos.y + 20)  // Show directly below the button
        }

        setOnMouseExited {
            popup.hide()  // Hide when mouse exits button
        }

        setOnAction {
            isSelected = !isSelected
        }
        setOnMouseClicked { event ->
            if (event.button == MouseButton.PRIMARY) {
                if (item is SetItem && bot.botComponents.invDManager.requiredStartItems.contains(item)) {
                    val popup2 = Popup()
                    val label3 = Label("This item is Required!").apply {
                        font = FxUtil.smallFont17
                        style = "-fx-text-fill: red; -fx-font-smoothing-type: gray;"
                        effect = DropShadow(5.0, Color.BLACK)
                    }

                    val hbox2 = HBox(2.0, label3).apply {
                        padding = Insets(3.0)
                        style =
                            "-fx-background-color: rgba(71,63,53, 0.7); -fx-border-color: rgba(41,38,31,0.5); -fx-border-width: 1;"
                    }

                    popup2.content.add(hbox2)
                    val screenPos = localToScreen(event.x, event.y)
                    popup2.show(this, screenPos.x, screenPos.y)

                    // Create a PauseTransition to hide the popup after 3 seconds
                    val delay = PauseTransition(Duration.seconds(1.5))
                    delay.setOnFinished {
                        popup2.hide()
                    }
                    delay.play()
                } else {
                    bot.botComponents.invDManager.removeFromInventory(item, 1)
                }
            }
        }
        setOnMousePressed {
            opacity = 0.7
        }
        setOnMouseReleased {
            opacity = 1.0
        }

        contextMenu = createInventoryDepositContextMenu(item as SetItem)
    }


    private fun createInventoryDepositContextMenu(item: SetItem): ContextMenu {
        return ContextMenu().apply {
            listOf("1", "5", "10", "All").forEach { amount ->
                items.add(MenuItem("Deposit $amount").apply {
                    setOnAction {
                        when (amount) {
                            "All" -> bot.botComponents.invDManager.removeFromInventory(item, removeAll = true)
                            else -> bot.botComponents.invDManager.removeFromInventory(item, amount.toInt())
                        }
                    }
                })
            }
        }
    }
}

class InventorySlot : StackPane() {
    init {
        style = """
            -fx-background-color: transparent;
        """.trimIndent()
        prefWidth = 40.0
        prefHeight = 40.0
        minWidth = prefWidth
        minHeight = prefHeight
        maxWidth = prefWidth
        maxHeight = prefHeight
    }
}