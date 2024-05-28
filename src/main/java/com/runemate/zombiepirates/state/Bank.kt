package com.runemate.zombiepirates.state

import com.runemate.common.RMLogger
import com.runemate.common.state.GenericTransition
import com.runemate.common.state.TaskState
import com.runemate.common.state.di.injected
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.zombiepirates.state.task.bank.BankLoot
import com.runemate.zombiepirates.state.task.bank.OpenBank
import com.runemate.zombiepirates.state.task.bank.WithdrawSupplies
import com.runemate.zombiepirates.state.task.retreat.PoolOfRefreshment

class Bank : TaskState() {

    private val log: RMLogger = RMLogger.getLogger(this::class.java)

    val bot: Bot by injected()

    override fun defineTransitions() {
        addTransition(
            GenericTransition(
                { !bot.needToBank() },
                { Starting() }
            )
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
        //log.debug("TaskState: Bank")
    }
}
