package com.runemate.common.framework.task

import com.runemate.common.LoggerUtils.getLogger

import com.runemate.common.util
import com.runemate.common.framework.core.Task
import com.runemate.common.framework.core.injected
import com.runemate.game.api.hybrid.entities.GameObject
import com.runemate.game.api.hybrid.location.Coordinate
import com.runemate.game.api.hybrid.location.navigation.cognizant.ScenePath
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.script.Execution
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot

class PoolOfRefreshment : Task {
    companion object {
        private val POOL_OF_REFRESHMENT_TILE = Coordinate(3130, 3636, 0)
    }

    private var poolOfRefreshment: GameObject? = null
    private val log = getLogger("PoolOfRefreshment")
    val bot : Bot by injected()

    override fun validate(): Boolean {
        poolOfRefreshment = util.findNearestGameObject("Pool of Refreshment") ?: return false

        return !util.healthIsFull() || !util.prayerIsFull()
    }

    override fun execute() {
        DefaultUI.setStatus("Using Pool of Refreshment")
        poolOfRefreshment?.let { gameObject ->
            if (gameObject.isVisible) {
                if (gameObject.interact("Drink")) {
                    Execution.delayUntil({ Players.getLocal()?.isMoving }, 600)
                    Execution.delayUntil({ util.healthIsFull() && util.prayerIsFull() }, 4000)
                    Execution.delay(1200) // The exit animation on the fountain is weirdly long
                    return
                }
            }
        }
        val path = ScenePath.buildTo(POOL_OF_REFRESHMENT_TILE)
        path?.run { step() } ?: log.warn("You aren't in Ferox or ScenePath isn't working")
    }
}