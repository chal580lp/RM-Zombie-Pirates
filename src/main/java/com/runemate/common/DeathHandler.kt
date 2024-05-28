package com.runemate.common

import com.runemate.game.api.hybrid.entities.Npc
import com.runemate.game.api.hybrid.local.hud.interfaces.ChatDialog
import com.runemate.game.api.hybrid.local.hud.interfaces.Interfaces
import com.runemate.game.api.hybrid.location.Coordinate
import com.runemate.game.api.hybrid.region.GameObjects
import com.runemate.game.api.hybrid.region.Npcs
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.script.Execution

class DeathHandler {

    private var reaper : Npc? = null
    private val log: RMLogger = RMLogger.getLogger(this::class.java)
    private val LUMBRIDGE_GRAVE = Coordinate(3239, 3194, 0)
    private val FALADOR_GRAVE = Coordinate(2979, 3347, 0)
    private val EDGEVILLE_GRAVE = Coordinate(3093, 3491, 0)
    private val FEROX_GRAVE = Coordinate(3129, 3636, 0)
    private val SELECTOPTIONTITLE = "Select an Option"
    private val FIRSTOPTION = "Can I collect the items from that gravestone now?"
    private val SECONDOPTION = "Bring my items here now; I'll pay your fee."
    private val FIRSTCHAT = "Hello mortal. I see you have a gravestone, somewhere\r\nin the world. Did you want to ask me about it?"
    private val SECONDCHAT = "Can I collect the items from that gravestone now?"
    private val THIRDCHAT = "If you wish. However, I do charge for returning items\r\nhere - you would likely pay less if you collect them\r\nfrom the gravestone directly. I however expect it to be\r\nfree for this reclaim."
    private val FOURTHCHAT = "Bring my items here now; I'll pay your fee."
    private val BADCHAT = "I'm sorry, but you have no items to collect from your gravestone."


    fun HandleDeath() {
        traverseToReaper()
        getItemsFromReaper()

    }

    private fun traverseToReaper() {
        // Find the closest reaper location to determine where we are
        val player = Players.getLocal() ?: return
        val reaperLocation = listOf(LUMBRIDGE_GRAVE, FALADOR_GRAVE, EDGEVILLE_GRAVE, FEROX_GRAVE)
            .minByOrNull { it.distanceTo(player.position) }

        reaperLocation?.let {
            // Walk to reaper
            //PathBuilder.buildTo(it).step()
            //Execution.delayUntil({ player.position.distanceTo(it) < 5 }, 10000)

            // Climb down hole
            val hole = GameObjects.newQuery().names("Death's Domain").results().nearest()
            hole?.interact("Climb-down")
            Execution.delayUntil({ Players.getLocal()?.isMoving == false }, 4000)
        }
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
            takeAllInterface.interact("Take-All")
            Execution.delay(1000)
        }

        // Check if the Close death interface is present
        val closeDeathInterface = Interfaces.getAt(669, 1, 11)
        if (closeDeathInterface != null) {
            closeDeathInterface.interact("Close")
            Execution.delay(1000)
        }
        // Talk to reset grave
        if (!ChatDialog.isOpen()) {
            reaper?.interact("Talk-to")
            Execution.delayUntil({ ChatDialog.isOpen() }, 2000)
        }

        if (ChatDialog.isOpen()) {
            when {
                ChatDialog.getText() == FIRSTCHAT -> {
                    ChatDialog.getContinue()?.select(true)
                }
                ChatDialog.getTitle() == SELECTOPTIONTITLE && ChatDialog.getOption(FIRSTOPTION)?.isValid == true -> {
                    ChatDialog.getOption(FIRSTOPTION)?.select(true)
                }
                ChatDialog.getText() == SECONDCHAT -> {
                    ChatDialog.getContinue()?.select(true)
                }
                ChatDialog.getText() == THIRDCHAT -> {
                    ChatDialog.getContinue()?.select(true)
                }
                ChatDialog.getTitle() == SELECTOPTIONTITLE && ChatDialog.getOption(SECONDOPTION)?.isValid == true -> {
                    ChatDialog.getOption(SECONDOPTION)?.select(true)
                }
                ChatDialog.getText() == FOURTHCHAT -> {
                    ChatDialog.getContinue()?.select(true)
                }
                else -> {
                    ChatDialog.getContinue()?.select(true)
                }
            }
            Execution.delay(1000)
        }
    }
    private fun leaveDeathsDomain() {
        val hole = GameObjects.newQuery().names("Portal").results().nearest()
        hole?.interact("Use")
        Execution.delayUntil({ Players.getLocal()?.isMoving == false }, 3000)
    }

}