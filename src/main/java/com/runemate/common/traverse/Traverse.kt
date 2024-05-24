package com.runemate.common.traverse

import com.runemate.common.util
import com.runemate.game.api.hybrid.entities.details.Locatable
import com.runemate.game.api.hybrid.local.hud.interfaces.ChatDialog
import com.runemate.game.api.hybrid.local.hud.interfaces.Equipment
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.hybrid.web.WebPath
import com.runemate.game.api.hybrid.web.WebPathRequest
import com.runemate.game.api.script.Execution
import java.util.regex.Pattern

interface Traverse {

    companion object {
        fun getPathDestination(destination: Locatable, usingTeleports: Boolean, pathCache: HashMap<String, WebPath>): WebPath? {
            val key: String = destination.toString() + usingTeleports
            var cachedPath: WebPath? = pathCache[key]

            if (cachedPath == null) {
                cachedPath = WebPathRequest.builder()
                    .setDestination(destination)
                    .setUsingTeleports(usingTeleports)
                    .build()
            }
            if (cachedPath != null) {
                pathCache[key] = cachedPath
            }
            return cachedPath
        }
        fun doTraverseLoop(item : Pattern?, interact : String): Boolean {
            val region = Players.getLocal()!!.serverPosition.containingRegionId
            if (!Equipment.contains(item)) {
                val invItem = Inventory.getItems(item).first()
                if (util.isEquippable(invItem)) {
                    util.equip(invItem)
                    Execution.delayUntil({
                        Equipment.contains(
                            item
                        )
                    }, 560, 720)
                    return false
                }
            }
            val spriteItem: SpriteItem? = if (Equipment.contains(item))
                Equipment.getItems(item).first()
            else Inventory.getItems(item).first()

            if (spriteItem == null) {
                println("Unable to find " + item)
                return false
            }
            if (spriteItem.interact(interact)) {
                Execution.delayUntil({ ChatDialog.isOpen()}, 600)
                if (ChatDialog.isOpen()) {
                    ChatDialog.getOption(1)?.select()
                }
                Execution.delayUntil({ Players.getLocal()?.getServerPosition()?.getContainingRegionId() != region },
                    2500)
            }
            return Players.getLocal()?.getServerPosition()?.getContainingRegionId() != region
        }
    }
}