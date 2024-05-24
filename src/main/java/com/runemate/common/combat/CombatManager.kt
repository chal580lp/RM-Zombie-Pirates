package com.runemate.common.combat

import com.runemate.common.DI
import com.runemate.game.api.hybrid.entities.Npc
import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.local.Camera
import com.runemate.game.api.hybrid.location.Area
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

fun ICombatManager.inCombatArea() = combatArea.contains(Players.getLocal())

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

class CombatManager(
    override val combatArea: Area,
    override val npcNames: List<String>
) : ICombatManager {
    override var targetNpc: Npc? = null
    override var attackOption: String = "Attack"
    override var killCount: Int = 0

    override val npcQuery
        get() = Npcs.newQuery()
            .names(Regex.getPatternsForContainsStrings(npcNames.joinToString(".")))
            .filter { it.isValid }
            .filter { combatArea.contains(it) }
            .filter { it.healthGauge?.percent != 0}
            .reachable()

    override fun resetTargetNpc() {
        println("Reset target npc")
        targetNpc = null
    }

    override fun setNewTargetNpc() {
        println("Setting new target npc")

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
        val closestMachingNpc = getClosestNonInteracting()

        targetNpc = if (closestMachingNpc !== null) {
            closestMachingNpc
        } else {
            println("Null Targets Avalable")
            null
        }


    }

    override fun attackTargetNpcDI(): Boolean {
        println("Attacking target npc")

        if (isTargetNull() || isTargetNotValid() || targetNpc?.healthGauge?.percent == 0) {
            return false
        }

        val animationBeforeAttacking = Players.getLocal()?.animationId

        if (targetNpc?.isVisible == false) {
            //Camera.turnTo(targetNpc)
            Camera.concurrentlyTurnTo(targetNpc)
            Execution.delayUntil({targetNpc?.isVisible == true }, 1000);
        }

        if (!DI.send(MenuAction.forNpc(targetNpc!!, attackOption))) return false


        //return //Execution.delayUntil({ inCombat() }, 1200)&& !targetNpc?.hitsplats.isNullOrEmpty()
        return Execution.delayUntil({
                     Players.getLocal()?.animationId != 1
                    && Players.getLocal()?.animationId != animationBeforeAttacking
                                        }, 1200)
    }
}

