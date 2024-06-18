package com.runemate.common

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.TaskMachine
import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.game.api.hybrid.entities.Npc
import com.runemate.game.api.hybrid.local.Camera
import com.runemate.game.api.hybrid.location.Area
import com.runemate.game.api.hybrid.location.navigation.cognizant.ScenePath
import com.runemate.game.api.hybrid.queries.NpcQueryBuilder
import com.runemate.game.api.hybrid.region.Npcs
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.hybrid.util.Regex
import com.runemate.game.api.script.Execution

interface ICombatManager {
    val combatArea: Area
    val npcNames: List<String>
    val npcQuery: NpcQueryBuilder
    var attackOption: String
    var targetNpc: Npc?
    var killCount: Int

    fun attackTargetNpcDI(): Boolean

    fun setNewTargetNpc()

    fun resetTargetNpc()
}

fun ICombatManager.incKillCount() {
    killCount++
}

fun ICombatManager.isTargetNull() = targetNpc === null

fun ICombatManager.isTargetValid() = targetNpc?.isValid == true

fun ICombatManager.isTargetNotValid() = targetNpc?.isValid == false

fun ICombatManager.isTargetNotNull() = targetNpc !== null

fun ICombatManager.isTargetNullOrNotValid() = isTargetNull() || isTargetNotValid()

fun ICombatManager.isTargetDying() = targetNpc?.healthGauge?.percent == 0

val ICombatManager.inCombatArea : Boolean
    get() = combatArea.contains(Players.getLocal())

fun ICombatManager.inCombat(): Boolean {
    if (isTargetNull() || isTargetNotValid()) return false

    // Check if player is interacting with the target npc
    val isPlayerTargetingNpc = Players.getLocal()
        ?.target
        ?.let { interacting ->
            interacting == targetNpc
        } ?: false

    // Check if the target npc is interacting with the player
    val isNpcTargetingPlayer = targetNpc!!.target == Players.getLocal()

    return isPlayerTargetingNpc && isNpcTargetingPlayer
}

fun ICombatManager.getInteractingWithMe() = npcQuery
    .targeting(Players.getLocal())
    .filter { it.healthGauge?.isValid ?: false }
    .results()
    .toList()

//fun ICombatManager.getInteractingCharacter() = Players.getLocal().target

fun ICombatManager.getClosestNonInteracting() = npcQuery
    .results()
    .nearest()

fun ICombatManager.getClosestByScenePath(): Npc? {
    val path = ScenePath.buildTo(npcQuery.results()) ?: return null
    val nearest = Npcs.getLoadedOn(path.last?.position).first()
    return nearest
}
fun ICombatManager.isTargetHealthZero(): Boolean {
    return targetNpc?.healthGauge?.percent == 0
}

fun ICombatManager.isTargetNotVisible(): Boolean {
    return targetNpc?.isVisible == false
}

fun ICombatManager.isAlreadyFightingEachOther(): Boolean {
    return targetNpc?.target?.equals(Players.getLocal()) == true && Players.getLocal()?.target?.equals(targetNpc) == true
}

fun ICombatManager.isTargetHealthGreaterThanZero(): Boolean {
    return targetNpc?.healthGauge == null || (targetNpc?.healthGauge?.percent ?: 0) > 0
}

fun ICombatManager.playerHasTarget(): Boolean {
    return Players.getLocal()?.target != null
}

class CombatManager<TSettings : BotConfig>(private val bot: TaskMachine<TSettings>) : ICombatManager  {
    override val combatArea: Area
        get() = bot.settings().combatArea

    override val npcNames: List<String>
        get() = bot.settings().npcNames

    override var targetNpc: Npc? = null
    override var attackOption: String = "Attack"
    override var killCount: Int = 0
    private val log = getLogger("CombatManager")


    override val npcQuery: NpcQueryBuilder
        get() = Npcs.newQuery()
            .names(Regex.getPatternsForContainsStrings(npcNames.joinToString(".")))
            .filter { it.isValid }
            .filter { combatArea.contains(it) }
            .filter { it.healthGauge?.percent != 0 || it.healthGauge == null }
            .filter { it != targetNpc }
            .reachable()

    override fun resetTargetNpc() {
        log.debug("Resetting target npc")
        targetNpc = null
    }



    override fun setNewTargetNpc() {

        // Prioritize the npc interacting with our player
        //val interactingNpc = get

        //Prioritize the npcs that are directly interacting with the player
        val interactingWithMe = getInteractingWithMe()

        // Filter by npc ids or names to ensure targeting the correct npcs
        val filteredInteractingWithMe = interactingWithMe.filter {
            npcNames.contains(it.name)
            it.healthGauge?.percent != 0
        }

        //If there's a npc directly interacting with the player that matches our targets, prioritize it
//        if (filteredInteractingWithMe.isNotEmpty()) {
//            targetNpc = filteredInteractingWithMe.first()
//            return
//        }

        // If no directly interacting npcs match, find the closest npc that matches the criteria
        val closestMachingNpc = getClosestByScenePath() ?: getClosestNonInteracting()

        targetNpc = if (closestMachingNpc !== null) {
            closestMachingNpc
        } else {
            log.debug("Null Targets available")
            null
        }
        log.debug("Set new Target NPC: ${targetNpc?.name}")


    }

    override fun attackTargetNpcDI(): Boolean {

        if (isTargetNull() || isTargetNotValid() || isTargetHealthZero()) {
            log.debug("Returning false due to target being null, invalid, or having zero health.")
            return false
        }

        val animationBeforeAttacking = Players.getLocal()?.animationId

        if (isTargetNotVisible()) {
            log.debug("Target NPC not visible, turning camera.")
            Camera.turnTo(targetNpc)
            Execution.delayUntil({ targetNpc?.isVisible == true }, 1000)
            if (isTargetNotVisible()) {
                util.setCamera()
            }
        }

        if (isAlreadyFightingEachOther()) {
            log.debug("Returning false due to already fighting each other.")
            return false
        }

        if (targetNpc?.interact(attackOption) == false) {
            log.debug("Returning false due to interaction failure.")
            return false
        }
        log.debug("Attacking Target NPC: ${targetNpc?.name}")
        Execution.delayUntil({ inCombat() }, 600)
        return Execution.delayUntil({
            Players.getLocal()?.animationId != 1
            && Players.getLocal()?.animationId != animationBeforeAttacking
            && inCombat()
            && Players.getLocal()?.isMoving == false
        }, 1200)
    }
}

