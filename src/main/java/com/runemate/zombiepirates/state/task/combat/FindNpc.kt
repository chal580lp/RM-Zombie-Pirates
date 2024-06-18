package com.runemate.zombiepirates.state.task.combat

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.isTargetDying
import com.runemate.common.isTargetNullOrNotValid
import com.runemate.common.framework.core.Task
import com.runemate.common.framework.core.injected
import com.runemate.common.isTargetHealthGreaterThanZero
import com.runemate.zombiepirates.Bot

class FindNpc : Task {
    private val log = getLogger("FindNpc")

    val bot : Bot by injected()

    override fun validate(): Boolean {
        if (bot.getCombatManager().isTargetNullOrNotValid()) {
            return true
        }
        if (bot.getCombatManager().isTargetDying()) {
            log.debug("TRUE: Target is dying")
            return true
        }
        return false
    }

    override fun execute() {
        bot.getCombatManager().setNewTargetNpc()
    }
}