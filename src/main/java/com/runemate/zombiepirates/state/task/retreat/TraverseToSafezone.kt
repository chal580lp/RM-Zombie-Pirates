package com.runemate.zombiepirates.state.task.retreat

import com.runemate.common.LoggerUtils.getLogger

import com.runemate.common.inCombatArea
import com.runemate.common.framework.core.Task
import com.runemate.common.framework.core.injected
import com.runemate.common.traverse.DuelingRingTraverse
import com.runemate.common.traverse.Traverse
import com.runemate.common.util
import com.runemate.game.api.hybrid.RuneScape
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.game.api.hybrid.location.Coordinate
import com.runemate.game.api.hybrid.location.navigation.cognizant.ScenePath
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.hybrid.web.WebPath
import com.runemate.game.api.script.Execution
import com.runemate.game.api.script.framework.listeners.ChatboxListener
import com.runemate.game.api.script.framework.listeners.events.MessageEvent
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.state.task.ManagePrayer

class TraverseToSafezone : Task, ChatboxListener {
    private val pathCache: HashMap<String, WebPath> = HashMap()
    private val log = getLogger("TraverseToSafezone")
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
    private val managePrayer = ManagePrayer()
    private var tryLogout = true

    override fun validate(): Boolean {
        return Wilderness.isInWilderness()
    }

    override fun execute() {
        DefaultUI.setStatus("Traversing out of Wilderness")
        if (managePrayer.validate()) managePrayer.execute()
//        if (tryLogout && Players.getLocal()?.healthGauge == null && Players.getLocal()?.hitsplats?.isEmpty() == true) {
//            RuneScape.logout()
//        }
        kotlin.runCatching {
            for (coord in path2safe) {
                if (!Wilderness.isInWilderness()) return
                if (coord.y + 4 >= (Players.getLocal()?.serverPosition?.y ?: return)) continue
                val path = ScenePath.buildTo(coord)
                if (path != null) {
                    path.step()
                    Execution.delay(150)
                    return
                }
            }
        }.onFailure { log.debug("Failed to traverse path2safe", it.stackTrace)}

        kotlin.runCatching {
            if (bot.getCombatManager().inCombatArea) {
                val path: ScenePath? = ScenePath.buildTo(belowBridge)
                if (path != null) {
                    path.step()
                }
            } else {
                if (path == null) path = Traverse.getPathDestination(safeSpot, false)
                if (Wilderness.isInWilderness()) {
                    path?.step()
                }
            }
        }.onFailure { log.debug("Failed to traverse to belowBridge or Web path", it.stackTrace) }

    }

    override fun onMessageReceived(p0: MessageEvent?) {
        if (p0?.message?.contains("You can't log out until 10 seconds after combat.") == true) {
            tryLogout = false
        }
    }
}