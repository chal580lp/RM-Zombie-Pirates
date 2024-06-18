package com.runemate.common.framework.core.addons

import com.runemate.common.LoggerUtils.getLogger

import com.runemate.game.api.hybrid.Environment
import kotlin.system.measureTimeMillis

data class RetryFailure(val methodName: String, val stage: Int, val exception: Exception?)
data class RetryMetrics(val methodName: String, val stage: Int, val retries: Int, val timeTaken: Long)

class RetryAnalytics {
    private val log = getLogger("RetryAnalytics")
    val failures = mutableListOf<RetryFailure>()
    val metrics = mutableListOf<RetryMetrics>()
    var totalRetries = 0
    var totalTimeTaken = 0L

    fun recordFailure(methodName: String, stage: Int, exception: Exception) {
        failures.add(RetryFailure(methodName, stage, exception))
    }

    fun recordRetry(timeTaken: Long) {
        totalRetries++
        totalTimeTaken += timeTaken
    }

    fun recordMetrics(methodName: String, stage: Int, retries: Int, timeTaken: Long) {
        metrics.add(RetryMetrics(methodName, stage, retries, timeTaken))
    }

    fun printAnalytics() {
        if (totalRetries > 0 || failures.isNotEmpty()) {
            log.info( "Total retries: $totalRetries" )
            log.info("Total failures: ${failures.size}")
            log.info("Total time taken: $totalTimeTaken ms" )
            metrics.groupBy { it.methodName }.forEach { (methodName, metrics) ->
                log.info("Method: $methodName, Average time taken: ${metrics.map { it.timeTaken }.average()} ms")
            }
            failures.groupBy { it.methodName }.forEach { (methodName, failures) ->
                log.info("Method: $methodName, Failures: ${failures.size}")
            }
        }
    }
}

class StageExecutor<R>() {
    private val defaultExitConditions: List<() -> Boolean> = listOf { Environment.getBot()?.isPaused ?: false }
    private val log = getLogger("StageExecutor")
    private val retryAnalytics = RetryAnalytics()
    private var maxRetries = 3
    private var currentStage = 0
    private var currentRetries = 0
    private var skipToStage: Int? = null
    private var additionalExitConditions: List<() -> Boolean> = emptyList()

    fun <T : R> executeStage(stage: Int, action: () -> T?): T? {
        currentStage = stage
        currentRetries = 0

        // Check for exit conditions
        if (defaultExitConditions.any { it() } || additionalExitConditions.any { it() }) {
            log.warn("Exit condition met. Exiting stage execution.")
            return null
        }

        // Check for stage skip
        if (skipToStage != null && skipToStage!! > stage) {
            log.info("Skipping to stage $skipToStage from stage $stage")
            return null
        }

        var result: T? = null
        val timeTaken = measureTimeMillis {
            result = withRetry(maxRetries, action)
        }
        retryAnalytics.recordMetrics(getCallerMethodName(), stage, currentRetries, timeTaken)
        return result
    }

    fun setAdditionalExitConditions(conditions: List<() -> Boolean>) {
        additionalExitConditions = conditions
    }

    private fun <T : R> withRetry(maxRetries: Int, action: () -> T?): T? {
        repeat(maxRetries) {
            try {
                val result = action()
                if (result != null) {
                    return result
                }
            } catch (e: Exception) {
                currentRetries++
                retryAnalytics.recordRetry(0)
                log.info("Retrying stage $currentStage, attempt: $currentRetries")
                onStageFailure(currentStage, currentRetries, e)
            }
        }
        onStageFailure(currentStage, currentRetries, IllegalStateException("Max retries exceeded"))
        return null
    }

    private fun onStageFailure(stage: Int, retries: Int, exception: Exception) {
        log.warn("Stage $stage failed, retry attempt: $retries, exception: $exception")
        retryAnalytics.recordFailure(getCallerMethodName(), stage, exception)
    }

    fun reset() {
        currentStage = 0
        currentRetries = 0
        skipToStage = null
        additionalExitConditions = emptyList()
    }

    fun printAnalytics() {
        retryAnalytics.printAnalytics()
    }

    fun skipTo(stage: Int) {
        skipToStage = stage
    }

    private fun getCallerMethodName(): String {
        return Thread.currentThread().stackTrace.first { it.className != this::class.java.name }.methodName
    }
}

inline fun <T, R : T> T.withStageExecutor(
    additionalExitConditions: List<() -> Boolean> = emptyList(),
    action: StageExecutor<R>.() -> R
): R {
    val stageExecutor = StageExecutor<R>()
    stageExecutor.setAdditionalExitConditions(additionalExitConditions)
    val result = stageExecutor.action()
    stageExecutor.reset()
    stageExecutor.printAnalytics()
    return result
}
