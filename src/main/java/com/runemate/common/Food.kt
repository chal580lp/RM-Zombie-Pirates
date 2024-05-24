package com.runemate.common

import com.runemate.game.api.hybrid.entities.definitions.ItemDefinition
import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.local.hud.interfaces.Health
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import java.util.logging.Logger

enum class Food(val gameName: String, val tickDelay: Int, val heals: Int) {
    Shark("Shark", 3, 20),
    MantaRay("Manta ray", 3, 22),
    Anglerfish("Anglerfish", 3, 22),
    TunaPotato("Tuna potato", 3, 22),
    EggPotato("Egg potato", 3, 16),
    ChilliPotato("Chilli potato", 3, 14),
    Karambwan("Cooked karambwan", 3, 18),
    PotatoWithCheese("Potato with cheese", 3, 16),
    Monkfish("Monkfish", 3, 16),
    Bass("Bass", 3, 13),
    Swordfish("Swordfish", 3, 14),
    SummerPie("Summer pie", 3, 11),
    SummerPieHalf("Half a summer pie", 3, 11),
    DarkCrab("Dark crab", 3, 22);

    companion object {
        //private val log: Logger = LoggerFactory.getLogger("Food")

        fun isFood(itemDefinition: ItemDefinition?): Boolean {
            if (itemDefinition == null || itemDefinition.isNoted) return false
            return values().any { it.gameName == itemDefinition.name }
        }

        fun fromName(name: String?): Food? {
            return entries.find { it.gameName == name }
        }

        fun dropWorst(): Boolean {
            val foods = Inventory.newQuery()
                .names(*entries.map { it.gameName }.toTypedArray())
                .unnoted()
                .results()
            if (foods.isEmpty()) return false

            val minFood = foods.minByOrNull {
                val definition = it.definition ?: return@minByOrNull -1
                fromName(definition.name)?.heals ?: -1
            } ?: return false

            DI.send(MenuAction.forSpriteItem(minFood, "Drop"))
            return true
        }

//        fun dropAny(): Boolean {
//            val foods = Inventory.newQuery()
//                .names(*values().map { it.gameName }.toTypedArray())
//                .unnoted()
//                .results()
//            if (foods.isEmpty()) return false
//
//            DI.send(MenuAction.forSpriteItem(foods.first(), "Drop"))
//            return true
//        }

        fun getAny(): Food? {
            val foodItem = Inventory.newQuery()
                .names(*entries.map { it.gameName }.toTypedArray())
                .unnoted()
                .results()
                .firstOrNull() ?: return null

            return fromName(foodItem.definition?.name)
        }

//        fun eatAny(): Boolean {
//            val foodItem = Inventory.newQuery()
//                .names(*entries.map { it.gameName }.toTypedArray())
//                .unnoted()
//                .results()
//                .firstOrNull() ?: return false
//
//            log.debug("Health: {}", Health.getCurrent())
//            return util.consume(foodItem, "Eat")
//        }

        fun countInventory(): Int {
            val foods = Inventory.newQuery()
                .names(*entries.map { it.gameName }.toTypedArray())
                .unnoted()
                .results()
            return foods.size
        }
    }

    override fun toString(): String {
        return gameName
    }

//    fun eat(): Boolean {
//        return util.eat(this)
//    }
}
