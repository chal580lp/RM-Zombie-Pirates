package com.runemate.zombiepirates.state.task.advance


import com.runemate.common.inCombat
import com.runemate.common.inCombatArea
import com.runemate.common.framework.core.Task
import com.runemate.common.framework.core.injected
import com.runemate.game.api.hybrid.location.Coordinate
import com.runemate.game.api.hybrid.location.navigation.cognizant.ScenePath
import com.runemate.game.api.hybrid.region.Npcs
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.hybrid.web.WebPath
import com.runemate.game.api.script.Execution
import com.runemate.ui.DefaultUI
import com.runemate.zombiepirates.Bot
import com.runemate.common.LoggerUtils.getLogger
import com.runemate.common.framework.core.addons.BotState
import com.runemate.common.item.items
import com.runemate.common.traverse.BurningAmuletTraverse
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.Wilderness


class TraverseToCombatArea : Task {
    private var path: WebPath? = null
    private val coordinate = Coordinate(3236,3627,0)
    private val log = getLogger("TraverseToCombatArea")


    val bot : Bot by injected()

    override fun validate(): Boolean {
        return !bot.getCombatManager().inCombatArea
    }

    override fun execute() {
        log.debug("Not in combat area")
        if (bot.getCombatManager().inCombat()) {
            log.debug("We are in Combat Area why are we traversing????")
            return
        }
        val npc = Npcs.newQuery().names("Elder Chaos druid").results().first()
        val scenePath = if (npc != null) {
            log.debug("Scene Path to NPC")
            ScenePath.buildTo(npc)
        } else {
            log.debug("Scene Path to Coordinate")
            ScenePath.buildTo(coordinate)
        }

        scenePath?.let {
            log.debug("Walking to combat area via Scene Path")
            it.step()
            Execution.delayUntil({ Players.getLocal()?.isMoving == true}, 300,600)
            Execution.delayUntil({ Players.getLocal()?.isMoving == false}, 600,1200)
            return
        }
        if (util.isItemInInventoryOrEquipment(items.burningAmulet)) {
            log.warn("Unable to find Scene Path, attempting to use Burning Amulet to teleport to Chaos Temple")
            if (BurningAmuletTraverse.traverse(BurningAmuletTraverse.Destination.ChaosTemple)) {
                Execution.delayUntil({ !Wilderness.isInWilderness() }, 1000)
            }
            return
        }
        log.warn("Unable to find Scene Path, no Burning Amulet found")
        bot.setCurrentState(BotState.RetreatState)
        //TODO: Implement WebPath or method if not near Chaos altar (e.g. Burning Amulet or Ring of Dueling)

    }

}