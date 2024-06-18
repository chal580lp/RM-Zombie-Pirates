package com.runemate.common.fx.bank

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.bot.BotConfig

import com.runemate.game.api.hybrid.util.Resources
import com.runemate.common.fx.bank.addon.MessageOverlay
import com.runemate.common.fx.bank.components.*
import com.runemate.common.framework.core.TaskMachine
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*

import javafx.scene.image.Image
import javafx.scene.layout.*

import javafx.scene.shape.Line
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import kotlin.system.measureTimeMillis


class BankDisplay<TSettings : BotConfig>(private val bot: TaskMachine<TSettings>) : VBox() {
    val displayEventHandler = DisplayEventHandler(this,bot)
    private val log = getLogger("BankDisplay")
    private val configuration = Configuration
    val header = HeaderBar(bot,displayEventHandler)
    val tabPane = com.runemate.common.fx.bank.components.TabPane(bot,displayEventHandler)
    val bankGrid = BankGrid(bot,displayEventHandler)
    val footer = Footer(bot,displayEventHandler)
    val messageOverlay = MessageOverlay(Constants.GRID_WIDTH, 380.0)
    val panes = VBox()
    private val container = StackPane()

    init {
        log.debug("BankDisplay init")
        panes.background = Background(
            BackgroundImage(
                Image(Resources.getAsStream(configuration.BANK_BACKGROUND_IMAGE)),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize(Constants.GRID_WIDTH, 380.0, false, false, false, false)
            )
        )
        panes.maxHeight = 380.0
        panes.children.add(header)
        panes.children.add(createSeparatorLine())
        panes.children.add(tabPane)
        panes.children.add(createSeparatorLine(opacity = 0.0))
        panes.children.add(ScrollPane(bankGrid).apply {
            isFitToWidth = true
            isFitToHeight = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        })
        panes.children.add(createSeparatorLine(alignment = Pos.BOTTOM_LEFT))
        panes.children.add(footer)
        container.children.add(panes)
        container.children.add(messageOverlay)
        children.add(container)
        setVgrow(footer, Priority.ALWAYS)
        setMargin(bankGrid, Insets(3.0, 0.0, 0.0, 25.0))

        messageOverlay.isMouseTransparent = true
        measureTimeMillis {
            bankGrid.updateBank()
        }.also { log.info("updateBank() took $it ms") }

        bot.botComponents.bankDManager.bankProperty.addListener { _, _, _ ->
            measureTimeMillis {
                bankGrid.updateBank()
            }.also { log.info("updateBank() on bank property change took $it ms") }
        }
        //messageOverlay.message("Hello, this is a test message!")
        log.debug("BankDisplay init done")
    }

    private fun createSeparatorLine(opacity: Double = 0.2, alignment: Pos = Pos.TOP_LEFT): Line {
        return Line().apply {
            stroke = Color.GRAY
            this.opacity = opacity
            startX = 0.0
            endXProperty().bind(bankGrid.widthProperty())
            StackPane.setAlignment(this, alignment)
        }
    }
    fun updateLootItemsStyle() {
        footer.updateMinLootButtons()
        bankGrid.updateLootItemsStyle()
    }
}
