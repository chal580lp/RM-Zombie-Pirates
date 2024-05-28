package com.runmate.common.traverse

import com.runemate.common.RMLogger
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.hud.interfaces.ChatDialog
import com.runemate.game.api.hybrid.local.hud.interfaces.Equipment
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceComponent
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem
import com.runemate.game.api.hybrid.region.Players
import com.runemate.game.api.hybrid.util.Regex
import com.runemate.game.api.osrs.local.hud.interfaces.ControlPanelTab
import com.runemate.game.api.script.Execution
import java.util.regex.Pattern

open class ItemTraverse {
    private val log: RMLogger = RMLogger.getLogger(this::class.java)

    fun doTraverseLoop(item: Pattern?, interact: String): Boolean {
        val region = Players.getLocal()?.serverPosition?.containingRegionId ?: run {
            log.debug("Region is null, cannot proceed.")
            return false
        }

        // Store the current state of all equipped item IDs
        val currentEquipmentIds = Equipment.getItems().map { it.id }.toSet()

        if (!Equipment.contains(item)) {
            val invItem = Inventory.getItems(item).firstOrNull() ?: run {
                log.debug("Inventory does not contain the item: $item")
                return false
            }
                log.debug("Equipping item: $invItem")
                util.equip(invItem) // item might not be one we can equip
                Execution.delayUntil({ Equipment.contains(item) }, 560, 720)
        }

        val spriteItem: SpriteItem? = if (Equipment.contains(item)) {
            Equipment.getItems(item).firstOrNull()
        } else {
            Inventory.getItems(item).firstOrNull()
        }

        if (spriteItem == null) {
            log.debug("Unable to find the item: $item")
            return false
        }

        if (spriteItem.interact(interact)) {
            Execution.delayUntil({ ChatDialog.isOpen() }, 600)
            if (ChatDialog.isOpen()) {
                ChatDialog.getOption(1)?.select()
            }
            Execution.delayUntil({ Players.getLocal()?.serverPosition?.containingRegionId != region }, 2500)
        }

        // Check for items that have appeared in the inventory and match previously equipped item IDs
        val newInventoryItems = Inventory.getItems().filter { it.id in currentEquipmentIds }
        if (newInventoryItems.size == 1) {
            val originalItem = newInventoryItems.first()
            // Re-equip the original item if it was unequipped and is in the inventory
            reEquipOriginalItem(originalItem)
        } else if (newInventoryItems.size > 1) {
            log.debug("More than one item appeared in the inventory: ${newInventoryItems.joinToString { it.definition?.name ?: it.id.toString() }}")
        }

        return Players.getLocal()?.serverPosition?.containingRegionId != region
    }

    private fun reEquipOriginalItem(originalItem: SpriteItem?) {
        if (originalItem != null && !Equipment.contains(originalItem.id)) {
            if (Inventory.contains(originalItem.id)) {
                util.equip(originalItem)
                Execution.delayUntil({ Equipment.contains(originalItem.id) }, 560, 720)
            }
        }
    }
}