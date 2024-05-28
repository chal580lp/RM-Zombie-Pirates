package com.runemate.common.state

import com.runemate.common.RMLogger


class GlobalTaskManager {
    val globalTasks: MutableList<Task> = mutableListOf()
    private val log: RMLogger = RMLogger.getLogger(this::class.java)

    fun addGlobalTask(task: Task) {
        globalTasks.add(task)
        log.debug("Added global task: ${task::class.simpleName}. Current size: ${globalTasks.size}")
    }

    fun executeGlobalTasks(): Boolean {
        //log.debug("Executing global tasks. Current size: ${globalTasks.size}")
        for (task in globalTasks) {
            if (task.validate()) {
                log.debug("Executing global task: ${task::class.simpleName}")
                task.execute()
                return true // Indicate that a global task was executed
            }
        }
        return false // No global task was executed
    }
}