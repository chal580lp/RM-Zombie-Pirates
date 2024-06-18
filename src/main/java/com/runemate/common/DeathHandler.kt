package com.runemate.common

import com.runemate.common.LoggerUtils.getLogger
import com.runemate.game.api.hybrid.entities.Npc
import com.runemate.game.api.hybrid.local.Varbits
import com.runemate.game.api.hybrid.local.hud.interfaces.ChatDialog
import com.runemate.game.api.hybrid.local.hud.interfaces.Interfaces
import com.runemate.game.api.hybrid.location.Coordinate
import com.runemate.game.api.hybrid.location.navigation.cognizant.ScenePath
import com.runemate.game.api.hybrid.region.GameObjects
import com.runemate.game.api.hybrid.region.Npcs
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.script.Execution

class DeathHandler {
    companion object{
        private const val GRAVE_VARP = 1697 // Counts down if active, if not 0, you have active grave meaning items to collect.
        private const val GRAVE_VARBIT = 10465 // Seems to be more accurate than Varp
        private val LUMBRIDGE_GRAVE = Coordinate(3239, 3194, 0)
        private val FALADOR_GRAVE = Coordinate(2979, 3347, 0)
        private val EDGEVILLE_GRAVE = Coordinate(3093, 3491, 0)
        private val FEROX_GRAVE = Coordinate(3129, 3636, 0)
        private const val SELECT_OPTION_TITLE = "Select an Option"
        private const val FIRST_OPTION = "Can I collect the items from that gravestone now?"
        private const val SECOND_OPTION = "Bring my items here now; I'll pay your fee."
        private const val FIRST_CHAT = "Hello mortal. I see you have a gravestone, somewhere\r\nin the world. Did you want to ask me about it?"
        private const val SECOND_CHAT = "Can I collect the items from that gravestone now?"
        private const val THIRD_CHAT = "If you wish. However, I do charge for returning items\r\nhere - you would likely pay less if you collect them\r\nfrom the gravestone directly. I however expect it to be\r\nfree for this reclaim."
        private const val FOURTH_CHAT = "Bring my items here now; I'll pay your fee."
        private const val BAD_CHAT = "I'm sorry, but you have no items to collect from your gravestone."
    }

    private var reaper : Npc? = null
    private val log = getLogger("DeathHandler")

    private var itemsRetrieved = false


    fun execute() {
        reaper = Npcs.newQuery().names("Death").results().nearest()
        if (reaper == null) {
            traverseToReaper()
            return
        }
        if (!itemsRetrieved) {
            getItemsFromReaper()
            return
        }
        leaveDeathsDomain()

    }

    private fun traverseToReaper() {
        // Find the closest reaper location to determine where we are
        val player = Players.getLocal() ?: return
        val reaperLocation = listOf(LUMBRIDGE_GRAVE, FALADOR_GRAVE, EDGEVILLE_GRAVE, FEROX_GRAVE)
            .minByOrNull { it.distanceTo(player.position) }
        log.debug("Reaper location: {}", reaperLocation)

        reaperLocation?.let {
            val hole = GameObjects.newQuery().names("Death's Domain").results().nearest()
            if (hole == null || !hole.isVisible || hole.distanceTo(player) > 5) {
                ScenePath.buildTo(it)?.step()
                log.debug("Walking to reaper.")
                Execution.delayUntil({ player.isMoving == false }, 3000)
                return
            } else {
                // Walk to reaper
                //ScenePath.buildTo(it)?.step()
                //Execution.delayUntil({ player. }, 10000)

                // Climb down hole
                hole.interact("Enter")
                log.debug("Climbing down hole.")
                Execution.delayUntil({ reaper != null }, 4000)
                return
            }
        }
        log.debug("Failed to traverse to reaper.")

    }

    private fun getItemsFromReaper() {
        // Set reaper in case we need to manually talk-to & our exit condition for later
        if (reaper == null) {
            reaper = Npcs.newQuery().names("Death").results().nearest() ?: run {
                log.debug("Reaper NPC not found.")
                return
            }
        }
        // Check if the Take-All items interface is present
        val takeAllInterface = Interfaces.getAt(669, 10)
        if (takeAllInterface != null) {
            if (takeAllInterface.interact("Take-All")) {
                Execution.delay(1000)
                // TODO: Verify if items were taken
                itemsRetrieved = true
            }
        }

        // Check if the Close death interface is present
        val closeDeathInterface = Interfaces.getAt(669, 1, 11)
        if (closeDeathInterface != null) {
             if (closeDeathInterface.interact("Close")) {
                 Execution.delay(1000)
                 return
             }
        }
        // Talk to reset grave
        if (!ChatDialog.isOpen()) {
            reaper?.interact("Talk-to")
            Execution.delayUntil({ ChatDialog.isOpen() }, 2000)
        }

        if (ChatDialog.isOpen()) {
            when {
                ChatDialog.getText() == FIRST_CHAT -> {
                    ChatDialog.getContinue()?.select(true)
                }
                ChatDialog.getTitle() == SELECT_OPTION_TITLE && ChatDialog.getOption(FIRST_OPTION)?.isValid == true -> {
                    ChatDialog.getOption(FIRST_OPTION)?.select(true)
                }
                ChatDialog.getText() == SECOND_CHAT -> {
                    ChatDialog.getContinue()?.select(true)
                }
                ChatDialog.getText() == THIRD_CHAT -> {
                    ChatDialog.getContinue()?.select(true)
                }
                ChatDialog.getTitle() == SELECT_OPTION_TITLE && ChatDialog.getOption(SECOND_OPTION)?.isValid == true -> {
                    ChatDialog.getOption(SECOND_OPTION)?.select(true)
                }
                ChatDialog.getText() == FOURTH_CHAT -> {
                    ChatDialog.getContinue()?.select(true)
                }
                ChatDialog.getText() == BAD_CHAT -> {
                    itemsRetrieved = true
                    log.debug("No items to collect from grave.")
                }
                else -> {
                    ChatDialog.getContinue()?.select(true)
                }
            }
            Execution.delay(1000)
        }
    }
    private fun leaveDeathsDomain() {
        val portal = GameObjects.newQuery().names("Portal").results().nearest()
        portal?.interact("Use")
        Execution.delayUntil({ portal?.isValid == false }, 3000)
        //Execution.delay(1400)
    }

    fun verify() : Boolean {
        val reaperNear = Npcs.newQuery().names("Death").results().any()
        return Varbits.load(GRAVE_VARBIT)?.value != 0 || (reaperNear)
    }

}