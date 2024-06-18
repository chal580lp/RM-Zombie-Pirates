package com.runemate.common

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.local.Skills
import com.runemate.game.api.hybrid.local.WorldOverview
import com.runemate.game.api.hybrid.local.WorldType
import com.runemate.game.api.hybrid.local.Worlds
import com.runemate.game.api.hybrid.local.hud.interfaces.ChatDialog
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceComponent
import com.runemate.game.api.hybrid.local.hud.interfaces.Interfaces
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.osrs.local.hud.interfaces.ControlPanelTab
import com.runemate.game.api.osrs.location.WorldRegion
import com.runemate.game.api.script.Execution
import java.util.*

/**
 * Most of this class goes to the original author: @GoblinNeck
 */

object WorldHopper {
    private val log = getLogger("WorldHopper")

    private const val CONTAINER_INDEX = 69
    private val EXCLUDE = listOf(
        WorldType.QUEST_SPEEDRUNNING,
        WorldType.TOURNAMENT_WORLD,
        WorldType.PVP,
        WorldType.BOUNTY,
        WorldType.PVP_ARENA,
        WorldType.QUEST_SPEEDRUNNING,
        WorldType.LAST_MAN_STANDING,
        WorldType.BETA,
        WorldType.NOSAVE_MODE,
        WorldType.TOURNAMENT_WORLD,
        WorldType.FRESH_START_WORLD,
        WorldType.DEADMAN,
        WorldType.HIGH_RISK,
        WorldType.SEASONAL
    )

    fun isOpen(): Boolean {
        return ControlPanelTab.WORLD_HOP.isOpen
    }

    fun open(): Boolean {
        return ControlPanelTab.WORLD_HOP.open()
    }

    fun close(): Boolean {
        val component = Interfaces.newQuery()
            .containers(69)
            .actions("Close")
            .sprites(535)
            .heights(23)
            .widths(26)
            .types(InterfaceComponent.Type.SPRITE)
            .results()
            .first()
        return component?.interact("Close") ?: false
    }

    private fun to(targetWorld: Int): Boolean {
        log.info("Hopping to world {}", targetWorld)
        if (targetWorld < 0) {
            log.warn("Invalid request to hop to world {}", targetWorld)
            return false
        }
        val initialWorld = Worlds.getCurrent()
        if (targetWorld == initialWorld) {
            log.info("The target world of {} is already loaded.", targetWorld)
            return true
        }
        log.debug("Attempting to hop to world {} from {}", targetWorld, initialWorld)
        if (open()) {
            val wc = getWorldComponent(targetWorld)
            if (wc == null) {
                log.warn("Couldn't find world component")
                return false
            }
            val cc = getContainerComponent()
            if (cc == null) {
                log.warn("Couldn't find container component")
                return false
            }
            DI.send(MenuAction.forInterfaceComponent(wc, 0))
            Execution.delayUntil({ Worlds.getCurrent() == targetWorld || getHopWarningDialog() != null }, 6000, 9000)
            val confirm = getHopWarningDialog()
            if (confirm != null && confirm.select()) {
                Execution.delayUntil({ Worlds.getCurrent() == targetWorld }, 6000, 9000)
            }
        }
        return targetWorld == Worlds.getCurrent()
    }

    private fun getHopWarningDialog(): ChatDialog.Option? {
        return ChatDialog.getOption(
            "Yes. In future, only warn about dangerous worlds.",
            "Switch world",
            "Switch World"
        )
    }

    private fun getContainerComponent(): InterfaceComponent? {
        return Interfaces.newQuery()
            .containers(CONTAINER_INDEX)
            .types(InterfaceComponent.Type.CONTAINER)
            .grandchildren(false)
            .widths(174)
            .heights(193)
            .results()
            .first()
    }

    private fun getWorldComponent(worldId: Int): InterfaceComponent? {
        return Interfaces.newQuery()
            .containers(CONTAINER_INDEX)
            .types(InterfaceComponent.Type.SPRITE)
            .grandchildren(true)
            .names(worldId.toString())
            .visible()
            .results()
            .first()
    }

    private fun isSafeAndUsable(destination: WorldOverview, current: Int): Boolean {
        val totalLevel = Skills.getBaseLevels().sum()
        val types = EnumSet.copyOf(destination.worldTypes)
        return destination.id != current &&
                destination.isMembersOnly &&
                types.none { EXCLUDE.contains(it) } &&
                !WorldType.isPvp(destination.worldTypes) &&
                canTotalLevel(destination, totalLevel)
    }

    private fun canTotalLevel(destination: WorldOverview, totalLevel: Int): Boolean {
        return (!destination.isSkillTotal2200 || totalLevel >= 2200) &&
                (!destination.isSkillTotal2000 || totalLevel >= 2000) &&
                (!destination.isSkillTotal1750 || totalLevel >= 1750) &&
                (!destination.isSkillTotal1500 || totalLevel >= 1500) &&
                (!destination.isSkillTotal1250 || totalLevel >= 1250) &&
                (!destination.isSkillTotal750 || totalLevel >= 750) &&
                (!destination.isSkillTotal500 || totalLevel >= 500)
    }

    fun hop(world: Int): Boolean {
        log.debug("hopping")
        if (!isOpen() && !open()) {
            Execution.delay(1200, 1400)
            log.error("Unable to open world hopper")
            return false
        }
        val current = Worlds.getCurrent()
        log.debug("current {}", current)
        if (!to(world)) {
            return false
        }
        return Execution.delayUntil({ current != Worlds.getCurrent() && Players.getLocal() != null }, 9000, 12000)
    }

    fun hop(region: WorldRegion): Boolean {
        log.debug("hopping")
        if (!isOpen() && !open()) {
            Execution.delay(1200, 1400)
            log.error("Unable to open world hopper")
            return false
        }
        val current = Worlds.getCurrent()
        log.debug("current {}", current)
        val newWorldWithRegion = Worlds.newQuery()
            .filter { isSafeAndUsable(it, current) && it.region.alpha2 == region.alpha2 }
            .results()
        val newWorldWithNoRegion = Worlds.newQuery().filter { isSafeAndUsable(it, current) }.results()
        val newWorld = if (newWorldWithRegion.isEmpty()) newWorldWithNoRegion.random() else newWorldWithRegion.random()
        if (newWorld == null) return false
        if (!to(newWorld.id)) {
            return false
        }
        Execution.delayUntil({ current != Worlds.getCurrent() && Players.getLocal() != null }, 9000, 12000)
        Execution.delay(5000, 6000)
        return current != Worlds.getCurrent()
    }
    fun hopToRandomWorld(): Boolean {
        log.debug("hopping")
        if (!isOpen() && !open()) {
            Execution.delay(1200, 1400)
            log.error("Unable to open world hopper")
            return false
        }
        val current = Worlds.getCurrent()
        log.debug("current {}", current)
        val newWorld = Worlds.newQuery()
            .filter { isSafeAndUsable(it, current) }
            .results()
            .randomOrNull()
        if (newWorld == null) {
            log.warn("No suitable world found for hopping")
            return false
        }
        if (!to(newWorld.id)) {
            return false
        }
        Execution.delayUntil({ current != Worlds.getCurrent() && Players.getLocal() != null }, 9000, 12000)
        Execution.delay(5000, 6000)
        return current != Worlds.getCurrent()
    }
}