package com.runemate.common.traverse

import com.runemate.common.item.items

object BurningAmuletTraverse : ItemTraverse() {

    fun traverse(dest: Destination) : Boolean {
        return doTraverseLoop(items.burningAmulet, dest.destinationAlias)
    }

    enum class Destination(private val options: String) {
        ChaosTemple("Chaos Temple"),
        BanditCamp("Bandit Camp"),
        LavaMaze("Lava Maze");

        val destinationAlias: String
            get() = options
    }
}
