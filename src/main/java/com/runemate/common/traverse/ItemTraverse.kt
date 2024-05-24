package com.runmate.common.traverse

import com.runemate.common.util
import com.runemate.game.api.hybrid.local.hud.interfaces.ChatDialog
import com.runemate.game.api.hybrid.local.hud.interfaces.Equipment
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.hybrid.util.Regex
import com.runemate.game.api.script.Execution
import java.util.regex.Pattern

open class ItemTraverse {
    var item : Pattern? = Regex.getPatternForContainsString("Burning amulet")
    var options : Array<String>? = null
    var interact : String = "Chaos Temple"

//    fun ItemTraverse(item: Pattern?, vararg options: String?) {
//        this.item = item!!
//        this.options = options
//    }

//    fun ItemTraverse(item: String?, vararg options: String?) {
//        this.item = Pattern.compile(item!!)
//        this.options = options
//    }
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