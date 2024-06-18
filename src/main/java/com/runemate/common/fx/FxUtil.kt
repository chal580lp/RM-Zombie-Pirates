package com.runemate.common.fx

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.bot.BotConfig

import com.runemate.common.item.Loot
import com.runemate.common.util
import com.runemate.game.api.hybrid.util.Resources
import com.runemate.common.fx.bank.BankDisplay
import com.runemate.common.framework.core.TaskMachine
import javafx.animation.RotateTransition
import javafx.animation.ScaleTransition
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.util.Duration
import kotlin.system.measureTimeMillis


object FxUtil {
    private val log = getLogger("FxUtil")
    val lootPrices = mutableMapOf<Loot, Int>()
    val smallFont17 = Font.loadFont(Resources.getAsStream("fonts/runescape-small.ttf"), 17.0)
        ?: throw IllegalArgumentException("Font not found")
    val smallFont20 = Font.loadFont(Resources.getAsStream("fonts/runescape-small.ttf"), 20.0)
        ?: throw IllegalArgumentException("Font not found")
    val smallFont28 = Font.loadFont(Resources.getAsStream("fonts/runescape-small.ttf"), 28.0)
        ?: throw IllegalArgumentException("Font not found")
    val ufFont = Font.font("DialogInput ", 15.0) ?: throw IllegalArgumentException("Font not found")


    val DARK_ORANGE_INTERFACE_TEXT: Color = Color.rgb(255, 152, 31)

    fun <TSettings : BotConfig>getLootPrices(bankDisplay: BankDisplay<TSettings>, loot: Set<Loot>): MutableMap<Loot, Int> {
        if (lootPrices.isEmpty()) {
            measureTimeMillis {
                loot.forEach { item ->
                    val adjustedId = if (item.noted) item.id - 1 else item.id
                    val price = util.getPrice(adjustedId, item.gameName)
                    lootPrices[item] = price
                    //log.debug("Fetched price for ${item.gameName}: $price gp")

                    // Update the corresponding button in the UI
                    Platform.runLater {
                        bankDisplay.bankGrid.updateLootItemPrice(item.id,price)
                    }
                }
            }
        }
        return lootPrices
    }

    fun scaleTo(node: Node, scale: Double, duration: Duration) {
        val scaleTransition = ScaleTransition(duration, node).apply {
            toX = scale
            toY = scale
            isAutoReverse = false
        }
        scaleTransition.play()
    }

    fun rotateTo(node: Node, angle: Double, duration: Duration, play: Boolean = false) {
        val rotateTransition = RotateTransition(duration, node).apply {
            byAngle = angle
            isAutoReverse = false
        }
        if (play) {
            rotateTransition.play()
        }
    }

    fun <TSettings : BotConfig>getItemName(bot: TaskMachine<TSettings>, id: Int): String {
        bot.botComponents.bankDManager.listsOfBankItems.forEach { list ->
            list.forEach { item ->
                if (item.id == id) {
                    return item.name
                }
            }
        }
        bot.botComponents.invDManager.requiredStartItems.forEach() { item ->
            if (item.id == id) {
                return item.name
            }
        }
        return "UNSET"
    }
}