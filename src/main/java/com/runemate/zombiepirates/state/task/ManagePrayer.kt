package com.runemate.zombiepirates.state.task

import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.game.api.hybrid.local.Skill
import com.runemate.game.api.osrs.local.hud.interfaces.Prayer
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot

class ManagePrayer : Task {

    val bot : Bot by injected()

    override fun validate(): Boolean {
        return !Prayer.PROTECT_FROM_MAGIC.isActivated || !Prayer.PROTECT_ITEM.isActivated
    }

    override fun execute() {
        DefaultUI.setStatus("Managing prayers")
        if (Skill.PRAYER.currentLevel < 30) {
            bot.pause("You don't have high enough Prayer level to use PROTECT FROM MAGIC")
            return
        }
        Prayer.PROTECT_FROM_MAGIC.activate()
        Prayer.PROTECT_ITEM.activate()
    }
}