package com.runemate.zombiepirates.state.task.combat

import com.runemate.common.framework.core.Task
import com.runemate.common.framework.core.injected
import com.runemate.game.api.hybrid.region.Players
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.common.PotionHandler
import com.runemate.common.isAlreadyFightingEachOther
import com.runemate.common.isTargetHealthGreaterThanZero
import com.runemate.common.playerHasTarget
import com.runemate.game.api.script.Execution

class AttackNpc : Task {

    val bot : Bot by injected()

    override fun validate(): Boolean {
        if (bot.getCombatManager().isTargetHealthGreaterThanZero()
            && (bot.getCombatManager().isAlreadyFightingEachOther()
                    || (bot.getCombatManager().playerHasTarget() && Players.getLocal()?.target == bot.getCombatManager().targetNpc))) {
            return false
        }
        return true
    }

    override fun execute() {
        DefaultUI.setStatus("Fighting Zombie")
        PotionHandler.boostIfNeeded(bot.settings().boost)
        bot.getCombatManager().attackTargetNpcDI()
        Execution.delay(250)
    }
}