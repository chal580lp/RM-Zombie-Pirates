package com.runemate.common.framework.task

import com.runemate.common.DeathHandler
import com.runemate.common.framework.core.Task
import com.runemate.ui.DefaultUI

class DeathHandle : Task {
    private val deathHandler = DeathHandler()

    override fun validate(): Boolean {
        return deathHandler.verify()
    }

    override fun execute() {
        DefaultUI.setStatus("Retrieving items from Death")
        deathHandler.execute()
    }
}