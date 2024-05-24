package com.runemate.zombiepirates.state.task.advance

import com.runemate.common.items
import com.runemate.common.state.Task
import com.runemate.common.traverse.BurningAmuletTraverse
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.game.api.script.Execution

class BurningAmuletTeleport : Task {
    override fun validate(): Boolean {
        return !Wilderness.isInWilderness() && util.isItemInInventoryOrEquipment(items.burningAmulet)
    }

    override fun execute() {
        println("Teleporting via Chaos Amulet")
        if (BurningAmuletTraverse.traverse(BurningAmuletTraverse.Destination.ChaosTemple)) {
            Execution.delayUntil({ !Wilderness.isInWilderness() }, 1000)
        }
    }
}