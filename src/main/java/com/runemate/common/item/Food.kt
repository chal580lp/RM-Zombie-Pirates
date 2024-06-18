package com.runemate.common.item

import com.runemate.common.util
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import org.apache.logging.log4j.LogManager

sealed class Food(val gameName: String, val heal: Int, val id: Int) {
    object BlightedMantaRay : Food("Blighted manta ray", 22, 24589)
    object BlightedAnglerfish : Food("Blighted anglerfish", 15, 24592)
    object BlightedKarambwan : Food("Blighted karambwan", 18, 24595)
    object Lobster : Food("Lobster", 12, 379)
    object Shark : Food("Shark", 20, 385)
    object MantaRay : Food("Manta ray", 22, 391)
    object Anglerfish : Food("Anglerfish", 22, 13441)
    object TunaPotato : Food("Tuna potato", 22, 7060)
    object EggPotato : Food("Egg potato", 16, 7056)
    object ChilliPotato : Food("Chilli potato", 14, 7054)
    object Karambwan : Food("Cooked karambwan", 18, 3144)
    object PotatoWithCheese : Food("Potato with cheese", 16, 6705)
    object Monkfish : Food("Monkfish", 16, 7946)
    object Bass : Food("Bass", 13, 365)
    object Swordfish : Food("Swordfish", 14, 373)
    object SummerPie : Food("Summer pie", 11, 7218)
    object SummerPieHalf : Food("Half a summer pie", 11, 7220)
    object DarkCrab : Food("Dark crab", 22, 11936)

    override fun toString(): String = gameName

    companion object {
        private val log = LogManager.getLogger(Food::class.java)

        val ALL_FOODS: Set<Food> by lazy {
            Food::class.sealedSubclasses.mapNotNull { it.objectInstance }.toSet()
        }

        val ZOMBIE_PIRATE_FOODS: Set<Food> by lazy {
            setOf(BlightedAnglerfish, BlightedMantaRay, BlightedKarambwan, Swordfish, Lobster)
        }


        fun getFoodType(id: Int): Food? = ALL_FOODS.find { it.id == id }

        fun isFood(name: String): Boolean = ALL_FOODS.any { it.gameName == name }

        fun countInventory(): Int = Inventory.newQuery()
            .names(*ALL_FOODS.map { it.gameName }.toTypedArray())
            .unnoted()
            .results()
            .size

        fun haveValidFood(): Boolean = Inventory.newQuery()
            .names(*ALL_FOODS.map { it.gameName }.toTypedArray())
            .filter { util.blightedCheck(it.name ?: "") }
            .unnoted()
            .results()
            .isNotEmpty()

        fun eatAny(): Boolean {
            if (eatAnyExcludingKarambwan()) {
                return true
            }
            return eatKarambwan()
        }

        fun eatAnyExcludingKarambwan(): Boolean {
            val food = Inventory.newQuery()
                .names(*ALL_FOODS.filter { it !is Karambwan && it !is BlightedKarambwan }.map { it.gameName }.toTypedArray())
                .filter { util.blightedCheck(it.name ?: "") }
                .unnoted()
                .results()
                .firstOrNull() ?: return false
            util.consume(food, "Eat")
            return true
        }

        fun eatKarambwan(): Boolean {
            val karambwan = Inventory.newQuery()
                .names("Cooked karambwan", "Blighted karambwan")
                .filter { util.blightedCheck(it.name ?: "") }
                .unnoted()
                .results()
                .firstOrNull() ?: return false
            util.consume(karambwan, "Eat")
            return true
        }

        fun getVirtualInvHealth(): Int = Inventory.newQuery()
            .names(*ALL_FOODS.map { it.gameName }.toTypedArray())
            .unnoted()
            .results()
            .sumOf { getFoodType(it.id)?.heal ?: 0 }
    }
}

fun Food.isAvailable(): Boolean {
    return Inventory.contains { it.name?.let { name -> name.equals(this.gameName, ignoreCase = true) } == true }
}
fun Food.Karambwan.eat(): Boolean {
    val karambwan = Inventory.newQuery()
        .names("Cooked karambwan", "Blighted karambwan")
        .filter { util.blightedCheck(it.name ?: "") }
        .unnoted()
        .results()
        .firstOrNull() ?: return false
    util.consume(karambwan, "Eat")
    return true
}
