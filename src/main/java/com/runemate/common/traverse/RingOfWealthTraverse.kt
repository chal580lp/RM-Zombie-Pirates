package com.runemate.common.traverse

import com.runemate.common.item.items

object RingOfWealthTraverse : ItemTraverse() {

    fun traverse(dest: Destination) : Boolean {
        return doTraverseLoop(items.ringOfWealth, dest.destinationAlias)
    }

    enum class Destination(private val options: String) {
        GrandSexchange("Grand Exchange");

        val destinationAlias: String
            get() = options
    }
}