package com.runemate.zombiepirates.state

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.TaskState
import com.runemate.common.framework.core.addons.BotState
import com.runemate.common.framework.core.injected
import com.runemate.common.framework.task.PoolOfRefreshment
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.state.task.bank.BankLoot
import com.runemate.zombiepirates.state.task.bank.OpenBank
import com.runemate.zombiepirates.state.task.bank.WithdrawSupplies

class BankState : TaskState() {

    private val log = getLogger("BankState")
    val bot: Bot by injected()

    override fun defineTransitions() {
        addGenericTransition(
            { !util.isBankReachable() && !Bank.isOpen() },
            { BotState.StartState }
        )
    }

    override fun defineTasks() {
        addTask(PoolOfRefreshment())
        addTask(OpenBank())
        addTask(BankLoot())
        addTask(WithdrawSupplies())
    }

    override fun onStart() {
        DefaultUI.setStatus("Banking")
    }
}
