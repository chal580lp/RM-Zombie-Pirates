package com.runemate.common

import com.runemate.game.api.script.framework.listeners.EngineListener
import com.runemate.game.api.script.framework.listeners.PlayerListener
import java.util.logging.Logger

class Consumeables(
    private var minEat: Int,
    private var isDharok: Boolean = false
) : EngineListener, PlayerListener {

    private val log: Logger = Logger.getLogger(Consumeables::class.java.name)

    //constructor(bot: NeckBot<TSettings>, minEat: Int) : this(bot, minEat, false)

    fun setDharok(isDharok: Boolean) {
        this.isDharok = isDharok
    }

    fun setMinEat(minEat: Int) {
        this.minEat = minEat
    }

//    private fun checkAndConsume() {
//        //if (bot.isPaused) return
//        if (Health.getCurrent() < minEat && !isDharok) {
//            if (!Food.eatAny() || Food.countInventory() == 0) {
//                if (Health.getCurrent() < minEat) {
//                    log.debug("No food forced to restore hp: {} mineat: {}", Health.current, minEat)
//                    //bot.forceState(NeckState.RESTORING)
//                }
//            }
//        }
//        if (Prayer.getPoints() < 5) {
//            if (!Util.restorePrayer()) {
//                log.debug("No prayer forced to restore")
//                //bot.forceState(NeckState.RESTORING)
//            }
//        }
//        if (Traversal.getRunEnergy() < 15) {
//            Traversal.drinkStaminaEnhancer()
//        }
//        if (Health.isPoisoned()) {
//            // if (!Util.curePoison()) {
//        }
//    }
}
