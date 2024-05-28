package com.runemate.zombiepirates.state.task

import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.game.api.hybrid.local.Skill
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.game.api.hybrid.local.hud.interfaces.Interfaces
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.osrs.local.hud.interfaces.ControlPanelTab
import com.runemate.game.api.osrs.local.hud.interfaces.Prayer
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.BotConfig

class ManagePrayer : Task {

    val bot : Bot by injected()

    override fun validate(): Boolean {
        return Prayer.getPoints() != 0
                && (!Prayer.PROTECT_FROM_MAGIC.isActivated || !Prayer.PROTECT_ITEM.isActivated)
                && (Wilderness.isInWilderness() || BotConfig.FEROX_ENCLAVE.contains(Players.getLocal()))
    }

    override fun execute() {
        DefaultUI.setStatus("Managing prayers")
        if (Skill.PRAYER.baseLevel < 30) {
            bot.pause("You don't have high enough Prayer level to use PROTECT FROM MAGIC")
            return
        }
        Prayer.PROTECT_FROM_MAGIC.activate()
        Prayer.PROTECT_ITEM.activate()
        ControlPanelTab.INVENTORY.open()
    }
}