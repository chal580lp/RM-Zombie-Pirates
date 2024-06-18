package com.runemate.common.item

import com.runemate.common.util
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem
import java.util.regex.Pattern

val SpriteItem.name: String?
    get() = definition?.name

val SpriteItem.isNumbered: Boolean
    get() = name?.let { util.isNumbered(it) } ?: false


fun SpriteItem.asSetItem(): SetItem? {
    val itemName = name ?: return null
    val itemId = id
    return SetItem(itemName, itemId, quantity = quantity)
}


fun List<EquipmentSetItem>.toSetItemList(): List<SetItem> {
    return this.map { it.setItem }
}
