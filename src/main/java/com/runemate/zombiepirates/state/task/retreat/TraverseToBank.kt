package com.runemate.zombiepirates.state.task.retreat

import com.runemate.common.LoggerUtils.getLogger

import com.runemate.common.item.items
import com.runemate.common.framework.core.Task
import com.runemate.common.traverse.DuelingRingTraverse
import com.runemate.common.traverse.Traverse
import com.runemate.common.util
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

    private val log = getLogger("TraverseToBank")
    private var path: WebPath? = null
    private val LUMBRIDGE_CASTLE = Coordinate(3215, 3219, 0)
    private val LUMBRIDGE_WEST_STAIRS = Coordinate(3206, 3209, 0)

    override fun validate(): Boolean {
        return !Bank.isOpen() && !util.isBankVisibleReachable()
    }

    override fun execute() {
        DefaultUI.setStatus("Traversing to a bank")
        val bankCoords = util.findGameObjects(listOf("Bank booth", "Bank chest"))
            ?.flatMap { it.area?.surroundingCoordinates ?: emptyList() }

        bankCoords?.forEach { coord ->
            val path = ScenePath.buildTo(coord)
            if (path != null) {
                log.debug("Found Bank, walking to it.")
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
        val localPlayer = Players.getLocal() ?: return

        when (localPlayer.serverPosition.plane) {
            0 -> {
                if (LUMBRIDGE_WEST_STAIRS.distanceTo(localPlayer) > 10) {
                    val scenePath = ScenePath.buildTo(LUMBRIDGE_WEST_STAIRS)
                    if (scenePath != null) {
                        log.debug("Walking to Lumbridge west stairs via ScenePath")
                        scenePath.step()
                        return
                    }
                    if (path == null) path = Traverse.getPathDestination(LUMBRIDGE_WEST_STAIRS, false)
                    log.debug("Walking to Lumbridge west stairs via WebPath")
                    path?.step()
                    return
                }
                val stairs = GameObjects.newQuery().names("Staircase").results().nearest()
                if (stairs?.isVisible == true) {
                    Camera.concurrentlyTurnTo(stairs)
                    Execution.delayUntil({ stairs.isVisible }, 2500)
                    stairs.interact("Climb-up")
                    log.debug("Climbing up stairs")
                    Execution.delayUntil({ Players.getLocal()?.serverPosition?.plane == 1 }, 5000)
                } else {
                    val path: ScenePath? = ScenePath.buildTo(LUMBRIDGE_WEST_STAIRS)
                    path?.step()
                }
            }
            1 -> {
                val stairs = GameObjects.newQuery().names("Staircase").results().nearest() ?: return
                if (stairs.isVisible) {
                    stairs.interact("Climb-up")
                    log.debug("Climbing to the next floor")
                    Execution.delayUntil({ Players.getLocal()?.serverPosition?.plane == 2 }, 2500)
                }
            }
            2 -> {
                log.debug("Should be able to find bank")
            }
        }
    }

}