package com.runemate.zombiepirates.state.task.retreat

import com.runemate.common.DI
import com.runemate.common.util
import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.game.api.hybrid.entities.GameObject
import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.location.Coordinate
import com.runemate.game.api.hybrid.location.navigation.cognizant.ScenePath
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.script.Execution
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot

class PoolOfRefreshment : Task {

    private var porTile = Coordinate(3130,3636, 0)
    private var poolOfRefreshment: GameObject? = null
    val bot : Bot by injected()

    override fun validate(): Boolean {
        poolOfRefreshment = util.findNearestGameObject("Pool of Refreshment") ?: return false

        return !util.healthIsFull() || !util.prayerIsFull()
    }

    override fun execute() {
        DefaultUI.setStatus("Using Pool of Refreshment")
        poolOfRefreshment?.let { gameObject ->
            if (gameObject.isVisible) {
                if (DI.send(MenuAction.forGameObject(gameObject, "Drink"))) {
                    Execution.delayUntil({ Players.getLocal()?.isMoving }, 600)
                    Execution.delayUntil({ util.healthIsFull() }, 4000)
                    return
                }
            }
        }
        val path = ScenePath.buildTo(porTile)
        path?.run { step() } ?: println("You are not in Ferox or ScenePath isn't working")
    }
}