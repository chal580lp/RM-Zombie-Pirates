package com.runemate.common.traverse

import com.runemate.common.items
import com.runemate.common.util
import com.runemate.game.api.hybrid.entities.Item
import com.runmate.common.traverse.ItemTraverse
import java.util.regex.Pattern


object DuelingRingTraverse : ItemTraverse() {

    fun traverse(dest: Destination) : Boolean {
        return doTraverseLoop(items.ringOfDueling, dest.destinationAlias)
    }

    enum class Destination(private vararg val options: String) {
        PvPArena("Al Kharid PvP Arena.", "PvP Arena"),
        CastleWars("Castle Wars Arena.", "Castle Wars"),
        Ferox("Ferox Enclave.", "Ferox Enclave");

        val destinationAlias: String
            get() = options[1]
    }
}