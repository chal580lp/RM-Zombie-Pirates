package com.runemate.zombiepirates.state.task

import com.runemate.common.DI
import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.local.hud.interfaces.Health
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.script.Execution
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.BotConfig

class ConsumeFood : Task {

    val bot : Bot by injected()

    override fun validate(): Boolean {
        return Health.getMaximum() - Health.getCurrent() >= BotConfig.foodHeal
    }

    override fun execute() {
        println("Eating ${BotConfig.food}")
        Inventory.newQuery()
            .names(BotConfig.food)
            .actions("Eat")
            .results()
            .first()?.let { DI.send(MenuAction.forSpriteItem(it,"Eat"))
                Execution.delayWhile(it::isValid, 600, 1200)}

    }
}