package com.runemate.zombiepirates.state.task.retreat

import com.runemate.common.item.items
import com.runemate.common.framework.core.Task
import com.runemate.common.framework.core.injected
import com.runemate.common.traverse.DuelingRingTraverse
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.game.api.script.Execution
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot

class TeleportOut: Task {

    val bot : Bot by injected()

    override fun validate(): Boolean {
        return Wilderness.isInWilderness()
                && Wilderness.getDepth() < 20
                && !util.isTeleBlocked()
                && util.isItemInInventoryOrEquipment(items.ringOfDueling)
    }

    override fun execute() {
        DefaultUI.setStatus("Teleporting to Ferox")
        if (DuelingRingTraverse.traverse(DuelingRingTraverse.Destination.Ferox)) {
            // Wait until we are out of the wilderness
            Execution.delayUntil({ !Wilderness.isInWilderness() }, 500)
        }
    }
}