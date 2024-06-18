package com.runemate.common.fx.bank

import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.item.Loot
import com.runemate.common.fx.FileManager
import com.runemate.common.fx.FxUtil
import com.runemate.common.framework.core.TaskMachine
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.effect.DropShadow
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Popup

class LootItemButton<TSettings : BotConfig>(
    val bot: TaskMachine<TSettings>,
    val item: Loot,
    private val slot: LootSlot,
    loadedLootItemIds: List<Int>,
    private var price: Int // Assuming this is the event handler
) : Button() {
    val settings  = bot.settings()
    private var take = if (loadedLootItemIds.isEmpty()) {
        item.quantities.any { quantity -> quantity * price > settings.minLootValue }
    } else {
        loadedLootItemIds.contains(item.id)
    }
    private var isSelected = take

    private lateinit var priceLabel: Label
    private lateinit var takeLabel: Label

    init {
        setupGraphic()
        setupTooltip()
//        setupAction()
//        setupContextMenu()
        slot.updateStyle(isSelected)
    }

    private fun setupGraphic() {
        val imageView = ImageView(FileManager.getImageForItem(item.id)).apply {
            isPreserveRatio = true
            isSmooth = true
            fitWidth = slot.prefWidth - 4
            fitHeight = slot.prefHeight - 6
            alignment = Pos.TOP_CENTER
            translateX = 4.0
        }
        priceLabel = Label().apply {
            textFill = Color.YELLOW
            style = "-fx-font-size: 11px;"
            alignment = Pos.CENTER
            effect = DropShadow(1.0, Color.BLACK).apply {
                spread = 1.0
            }
        }

        takeLabel = Label("0").apply {
            textFill = Color.GOLD
            style = "-fx-font-size: 10px;"
            alignment = Pos.TOP_LEFT
            padding = Insets(2.0)
            isVisible = !take
        }

        val buttonContent = VBox(imageView).apply { alignment = Pos.CENTER }
        graphic = StackPane(buttonContent, priceLabel, takeLabel).apply {
            StackPane.setAlignment(priceLabel, Pos.BOTTOM_CENTER)
            StackPane.setAlignment(takeLabel, Pos.TOP_LEFT)
        }

        style = "-fx-background-color: transparent; -fx-padding: 0;"
        updateStyleBasedOnValue()
        if (price != 0) updatePrice(price)
    }

    private fun setupTooltip() {
        val popup = Popup()
        val label1 = Label(item.gameName).apply {
            font = FxUtil.smallFont17
            textFill = FxUtil.DARK_ORANGE_INTERFACE_TEXT
            effect = DropShadow(5.0, Color.BLACK)
        }

        val label2 = Label(buildTooltipText()).apply {
            font = FxUtil.smallFont17
            style = "-fx-text-fill: white; -fx-font-smoothing-type: gray;"
            effect = DropShadow(5.0, Color.BLACK)
        }

        val vbox = VBox(2.0, label1, label2).apply {
            padding = Insets(3.0)
            style = "-fx-background-color: rgba(71,63,53, 0.7); -fx-border-color: rgba(41,38,31,0.5); -fx-border-width: 1;"
        }

        popup.content.add(vbox)

        setOnMouseMoved { event ->
            val screenPos = localToScreen(event.x, event.y)
            popup.show(this, screenPos.x + 10, screenPos.y + 20)
        }

        setOnMouseEntered {
            val screenPos = localToScreen(boundsInLocal.minX, boundsInLocal.maxY)
            popup.show(this, screenPos.x + 10, screenPos.y + 20)
        }

        setOnMouseExited {
            popup.hide()
        }

        setOnMousePressed {
            opacity = 0.7
        }

        setOnMouseReleased {
            opacity = 1.0
        }
    }

    private fun setupAction() {
        setOnAction {
            take = !take
            isSelected = take
            slot.updateStyle(isSelected)

            takeLabel.isVisible = !take
            priceLabel.isVisible = take

            // Call the event handler to update the display if needed
            }
    }

    private fun setupContextMenu() {
        contextMenu = ContextMenu().apply {
            items.add(MenuItem("Loot All").apply { setOnAction { /* Handle looting all items */ } })
            item.quantities.forEach { quantity ->
                items.add(MenuItem("Loot $quantity").apply { setOnAction { /* Handle looting the specific quantity */ } })
            }
        }
    }

    private fun buildTooltipText() = """
        Quantity: ${item.quantities.joinToString("-")}
        Price: ${formatPrice(price)}
    """.trimIndent()

    private fun formatPrice(price: Int) = when {
        price >= 1_000_000 -> "${price / 1_000_000}m"
        price >= 1_000 -> "${price / 1_000}k"
        else -> price.toString()
    }

    fun updateStyleBasedOnValue() {
        val totalValue = price * item.maxQuantity()
        if (totalValue > settings.minLootValue) {
            slot.updateStyle(true)
            takeLabel.isVisible = false
            priceLabel.isVisible = true
        } else {
            slot.updateStyle(false)
            takeLabel.isVisible = true
            priceLabel.isVisible = false
        }
        updatePriceLabelFormat()
    }

    private fun updatePriceLabelFormat() {
        val minPrice = price * item.minQuantity()
        val maxPrice = price * item.maxQuantity()
        val displayPrice = if (item.maxQuantity() > 2 && item.quantities.size > 1) {
            if (settings.minLootValue in (minPrice + 1) until maxPrice) {
                "${formatPrice(settings.minLootValue)}-${formatPrice(maxPrice)}"
            } else {
                "${formatPrice(minPrice)}-${formatPrice(maxPrice)}"
            }
        } else {
            formatPrice(maxPrice)
        }
        priceLabel.text = displayPrice
    }

    fun updatePrice(newPrice: Int) {
        price = newPrice
        updateStyleBasedOnValue()
    }
}

class LootSlot : StackPane() {
    init {
        style = """
            -fx-background-color: transparent;
            -fx-padding: 0;
        """.trimIndent()
        prefWidth = 48.0
        prefHeight = 54.0
        minWidth = prefWidth
        minHeight = prefWidth
        maxWidth = prefWidth
        maxHeight = prefHeight
    }

    fun updateStyle(isSelected: Boolean) {
        opacity = if (isSelected) 1.0 else 0.5
    }
}
