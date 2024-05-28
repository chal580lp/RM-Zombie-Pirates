package com.runemate.zombiepirates.state.task.bank

import com.runemate.common.state.Task
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank
import com.runemate.game.api.script.Execution
import com.runemate.ui.DefaultUI

class OpenBank : Task {
    override fun validate(): Boolean {
        return !Bank.isOpen()
    }

    override fun execute() {
        Bank.open()
        //Wait until bank is open or timeout.
        Execution.delayUntil({Bank.isOpen() }, 2500)
    }
}