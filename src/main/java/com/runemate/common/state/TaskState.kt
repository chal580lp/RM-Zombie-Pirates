package com.runemate.common.state

import com.runemate.common.state.di.injected
import java.util.ArrayList

abstract class TaskState : State() {

    private var tasks: List<Task> = ArrayList()
    val globalTaskManager: GlobalTaskManager by injected()
    var checkGlobalTasks: Boolean = false

    open fun defineTasks() {
    }

    fun addTask(task: Task): Task {
        tasks += task

        return task
    }

    override fun execute() {

        if (checkGlobalTasks && globalTaskManager.executeGlobalTasks()) {
            return
        }

        for (task in tasks) {
            if (task.validate()) {
                task.execute()
                return
            }
        }
    }
}