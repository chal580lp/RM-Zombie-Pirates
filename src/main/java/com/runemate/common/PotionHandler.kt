package com.runemate.common

import com.runemate.common.item.Boost
import com.runemate.game.api.hybrid.local.Skill

object PotionHandler {

    private fun consumeBoostIfNeeded(boost: Boost): Boolean {
        val skill = when (boost) {
            in Boost.ATTACK_BOOSTS -> Skill.ATTACK
            in Boost.STRENGTH_BOOSTS -> Skill.STRENGTH
            in Boost.DEFENCE_BOOSTS -> Skill.DEFENCE
            in Boost.RANGED_BOOSTS -> Skill.RANGED
            in Boost.MAGIC_BOOSTS -> Skill.MAGIC
            in Boost.MELEE_BOOSTS -> Skill.STRENGTH
            else -> return false
        }

        val currentBoost = skill.currentLevel - skill.baseLevel
        val boostThreshold = Boost.getBoostThreshold(boost, skill.baseLevel)

        if (currentBoost < boostThreshold) {
            return util.consume(boost.gameName, "Drink")
        }
        return false
    }

    fun boostIfNeeded(boost: Boost?): Boolean {
        return if (boost != null) {
            consumeBoostIfNeeded(boost)
        } else {
            false
        }
    }
}