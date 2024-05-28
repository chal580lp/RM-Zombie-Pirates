package com.runemate.zombiepirates.state.task.retreat

import com.runemate.common.items
import com.runemate.common.state.Task
import com.runemate.common.state.di.injected
import com.runemate.common.traverse.DuelingRingTraverse
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.game.api.script.Execution
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.getAntiPkManager

class TeleportOut: Task {

    val bot : Bot by injected()

    override fun validate(): Boolean {
        return Wilderness.isInWilderness()
                && Wilderness.getDepth() < 20
                && !util.isTeleBlocked()
                && util.isItemInInventoryOrEquipment(items.ringOfDueling)
    }

    override fun execute() {
        if (DuelingRingTraverse.traverse(DuelingRingTraverse.Destination.Ferox)) {
            // Wait until we are out of the wilderness
            Execution.delayUntil({ !Wilderness.isInWilderness() }, 500)
        }
    }
}