package com.runemate.common.item

import com.runemate.common.CombatStyle
import java.util.regex.Pattern
import kotlin.math.roundToInt

sealed class Boost(val title: String, val combatStyle: CombatStyle, val id: Int) {
    object DivineSuperCombat : Boost("Divine super combat potion(4)", CombatStyle.Melee, 23685)
    object DivineSuperStrength : Boost("Divine super strength potion(4)", CombatStyle.Melee, 23709)
    object DivineRangedPotion : Boost("Divine ranged potion(4)", CombatStyle.Ranged, 23733)
    object DivineBattlemagePotion : Boost("Divine battlemage potion(4)", CombatStyle.Magic, 24623)
    object DivineMagicPotion : Boost("Divine magic potion(4)", CombatStyle.Magic, 23745)
    object SuperCombatPotion : Boost("Super combat potion(4)", CombatStyle.Melee, 12695)
    object SuperStrengthPotion : Boost("Super strength potion(4)", CombatStyle.Melee, 2440)
    object SuperAttackPotion : Boost("Super attack potion(4)", CombatStyle.Melee, 2436)
    object SuperDefencePotion : Boost("Super defence potion(4)", CombatStyle.Melee, 2442)
    object StrengthPotion : Boost("Strength potion(4)", CombatStyle.Melee, 113)
    object RangingPotion : Boost("Ranging potion(4)", CombatStyle.Ranged, 2444)
    object MagicPotion : Boost("Magic potion(4)", CombatStyle.Magic, 3040)

    val gameName: Pattern
        get() = Pattern.compile(title.replace("(4)", ".*\\(.*\\)"))

    companion object {

        val ALL_BOOSTS: Set<Boost> by lazy {
            Boost::class.sealedSubclasses.mapNotNull { it.objectInstance }.toSet()
        }

        val RECOMMENDED_BOOSTS: Set<Boost> by lazy {
            setOf(
                DivineSuperCombat, SuperCombatPotion,
                DivineRangedPotion, RangingPotion, StrengthPotion
            )
        }

        val ATTACK_BOOSTS: Set<Boost> by lazy {
            setOf(SuperAttackPotion)
        }

        val STRENGTH_BOOSTS: Set<Boost> by lazy {
            setOf(DivineSuperStrength, SuperStrengthPotion, StrengthPotion)
        }

        val DEFENCE_BOOSTS: Set<Boost> by lazy {
            setOf(SuperDefencePotion)
        }

        val MELEE_BOOSTS: Set<Boost> by lazy {
            setOf(DivineSuperCombat, SuperCombatPotion)
        }

        val RANGED_BOOSTS: Set<Boost> by lazy {
            setOf(DivineRangedPotion, RangingPotion)
        }

        val MAGIC_BOOSTS: Set<Boost> by lazy {
            setOf(DivineBattlemagePotion, DivineMagicPotion, MagicPotion)
        }

        fun find(id: Int): Boost? = ALL_BOOSTS.find { it.id == id }

        fun isBoost(itemName: String): Boolean = ALL_BOOSTS.any { it.gameName.matcher(itemName).matches() }

        private fun getSuperDivineBoost(lvl: Int): Int = (lvl / 6) + 5

        private fun getStrengthPotionBoost(lvl: Int): Int = (lvl / 10) + 3

        private fun getRangedPotionBoost(lvl: Int): Int = getStrengthPotionBoost(lvl) + 1

        fun getBoostThreshold(boost: Boost, lvl: Int): Int {
            return when (boost) {
                StrengthPotion -> (getStrengthPotionBoost(lvl) / 2 + 1.5).roundToInt()
                RangingPotion -> (getRangedPotionBoost(lvl) / 2 + 1.5).roundToInt()
                else -> getSuperDivineBoost(lvl) - 5
            }
        }
    }
}
