package com.runemate.common.state

import com.runemate.common.RMLogger
import com.runemate.common.state.di.injected

abstract class TaskState : State() {

    private val log: RMLogger = RMLogger.getLogger(this::class.java)
    private var tasks: List<Task> = mutableListOf()
    open val globalTaskManager: GlobalTaskManager by injected()
    var checkGlobalTasks: Boolean = false

    open fun defineTasks() {}

    fun addTask(task: Task): Task {
        tasks += task
        return task
    }

    override fun execute() {
        //log.debug("Executing TaskState: ${this::class.simpleName}")
        if (checkGlobalTasks && globalTaskManager.executeGlobalTasks()) {
            //log.debug("Executed a global task")
            return
        }

        for (task in tasks) {
            if (task.validate()) {
                //log.debug("Executing task: ${task::class.simpleName}")
                task.execute()
                return
            }
        }
        //log.debug("No valid task found to execute")
    }
}