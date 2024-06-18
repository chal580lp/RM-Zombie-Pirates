package com.runemate.common.fx.bank

import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.item.BaseItem
import com.runemate.common.fx.FileManager
import com.runemate.common.fx.FxUtil
import com.runemate.common.framework.core.TaskMachine
import javafx.animation.*
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.effect.DropShadow
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Popup
import javafx.util.Duration

class BankItemButton<TSettings : BotConfig>(
    val bot: TaskMachine<TSettings>,
    item: BaseItem,
    private val slot: BankSlot) : Button() {
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
        style = "-fx-background-color: transparent; -fx-padding: 0;"

        val popup = Popup()
        val label1 = Label("Withdraw-1 ").apply {
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

            style = "-fx-background-color: rgba(71,63,53, 0.7); -fx-border-color: rgba(41,38,31,0.5); -fx-border-width: 1;"
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
                bot.botComponents.bankDManager.withdrawFromBank(item, 1)
            }
            if (event.button == MouseButton.SECONDARY) {
                val contextMenuPopup = Popup()
                val customContextMenu = createBankWithdrawVBox(item, contextMenuPopup).apply {
                    setOnMouseExited {
                        contextMenuPopup.hide()
                    }
                }
                contextMenuPopup.content.add(customContextMenu)
                contextMenuPopup.show(this, event.screenX, event.screenY)
                event.consume()
            }
        }
        setOnMousePressed {
            opacity = 0.7
        }
        setOnMouseReleased {
            opacity = 1.0
        }
    }
    private fun createBankWithdrawVBox(item: BaseItem, popup: Popup): VBox {
        return VBox(0.0).apply {
            val chooseOptionLabel = Label("Choose Option").apply {
                font = FxUtil.smallFont20
                textFill = Color.rgb(95, 86, 72)
                //maxHeight = 14.0
                padding = Insets(1.0, 3.0, 1.0, 3.0)
            }

            val borderPanel = HBox().apply {
                style = "-fx-border-color: black; -fx-border-width: 1;"
                maxHeight = 14.0
            }

            val chooseOptionPanel = HBox(chooseOptionLabel).apply {
                style = "-fx-background-color: black; -fx-border-color: rgb(93, 84, 71); -fx-border-width: 1;"
                alignment = Pos.CENTER_LEFT
                prefWidthProperty().bind(borderPanel.widthProperty())
            }

            borderPanel.children.add(chooseOptionPanel)

            padding = Insets(0.0)

            children.add(borderPanel)

            listOf("1", "5", "10", "All").forEach { amount ->
                val button = Button("Withdraw-$amount ").apply {
                    font = FxUtil.smallFont20
                    style = "-fx-font-smoothing-type: gray; -fx-border-radius: 0; -fx-background-radius: 0; -fx-background-color: transparent"
                    textFill = Color.WHITE
                    maxHeight = 14.0
                    padding = Insets(0.0, 3.0, 0.0, 3.0)
                    setOnMouseEntered {
                        textFill = Color.YELLOW
                    }
                    setOnMouseExited {
                        textFill = Color.WHITE
                    }
                    setOnMouseClicked {
                        println("Withdraw $amount")
                        when (amount) {
                            "All" -> bot.botComponents.bankDManager.withdrawAllFromBank(item)
                            else -> bot.botComponents.bankDManager.withdrawFromBank(item, amount.toInt())
                        }
                        popup.hide()
                    }
                }

                val itemNameLabel = Label(item.name).apply {
                    font = FxUtil.smallFont20
                    //style = "-fx-font-size: 16px;"
                    textFill = FxUtil.DARK_ORANGE_INTERFACE_TEXT
                    maxHeight = 14.0
                    padding = Insets(0.0, 0.0, 0.0, 0.0)
                }

                val hbox = HBox(button, itemNameLabel).apply {
                    alignment = Pos.CENTER_LEFT
                    spacing = 0.0
                    setOnMouseClicked {
                        println("Withdraw $amount")
                        when (amount) {
                            "All" -> bot.botComponents.bankDManager.withdrawAllFromBank(item)
                            else -> bot.botComponents.bankDManager.withdrawFromBank(item, amount.toInt())
                        }
                        popup.hide()
                    }
                    setOnMouseEntered {
                        button.textFill = Color.YELLOW
                    }
                    setOnMouseExited {
                        button.textFill = Color.WHITE
                    }
                }

                children.add(hbox)
            }
            style = "-fx-background-color: rgb(93, 84, 71); -fx-border-color: black; -fx-border-width: 1;"
        }
    }
}

class BankSlot : StackPane() {
    init {
        style = """
            -fx-background-color: transparent;
            -fx-padding: 1;
        """.trimIndent()
        prefWidth = 45.0
        prefHeight = 45.0
        minWidth = prefWidth
        minHeight = prefWidth
        maxWidth = prefWidth
        maxHeight = prefHeight
//        if (true) {
//            val hoverAnimation = TranslateTransition(Duration.millis(2000.0), this).apply {
//                fromY = 0.0
//                toY = -5.0
//                cycleCount = Timeline.INDEFINITE
//                isAutoReverse = true
//            }
//
//            hoverAnimation.play()
//        }
    }
    fun addPopularEffect() {
        // Hover animation
        val hoverAnimation = TranslateTransition(Duration.millis(2000.0), this).apply {
            fromY = 0.0
            toY = -5.0
            cycleCount = Timeline.INDEFINITE
            isAutoReverse = true
        }
        hoverAnimation.play()
    }
}