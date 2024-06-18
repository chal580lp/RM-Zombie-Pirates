package com.runemate.common.fx.bank.components

import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.framework.core.TaskMachine
import com.runemate.game.api.hybrid.util.Resources
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color

class Footer<TSettings : BotConfig>(private val bot: TaskMachine<TSettings>, private val displayEventHandler: DisplayEventHandler<TSettings>) : StackPane() {
    private val minLootButtons = createLootItemsAboveButtons(listOf(0, 1, 2, 3, 4, 5, 7, 10, 15, 25))
    private val bankInvButton = createBankInvButton()
    val settings = bot.settings()

    init {
        children.add(bankInvButton)
        setAlignment(bankInvButton, Pos.BOTTOM_RIGHT)
        setMargin(bankInvButton, Insets(5.0, 10.0, 10.0, 0.0))

        //updateButtonStyles(settings.minLootValue / 1000, minLootButtons)
    }

    private fun createLootItemsAboveButtons(values: List<Int>): HBox {
        val buttonBox = HBox().apply {
            spacing = 3.0
            alignment = Pos.CENTER_LEFT
            padding = Insets(0.0, 0.0, 0.0, 5.0)
        }

        values.forEach { value ->
            val button = Button("${value}k").apply {
                prefWidth = 10.0 * value.toString().length.toDouble() + 25
                prefHeight = 25.0
                textFill = Color.WHITE
                style = "-fx-font-size: 12px; -fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 1px; -fx-background-radius: 0;"
                setOnAction { handleButtonAction(value, buttonBox) }
                padding = Insets(0.0, 0.0, 0.0, 0.0)
            }

            buttonBox.children.add(button)
        }

        return buttonBox
    }

    private fun createBankInvButton(): Button {
        val bankInvImage = Image(Resources.getAsStream(Configuration.CLEAR_BUTTON_IMAGE))
        return Button().apply {
            graphic = ImageView(bankInvImage).apply {
                fitWidth = 40.0
                fitHeight = 40.0
            }
            setOnAction {
                displayEventHandler.handleDepositAllClick()
            }
            style = "-fx-background-color: transparent; -fx-padding: 0;"
            onMouseEntered = javafx.event.EventHandler {
                opacity = 0.7
            }
            onMouseExited = javafx.event.EventHandler {
                opacity = 1.0
            }
        }
    }

    private fun handleButtonAction(value: Int, buttonBox: HBox) {
        // Update the loot items above value
        displayEventHandler.updateLootDisplay(value * 1000)
        //settings.minLootValue = value * 1000


        // Update the button styles
        updateButtonStyles(value, buttonBox)
    }

    private fun updateButtonStyles(selectedValue: Int, buttonBox: HBox) {
        val higherValueStyle = "-fx-text-fill: gold; -fx-font-size: 12px; -fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 1px; -fx-background-radius: 0;"
        val lowerValueStyle = "-fx-text-fill: gray; -fx-font-size: 12px; -fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 1px; -fx-background-radius: 0;"
        val selectedStyle = "-fx-text-fill: gold; -fx-background-color: rgba(0, 255, 0, 0.1); -fx-border-color: black; -fx-border-width: 1px; -fx-background-radius: 0;"
        val hoverStyle = "; -fx-background-color: rgba(255, 255, 255, 0.2);"

        buttonBox.children.forEach { btn ->
            val button = btn as Button
            val btnValue = button.text.replace("k", "").toInt()
            button.style = if (btnValue > selectedValue) {
                higherValueStyle
            } else {
                lowerValueStyle
            }

            button.setOnMouseEntered {
                if (!button.style.contains(hoverStyle)) {
                    button.style += hoverStyle
                }
            }
            button.setOnMouseExited {
                button.style = button.style.replace(hoverStyle, "")
            }
        }

        // Select the clicked button
        val selectedButton = buttonBox.children.first { (it as Button).text == "${selectedValue}k" } as Button
        selectedButton.style = selectedStyle
    }

    fun showMinLootButtons() {
        if (!children.contains(minLootButtons)) {
            children.add(minLootButtons)
            setAlignment(minLootButtons, Pos.CENTER_LEFT)
            setMargin(minLootButtons, Insets(0.0, 0.0, 10.0, 8.0))

        }
    }

    fun hideMinLootButtons() {
        children.remove(minLootButtons)
    }

    fun updateMinLootButtons() {
        Platform.runLater {
            updateButtonStyles(settings.minLootValue / 1000, minLootButtons)
        }
    }
}
