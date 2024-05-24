package com.runemate.zombiepirates.state.task.fight

import com.runemate.common.combat.isTargetNotValid
import com.runemate.common.combat.isTargetNull
import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.getCombatManager

class FindNpc : Task {

    val bot : Bot by injected()

    override fun validate(): Boolean {
        if (bot.getCombatManager().isTargetNull() == true) {
            return true
        }
        if (bot.getCombatManager().isTargetNotValid() == true) {
            return true
        }
        if (bot.getCombatManager()?.targetNpc?.healthGauge?.percent == 0) {
            return true
        }
        return false
    }

    override fun execute() {
        bot.getCombatManager().setNewTargetNpc()
    }
}