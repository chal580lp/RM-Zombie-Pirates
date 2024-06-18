package com.runemate.common.traverse

import com.runemate.common.item.items


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