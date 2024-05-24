package com.runemate.zombiepirates.state

import com.runemate.common.RMLogger
import com.runemate.common.state.GenericTransition
import com.runemate.common.state.TaskState
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.state.task.retreat.TraverseToSafezone

class AntiPk : TaskState() {

    private val log: RMLogger = RMLogger.getLogger(this::class.java)

    init {
        checkGlobalTasks = true
    }

    override fun defineTransitions() {
        addTransition(
            GenericTransition({
                !Wilderness.isInWilderness()
            },
                { Starting() })
        )
    }

    override fun defineTasks() {

        addTask(TraverseToSafezone())

    }

    override fun onStart() {
        DefaultUI.setStatus("AntiPk Handler")
        log.debug("TaskState: AntiPk")
    }
}