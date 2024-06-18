// HeaderBar.kt
package com.runemate.common.fx.bank.components

import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.game.api.hybrid.util.Resources
import com.runemate.common.fx.FxUtil
import com.runemate.common.framework.core.TaskMachine
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color

class HeaderBar<TSettings : BotConfig>(private val bot: TaskMachine<TSettings>, private val displayEventHandler: DisplayEventHandler<TSettings>) :  HBox() {
    val titleLabel = Label("Bank Tab").apply {
        font = FxUtil.smallFont28
        padding = Insets(10.0, 5.0, 0.0, 5.0)
        textFill = Color.rgb(221, 134, 29)
        effect = DropShadow(5.0, Color.BLACK)
        StackPane.setAlignment(this, Pos.CENTER)
    }

    init {
        spacing = 0.0
        minHeight = 35.0
        children.add(createEmptyButton())
        children.add(createTitleLabelContainer())
        children.add(createCloseButton())
    }

    private fun createEmptyButton(): StackPane {
        return StackPane().apply {
            children.add(Button("").apply {
                style = "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: red; -fx-border-width: 0;"
                StackPane.setAlignment(this, Pos.TOP_LEFT)
            })
        }
    }

    private fun createTitleLabelContainer(): StackPane {
        return StackPane().apply {
            children.add(titleLabel)
            HBox.setHgrow(this, Priority.ALWAYS)
            StackPane.setAlignment(this, Pos.CENTER)
        }
    }

    private fun createCloseButton(): StackPane {
        return StackPane().apply {
            children.add(Button("").apply {
                style = "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: red; -fx-border-width: 0;"
                StackPane.setAlignment(this, Pos.TOP_RIGHT)
                val image = Image(Resources.getAsStream(Configuration.CLOSE_BUTTON_IMAGE))
                val xGraphic = ImageView(image).apply {
                    minWidth = 34.0
                    minHeight = 34.0
                    isPreserveRatio = true
                    padding = Insets(7.0, 7.0, 0.0, 0.0)
                }
                val xClickedImage = Image(Resources.getAsStream(Configuration.CLOSE_BUTTON_CLICKED_IMAGE))
                val xClickedGraphic = ImageView(xClickedImage).apply {
                    minWidth = 34.0
                    minHeight = 34.0
                    isPreserveRatio = true
                    padding = Insets(7.0, 7.0, 0.0, 0.0)
                }
                graphic = xGraphic
                setOnMouseEntered {
                    graphic = xClickedGraphic
                }
                setOnMouseExited {
                    graphic = xGraphic
                }
                setOnAction {
                    // eventHandler.handleCloseButtonClick()
                }
            })
        }
    }
}