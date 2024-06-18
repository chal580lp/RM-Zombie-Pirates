package com.runemate.common.framework.task

import com.runemate.common.AntiPkManager
import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.framework.core.Task
import com.runemate.common.framework.core.TaskMachine
import com.runemate.common.item.*
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.hud.interfaces.Health
import com.runemate.game.api.osrs.local.hud.interfaces.Prayer
import com.runemate.game.api.script.framework.listeners.EngineListener
import kotlin.system.measureTimeMillis

class PKConsumeables<TSettings : BotConfig>(var bot: TaskMachine<TSettings>) : Task {

    private val log = getLogger("PKConsumeables")
    private val minHealth = Health.getMaximum() - (bot.settings().food.heal + 2)
    private val minPrayer = Prayer.getMaximumPoints() - (Restore.getRestoreAmount() + 2)

    private val consumeActions = listOf(
        ConsumeAction("canEat2Restore1", ::canEat2Restore1, listOf(Action("eatAnyExcludingKarambwan", ::eatAnyExcludingKarambwan), Action("consumeRestore", ::consumeRestore), Action("eatKarambwan", ::eatKarambwan))),
        ConsumeAction("canEat1Restore1", ::canEat1Restore1, listOf(Action("eatAny", ::eatAny), Action("consumeRestore", ::consumeRestore))),
        ConsumeAction("canRestore1", ::canRestore1, listOf(Action("consumeRestore", ::consumeRestore))),
        ConsumeAction("canEat3", ::canEat3, listOf(Action("eatAnyExcludingKarambwan", ::eatAnyExcludingKarambwan), Action("consumeSarabrewBrew", ::consumeSarabrewBrew), Action("eatKarambwan", ::eatKarambwan))),
        ConsumeAction("canEat2", ::canEat2, listOf(Action("eatAny", ::eatAny), Action("consumeSarabrewBrew", ::consumeSarabrewBrew))),
        ConsumeAction("canEat2NoBrew", ::canEat2NoBrew, listOf(Action("eatAnyExcludingKarambwan", ::eatAnyExcludingKarambwan), Action("eatKarambwan", ::eatKarambwan))),
        ConsumeAction("canEat1", ::canEat1, listOf(Action("eatAny", ::eatAny)))
    )

    override fun validate(): Boolean {
        if (AntiPkManager.tickPenalty > 0) return false
        return canRestore1() || canEat1()
    }

    override fun execute() {
        var lastTickDuration = System.currentTimeMillis() - AntiPkManager.tickStartTime
        log.info("Time since last tick: $lastTickDuration ms, current penalty: ${AntiPkManager.tickPenalty}")

        val currentHealth = Health.getCurrent()
        val currentPrayer = Prayer.getPoints()

        AntiPkManager.tickPenalty = 3
        val elapsed = measureTimeMillis {
            consumeActions.firstOrNull { it.condition() }?.let { action ->
                logTickInfo("Executing action: ${action.name} at $currentHealth health $currentPrayer prayer")
                action.actions.forEach { logActionTime(it.name) { it.function() } }
            }
        }

        lastTickDuration = System.currentTimeMillis() - AntiPkManager.tickStartTime
        log.info("Time since last tick: $lastTickDuration ms, current penalty: ${AntiPkManager.tickPenalty}")
        log.info("Execution time: $elapsed ms")
    }

    private inline fun logActionTime(actionName: String, action: () -> Boolean) {
        val time = measureTimeMillis { action() }
        log.info("Time to consume $actionName: $time ms")
    }

    private fun logTickInfo(message: String) {
        log.info(message)
    }

    private fun canEat1() =
        Health.getCurrent() < minHealth && Food.haveValidFood()

    private fun canRestore1() =
        Prayer.getPoints() < minPrayer && Restore.isPrayerRestoreAvailable()

    private fun canEat1Restore1() =
        canEat1() && canRestore1()

    private fun canEat2() =
        Health.getCurrent() < minHealth + Restore.getSarabrewHeal(Health.getMaximum())
                && HitpointsRestore.SaradominBrew.isAvailable()

    private fun canEat2NoBrew() =
        Health.getCurrent() < minHealth + Food.Karambwan.heal
        && (Food.BlightedKarambwan.isAvailable() || Food.Karambwan.isAvailable())

    private fun canEat2Restore1() =
        canEat2NoBrew() && canRestore1()

    private fun canEat3() =
        Health.getCurrent() < minHealth + Restore.getSarabrewHeal(Health.getMaximum()) + Food.Karambwan.heal
        && (Food.BlightedKarambwan.isAvailable() || Food.Karambwan.isAvailable())
        && HitpointsRestore.SaradominBrew.isAvailable()

    private fun eatAny() =
        Food.eatAny()

    private fun eatAnyExcludingKarambwan() =
        Food.eatAnyExcludingKarambwan()

    private fun eatKarambwan() =
        Food.eatKarambwan()

    private fun consumeRestore() =
        Restore.restorePrayer()

    private fun consumeSarabrewBrew() =
        HitpointsRestore.SaradominBrew.consume()

    private data class ConsumeAction(val name: String, val condition: () -> Boolean, val actions: List<Action>)

    private data class Action(val name: String, val function: () -> Boolean)


}