package com.runemate.zombiepirates.state.task.retreat

import com.runemate.common.state.Task
import com.runemate.common.traverse.DuelingRingTraverse
import com.runemate.game.api.hybrid.entities.details.Locatable
import com.runemate.game.api.hybrid.local.hud.interfaces.*
import com.runemate.game.api.hybrid.location.navigation.cognizant.ScenePath
import com.runemate.game.api.hybrid.region.GameObjects
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.script.Execution
import com.runemate.ui.DefaultUI

class TraverseToBank : Task {

    override fun validate(): Boolean {
        return !Bank.isOpen() && !GameObjects.newQuery()
            .names("Bank booth", "Bank chest")
            .visible()
            .results()
            .any()
    }

    override fun execute() {
        DefaultUI.setStatus("Traversing to bank")

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

        if (DuelingRingTraverse.traverse(DuelingRingTraverse.Destination.Ferox)) return

        traverseToLumbridgeBank()
    }

    private fun traverseToLumbridgeBank() {
        when (Players.getLocal()?.serverPosition?.plane) {
            0 -> {
                val stairs = GameObjects.newQuery().names("Staircase").results().first() ?: return
                if (stairs.isVisible) {
                    stairs.interact("Climb-up")
                    Execution.delayUntil({ Players.getLocal()?.serverPosition?.height == 1},2500)
                }
                val path: ScenePath? = ScenePath.buildTo()
                path?.step()
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