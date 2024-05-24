package com.runemate.common.state

import com.runemate.zombiepirates.state.task.ConsumeFood

class GlobalTaskManager {
    private val globalTasks: MutableList<Task> = mutableListOf()

    fun addGlobalTask(task: Task) {
        globalTasks.add(task)
    }

    fun executeGlobalTasks(): Boolean {
        for (task in globalTasks) {
            if (task.validate()) {
                task.execute()
                return true // Indicate that a global task was executed
            }
        }
        return false // No global task was executed
    }
}