package com.runemate.common

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.TaskMachine
import com.runemate.common.framework.core.addons.BotState
import com.runemate.game.api.hybrid.entities.Player
import com.runemate.game.api.hybrid.entities.status.OverheadIcon
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.script.framework.listeners.ChatboxListener
import com.runemate.game.api.script.framework.listeners.EngineListener
import com.runemate.game.api.script.framework.listeners.PlayerListener
import com.runemate.game.api.script.framework.listeners.events.DeathEvent
import com.runemate.game.api.script.framework.listeners.events.EntityEvent
import com.runemate.game.api.script.framework.listeners.events.MessageEvent
import com.runemate.game.api.script.framework.listeners.events.TargetEvent

object AntiPkManager : EngineListener, ChatboxListener, PlayerListener {
    private lateinit var bot: TaskMachine<*>
    private val log = getLogger("AntiPkManager")
    private var deaths = 0
    private var attacked = 0
    var tickPenalty = 0
    var tickStartTime: Long = System.currentTimeMillis()


    fun initialize(bot: TaskMachine<*>) {
        this.bot = bot
    }

    fun getDeaths(): Int {
        return deaths
    }

    private fun underAttack(): Boolean {
        if (!Wilderness.isInWilderness()) return false
        val player = Players.getLocal() ?: return false
        val wildlvl = Wilderness.getDepth(player)
        val cmbtlvl = player.combatLevel
        val low = cmbtlvl - wildlvl
        val high = cmbtlvl + wildlvl
        val players = Players.newQuery()
            .targeting(player)
            .combatLevels(low, high)
            .results()

        return players.any { hasPKSkull(it) }
    }

    private fun hasPKSkull(player: Player): Boolean {
        return player.overheadIcons.any { it.skullType == OverheadIcon.SkullType.BASIC }
    }

    override fun onTickStart() {
        if (!::bot.isInitialized) {
            println("AntiPkManager is not initialized properly.")
            return
        }
        tickStartTime = System.currentTimeMillis()

        if (bot.isPaused) return
        if (!Wilderness.isInWilderness()) return

        val currentState = bot.getCurrentState()
        val playersAttackingMe = util.getPlayersAttackingMe().toTypedArray()

        if (currentState !is BotState.AntiPkState) {
            if (!underAttack() && Players.newQuery().targeting(Players.getLocal()).results().isNotEmpty()) {
                val targetingPlayer = Players.newQuery().targeting(Players.getLocal()).results().first() ?: return
                log.debug("We are being targeted by '{}', underAttack() returned false", targetingPlayer.name)
                log.debug("hasPKSkull: {}. Overhead: {}", hasPKSkull(targetingPlayer), targetingPlayer.overheadIcons)
            }

            if (underAttack()) {
                log.debug("Under attack by player/s {}", playersAttackingMe)
                log.debug("Deaths: {} Attacked {}", deaths, attacked)
                playersAttackingMe.firstOrNull()?.let { player ->
                    logPlayerInfo(player)
                    attacked++
                    bot.setCurrentState(BotState.AntiPkState)
                    bot.setConditionalState(BotState.HopState) { bot.getCurrentState() == BotState.BankState }
                } ?: log.error("AntiPkManager seems to think we aren't under attack?")
            }
        }

        if (currentState is BotState.AntiPkState) {
            log.debug("Still under attack by player/s {}", playersAttackingMe)
            playersAttackingMe.firstOrNull()?.let { player ->
                kotlin.runCatching {
                    logPlayerInfo(player)
                }.onFailure { log.error("Error getting player info: $it") }
            }
        }
    }

    private fun logPlayerInfo(player: Player) {
        log.debug("Combat Level {} Wilderness Level {}", player.combatLevel, Wilderness.getDepth())
        log.debug("{} {} {}", player.target, player.animationId, player.wornItems)
    }

    override fun onMessageReceived(p0: MessageEvent?) {

        if (bot.getCurrentState() is BotState.AntiPkState) return

        if (p0?.message?.contains("You are unable to move") == true) {
            attacked++
            log.debug("Setting Anti Pk State based on msg: You are unable to move")
            bot.setCurrentState(BotState.AntiPkState)
        }

        if (p0?.message?.contains("You have been frozen") == true) {
            attacked++
            log.debug("Setting Anti Pk State based on msg: You have been frozen")
            bot.setCurrentState(BotState.AntiPkState)
        }
    }

    override fun onPlayerDeath(event: DeathEvent?) {
        if (event != null) {
            if (event.source == Players.getLocal()) {
                deaths++
                log.debug("Deaths: {} Attacked {}", deaths, attacked)
            }
        }
    }

    override fun onPlayerTargetChanged(event: TargetEvent?) {
        if (event == null || event.target == null) return
        if (event.target == Players.getLocal() && event.entityType == EntityEvent.EntityType.PLAYER)  {
            log.debug("{} is now targeting us", event.source)
        }
        if (event.source != Players.getLocal() ) return
        if (event.entityType == EntityEvent.EntityType.PLAYER) {
            log.debug("We are now targeting {}", event.target)
        }
    }
}