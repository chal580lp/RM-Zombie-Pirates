package com.runemate.zombiepirates.state.task.fight

import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.getLootManager

class Loot : Task {
    val bot : Bot by injected()
    override fun validate(): Boolean {
        return bot.getLootManager().shouldLoot ?: false
    }

    override fun execute() {
        bot.getLootManager().manageLooting()
    }
}