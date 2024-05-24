package com.runemate.zombiepirates.state.task.retreat

import com.runemate.common.combat.inCombatArea
import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.common.traverse.Traverse
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.game.api.hybrid.location.Coordinate
import com.runemate.game.api.hybrid.location.navigation.cognizant.ScenePath
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.hybrid.web.WebPath
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.getCombatManager

class TraverseToSafezone : Task {
    private val pathCache: HashMap<String, WebPath> = HashMap()
    private var path: WebPath? = null
    private val safeSpot = Coordinate(3245,3500,0)
    private val belowBridge = Coordinate(3238, 3587, 0)
    val bot : Bot by injected()
    val path2safe =  arrayOf(
         Coordinate(3236, 3622, 0),
         Coordinate(3229, 3609, 0),
         Coordinate(3238, 3599, 0),
         Coordinate(3238, 3591, 0),
         Coordinate(3240, 3581, 0),
         Coordinate(3242, 3571, 0),
         Coordinate(3242, 3562, 0),
         Coordinate(3239, 3552, 0),
         Coordinate(3239, 3543, 0),
         Coordinate(3240, 3534, 0),
         Coordinate(3240, 3523, 0)
    )

    override fun validate(): Boolean {
        return Wilderness.isInWilderness()
    }

    override fun execute() {
        DefaultUI.setStatus("Traversing to outside Wilderness")
        if (!isTeleblocked()) {
            //ferox tp
            return
        }
        for (coord in path2safe) {
            if (coord.y >= (Players.getLocal()?.serverPosition?.y ?: return)) continue
            val path = ScenePath.buildTo(coord)
            if (path != null) {
                path.step()
                return
            }
        }
        if (bot.getCombatManager().inCombatArea()) {
            val path: ScenePath? = ScenePath.buildTo(belowBridge)
            if (path != null) {
                path.step()
            }
        } else {
            if (path == null) path = Traverse.getPathDestination(safeSpot, false, pathCache)
            if (Wilderness.isInWilderness()) {
                println("Stepping")
                path?.step()
                println("Stepped")
            }
        }

    }

    private fun isTeleblocked(): Boolean {
        return true
    }
}