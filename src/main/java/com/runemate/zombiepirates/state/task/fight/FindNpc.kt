package com.runemate.zombiepirates.state.task.fight

import com.runemate.common.combat.isTargetDying
import com.runemate.common.combat.isTargetNotValid
import com.runemate.common.combat.isTargetNull
import com.runemate.common.combat.isTargetNullOrNotValid
import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.getCombatManager

class FindNpc : Task {

    val bot : Bot by injected()

    override fun validate(): Boolean {
        if (bot.getCombatManager().isTargetNullOrNotValid()) {
            return true
        }
        if (bot.getCombatManager().isTargetDying()) {
            return true
        }
        return false
    }

    override fun execute() {
        bot.getCombatManager().setNewTargetNpc()
    }
}