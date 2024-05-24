package com.runemate.zombiepirates.state.task.fight

import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.game.api.hybrid.entities.Player
import com.runemate.game.api.hybrid.region.Players
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.getCombatManager

class AttackNpc : Task {

    val bot : Bot by injected()

    private val player: Player? = Players.getLocal()

    override fun validate(): Boolean {
        val target = bot.getCombatManager().targetNpc
        if ((target?.healthGauge != null && target.healthGauge?.percent!! > 0) && Players.getLocal()?.target != null) {
            return false
        }

        return true
    }

    override fun execute() {
        DefaultUI.setStatus("Fighting Zombie")
        bot.getCombatManager().attackTargetNpcDI()
    }
}