package com.runemate.common.framework.core

import com.runemate.common.LoggerUtils.getLogger
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
abstract class TaskState : BaseState() {
    private val tasks = mutableListOf<Task>()
    private val log = getLogger("TaskState")
    private var lastExecutionTimestamp = TimeSource.Monotonic.markNow()

    open fun defineTasks() {}

    fun addTask(task: Task) = tasks.add(task)

    override fun execute() {
        val timeSinceLastExecution = lastExecutionTimestamp.elapsedNow()
        val task = tasks.find { it.validate() }

        task?.let {
            val (_, executionTime) = measureTimedValue {
                it.execute()
            }

            log.debug(
                "(${timeSinceLastExecution.toSecondsString(2)}s) Executing task (${executionTime.toSecondsString(2)}s): ${it.javaClass.simpleName}"
            )
            val currentTimestamp = TimeSource.Monotonic.markNow()
            lastExecutionTimestamp = currentTimestamp
        }
    }
}

fun Duration.toSecondsString(digits: Int): String {
    val seconds = this.inWholeMilliseconds / 1000.0
    return "%.${digits}f".format(seconds)
}