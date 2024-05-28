package com.runemate.zombiepirates.state.task.retreat

import com.runemate.common.RMLogger
import com.runemate.common.items
import com.runemate.common.state.Task
import com.runemate.common.traverse.DuelingRingTraverse
import com.runemate.common.traverse.Traverse
import com.runemate.common.util
import com.runemate.game.api.hybrid.entities.details.Locatable
import com.runemate.game.api.hybrid.local.Camera
import com.runemate.game.api.hybrid.local.hud.interfaces.*
import com.runemate.game.api.hybrid.location.Coordinate
import com.runemate.game.api.hybrid.location.navigation.cognizant.ScenePath
import com.runemate.game.api.hybrid.region.GameObjects
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.hybrid.web.WebPath
import com.runemate.game.api.script.Execution
import com.runemate.ui.DefaultUI

class TraverseToBank : Task {

    private val log: RMLogger = RMLogger.getLogger(this::class.java)
    private var path: WebPath? = null
    private val LUMBRIDGE_CASTLE = Coordinate(3215, 3219, 0)

    override fun validate(): Boolean {
        return !Bank.isOpen() && !GameObjects.newQuery()
            .names("Bank booth", "Bank chest")
            .visible()
            .results()
            .any()
    }

    override fun execute() {

        val bankObjects = GameObjects.newQuery().names("Bank booth", "Bank chest").results()
        val bankCoords = bankObjects
            .filterIsInstance<Locatable>()
            .mapNotNull { it.area?.surroundingCoordinates }
            .flatten()

        for (coord in bankCoords) {
            val path = ScenePath.buildTo(coord)
            if (path != null) {
                path.step()
                return
            }
        }

        if (util.isItemInInventoryOrEquipment(items.ringOfDueling)) {
            DuelingRingTraverse.traverse(DuelingRingTraverse.Destination.Ferox)
            return
        }
        log.debug("No ring of dueling found, we must have died. Walking to Lumbridge bank.")
        traverseToLumbridgeBank()

    }

    private fun traverseToLumbridgeBank() {
        when (Players.getLocal()?.serverPosition?.plane) {
            0 -> {
                if (LUMBRIDGE_CASTLE.distanceTo(Players.getLocal()) > 20) {
                    if (path == null) path = Traverse.getPathDestination(LUMBRIDGE_CASTLE, false)
                    path?.step()
                    return
                }
                val stairs = GameObjects.newQuery().names("Staircase").results().first() ?: run {
                    log.debug("Stairs not found")
                    return
                }
                if (!stairs.isVisible) {
                    Camera.concurrentlyTurnTo(stairs)
                    Execution.delayUntil({ stairs.isVisible }, 2500)
                }
                stairs.interact("Climb-up")
                log.debug("Climbing up stairs")
                Execution.delayUntil({ Players.getLocal()?.serverPosition?.height == 1},5000)
                //val path: ScenePath? = ScenePath.buildTo()
                //path?.step()
            }
            1 -> {
                val stairs = GameObjects.newQuery().names("Staircase").results().first() ?: return
                if (stairs.isVisible) {
                    stairs.interact("Climb-up")
                    Execution.delayUntil({ Players.getLocal()?.serverPosition?.height == 2},2500)
                }

            }
            2 -> {
                println("Should beable to find bank")
            }
        }
    }

}