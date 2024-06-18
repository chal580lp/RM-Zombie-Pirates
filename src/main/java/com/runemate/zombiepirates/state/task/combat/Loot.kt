package com.runemate.zombiepirates.state.task.combat

import com.runemate.common.LoggerUtils.getLogger

import com.runemate.common.framework.core.Task
import com.runemate.common.framework.core.injected
import com.runemate.zombiepirates.Bot

class Loot : Task {
    val bot : Bot by injected()
    private val log = getLogger("Loot")

    override fun validate(): Boolean {
        return bot.getLootManager().shouldLoot
    }

    override fun execute() {
        bot.getLootManager().manageLooting()
    }
}