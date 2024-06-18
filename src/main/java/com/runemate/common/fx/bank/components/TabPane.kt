package com.runemate.common.fx.bank.components

import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.game.api.hybrid.util.Resources
import com.runemate.common.fx.FileManager
import com.runemate.common.fx.FxUtil
import com.runemate.common.framework.core.TaskMachine
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Popup

class TabPane<TSettings : BotConfig>(val bot: TaskMachine<TSettings>, private val displayEventHandler: DisplayEventHandler<TSettings>) : HBox() {
    private var selectedTabButton: Button? = null
    var selectedTab = 0
    private val tabImage = Image(Resources.getAsStream("images/bank_tab.png"))
    private val tabImageClicked = Image(Resources.getAsStream("images/bank_tab_clicked.png"))
    val tabBackground = Background(
        BackgroundImage(
            tabImage,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.DEFAULT,
            BackgroundSize(tabImage.width, tabImage.height * 1.3, true, true, true, true)
        )
    )
    val tabBackgroundClicked = Background(
        BackgroundImage(
            tabImageClicked,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.DEFAULT,
            BackgroundSize(tabImage.width , tabImage.height , true, true, true, true)
        )
    )

    init {
        spacing = 0.0
        padding = Insets(4.0, 1.0, 0.0, 60.0)
        children.add(createAllItemsTabButton())
        children.add(createLootTabButton())
    }

    private fun createAllItemsTabButton(): Button {
        val image = Image(Resources.getAsStream("images/bank_tab_icon.png"))
        return Button().apply {
            graphic = ImageView(image).apply {
                minWidth = 45.0
                minHeight = 45.0
                isPreserveRatio = true
                padding = Insets(0.0, 0.0, 0.0, 0.0)
            }
            minWidth = tabImage.width
            minHeight = tabImage.height
            selectedTabButton = this
            background = tabBackgroundClicked // first tab is always clicked on initialization of bank
            val popup = Popup()

            val label1 = Label("View all items").apply {
                font = FxUtil.smallFont17
                style = "-fx-text-fill: white; -fx-font-smoothing-type: gray;"
                effect = DropShadow(5.0, Color.BLACK)
            }

            // Create an HBox to hold the labels
            val hbox = HBox(2.0, label1).apply {
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
                if (selectedTab != 0) {
                    background = tabBackgroundClicked
                }
            }
            setOnMouseExited {
                popup.hide()  // Hide when mouse exits button
                if (selectedTab != 0) {
                    background = tabBackground
                }
            }
            setOnAction {
                selectedTabButton?.background = tabBackground
                selectedTabButton = this
                selectedTab = 0
                displayEventHandler.handleAllItemsTabClick()
                background = tabBackgroundClicked
            }
        }
    }

    private fun createLootTabButton(): Button {
        val itemImage = FileManager.getImageForItem(22586)
        return Button().apply {
            graphic = ImageView(itemImage).apply {
                minWidth = 45.0
                minHeight = 45.0
                isPreserveRatio = true
                padding = Insets(0.0, 0.0, 0.0, 0.0)
            }
            minWidth = tabImage.width
            minHeight = tabImage.height
            background = tabBackground

            val popup = Popup()

            val description = Label("View tab").apply {
                font = FxUtil.smallFont17
                style = "-fx-text-fill: white; -fx-font-smoothing-type: gray;"
                effect = DropShadow(5.0, Color.BLACK)
            }
            val description2 = Label("Loot").apply {
                font = FxUtil.smallFont17
                textFill = FxUtil.DARK_ORANGE_INTERFACE_TEXT
                effect = DropShadow(5.0, Color.BLACK)
            }

            // Create an HBox to hold the labels
            val hbox = HBox(2.0, description, description2).apply {
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
                if (selectedTab != -1) {
                    background = tabBackgroundClicked
                }
            }
            setOnMouseExited {
                popup.hide()  // Hide when mouse exits button
                if (selectedTab != -1) {
                    background = tabBackground
                }
            }
            setOnAction {
                selectedTabButton?.background = tabBackground
                selectedTabButton = this
                selectedTab = -1 // Special index for loot tab
                displayEventHandler.handleLootTabClick()
                background = tabBackgroundClicked
            }
        }
    }
}