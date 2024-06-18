package com.runemate.common

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.item.EnergyRestore
import com.runemate.common.item.Food
import com.runemate.common.item.Restore
import com.runemate.common.item.isAvailable
import com.runemate.game.api.hybrid.local.hud.interfaces.Health
import com.runemate.game.api.hybrid.location.navigation.Traversal
import com.runemate.game.api.osrs.local.hud.interfaces.Prayer
import com.runemate.game.api.script.framework.AbstractBot
import com.runemate.game.api.script.framework.listeners.EngineListener

@Suppress("unused")
object Consumeables : EngineListener {
    private const val TICK_PENALTY = 3
    private lateinit var bot: AbstractBot //TODO:
    private var minHealth: Int = 0
    private var minPrayer: Int = 0
    private var tickPenalty = 0
    private var tick = 0
    private val log = getLogger("Consumeables")

    fun initialize(bot: AbstractBot, minHealth: Int, minPrayer: Int) {
        this.bot = bot
        this.minHealth = minHealth
        this.minPrayer = minPrayer
    }

    private fun checkAndConsume(): Boolean {
        if (bot.isPaused) return false

        when {
            shouldEatFood() -> {
                Food.eatAny()
                applyTickPenalty()
                return true
            }
            shouldRestorePrayer() -> {
                Restore.restorePrayer()
                applyTickPenalty()
                return true
            }
            shouldDrinkStamina() -> {
                if (Traversal.drinkStaminaEnhancer()) {
                    applyTickPenalty()
                    return true
                }
            }
            Health.isPoisoned() -> {
                if (Restore.isAntiPoisonAvailable()) {
                    Restore.curePoison()
                }
            }
        }

        return false
    }

    private fun shouldEatFood(): Boolean {
        return Health.getCurrent() < minHealth && Food.haveValidFood()
    }

    private fun shouldRestorePrayer(): Boolean {
        return Prayer.getPoints() < minPrayer && Restore.isPrayerRestoreAvailable()
    }

    private fun shouldDrinkStamina(): Boolean {
        return Traversal.getRunEnergy() < 15 && EnergyRestore.StaminaPotion.isAvailable()
    }

    private fun applyTickPenalty() {
        tickPenalty = TICK_PENALTY
    }

     override fun onTickStart() {
         if (!::bot.isInitialized) {
             println("Consumeables is not initialized properly.")
             return
         }
        tick++
        if (tickPenalty > 0) {
            tickPenalty--
            return
        }
        if (checkAndConsume()){
            log.debug("checkAndConsumed on tick: $tick")
            tickPenalty++
        }

    }
}
