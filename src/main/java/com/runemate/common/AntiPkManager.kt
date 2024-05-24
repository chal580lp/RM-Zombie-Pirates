package com.runemate.common

import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.game.api.hybrid.region.Players

class AntiPkManager {

    fun beingAttacked() : Boolean {
        val player = Players.getLocal() ?: return false
        val wildlvl = Wilderness.getDepth(player)
        val cmbtlvl = player.combatLevel
        val low = cmbtlvl - wildlvl
        val high = cmbtlvl + wildlvl
        val players = Players.newQuery()
            .targeting(player)
            .combatLevels(low,high)
            .results()
        return players.any()
    }

}