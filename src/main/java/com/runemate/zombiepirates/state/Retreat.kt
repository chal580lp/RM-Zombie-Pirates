package com.runemate.zombiepirates.state

import com.runemate.common.RMLogger
import com.runemate.common.state.GenericTransition
import com.runemate.common.state.GlobalTaskManager
import com.runemate.common.state.TaskState
import com.runemate.common.state.di.injected
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.state.task.retreat.PoolOfRefreshment
import com.runemate.zombiepirates.state.task.retreat.TeleportOut
import com.runemate.zombiepirates.state.task.retreat.TraverseToBank
import com.runemate.zombiepirates.state.task.retreat.TraverseToSafezone

class Retreat : TaskState() {
    private val log: RMLogger = RMLogger.getLogger(this::class.java)
    override val globalTaskManager: GlobalTaskManager by injected()

    init {
        checkGlobalTasks = true
    }

    override fun defineTransitions() {
        addTransition(
            GenericTransition(
                { !Wilderness.isInWilderness() && util.isGameObjectVisible(listOf("Bank booth", "Bank chest")) },
                { Bank() }
            )
        )
    }

    override fun defineTasks() {
        addTask(TeleportOut())
        addTask(TraverseToSafezone())
        addTask(TraverseToBank())
    }

    override fun onStart() {
        DefaultUI.setStatus("Traversing to Bank")
        //log.debug("TaskState: Retreat")
    }
}