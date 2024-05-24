package com.runemate.zombiepirates.state.task

import com.runemate.common.DI
import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.osrs.local.hud.interfaces.Prayer
import com.runemate.game.api.script.Execution
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.BotConfig

class ConsumePrayer : Task {

    val bot : Bot by injected()

    override fun validate(): Boolean {
        return Prayer.getMaximumPoints() - Prayer.getPoints() >= 20 && Inventory.contains(BotConfig.restorePotion)
    }

    override fun execute() {
        val points = Prayer.getPoints()
        println("Drinking ${BotConfig.restorePotion}")
        Inventory.newQuery()
            .names(BotConfig.restorePotion)
            .actions("Drink")
            .results()
            .first()?.let { DI.send(MenuAction.forSpriteItem(it,"Drink")) }
        Execution.delayUntil({Prayer.getPoints() > points} , 600)
    }
}