package com.runemate.common.framework.state

import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.WorldHopper
import com.runemate.common.framework.core.*
import com.runemate.common.framework.core.addons.BotState
import com.runemate.game.api.hybrid.input.Mouse
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.script.Execution
import com.runemate.game.api.script.framework.listeners.PlayerListener
import com.runemate.game.api.script.framework.listeners.events.HitsplatEvent
import org.apache.logging.log4j.LogManager.getLogger


class HoppingState<TSettings : BotConfig>(private val bot: TaskMachine<TSettings>) : BaseState(), PlayerListener {
    private var hopped = false
    private var inCombat = false

    override fun onStart() {
        bot.updateStatus("Hopping")
        log.debug("Hopping to world started")
    }

    override fun onExit() {
       log.debug("Hopped to world finished")
    }

    override fun onPlayerHitsplat(event: HitsplatEvent) {
        if (event.source == Players.getLocal()) {
            this.inCombat = true
        }
    }

    override fun execute() {
        if (WorldHopper.hopToRandomWorld()) {
            finished()
            Execution.delay(4000, 5000)
        }

        if (!hopped) {
            log.error("unable to hop worlds")
        }

        Players.getLocal()?.let { player ->
            player.healthGauge?.let { healthGauge ->
                if (healthGauge.isValid) {
                    log.error("Hopper got into combat")
                    Execution.delayUntil({ player.healthGauge == null || inCombat }, 3000, 4000)
                    if (inCombat) {
                        finished()
                    } else {
                        //Empty else to satisfy compiler??????
                    }
                } else {
                    // di.sendMovement(player.serverPosition)
                    Mouse.move(player.serverPosition)
                    Execution.delay(600, 1200)
                }
            }
        }
    }
    private fun finished() {
        publish(ConditionalStateCompletedEvent("HoppingState finished has been called.", BotState.HopState))
    }

    companion object {
        private val log = getLogger(HoppingState::class.java)
    }
}