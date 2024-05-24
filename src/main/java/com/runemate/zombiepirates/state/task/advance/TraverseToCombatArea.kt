package com.runemate.zombiepirates.state.task.advance

import com.runemate.common.combat.inCombatArea
import com.runemate.common.items
import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.common.traverse.Traverse
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.game.api.hybrid.location.Coordinate
import com.runemate.game.api.hybrid.location.navigation.cognizant.ScenePath
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.hybrid.web.WebPath
import com.runemate.game.api.script.Execution
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.getCombatManager


class TraverseToCombatArea : Task {
    private val pathCache: HashMap<String, WebPath> = HashMap()
    private var path: WebPath? = null
    private var scenePath : ScenePath? = null
    private val coordinate = Coordinate(3236,3627,0)

    val bot : Bot by injected()

    override fun validate(): Boolean {
        return !bot.getCombatManager().inCombatArea()
    }

    override fun execute() {
        if (scenePath == null) {
            scenePath = ScenePath.buildTo(coordinate)
        }

        scenePath?.let {
            it.step()
            return // Exit early if scenePath is used
        }

        if (path == null) {
            path = Traverse.getPathDestination(coordinate, false, pathCache)
        }

        path?.step()
    }

}