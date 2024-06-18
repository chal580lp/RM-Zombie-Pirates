package com.runemate.common.item

import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory
import com.runemate.game.api.osrs.local.hud.interfaces.Prayer
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.hud.interfaces.Health
import com.runemate.game.api.hybrid.location.navigation.Traversal
import com.runemate.game.api.script.Execution
import java.util.regex.Pattern

enum class RestoreType {
    PRAYER, ENERGY, HITPOINTS, ANTIDOTE, ANTI_VENOM, ANTIPOISON
}

sealed class Restore(val title: String, val id: Int) {
    abstract val restoreType: RestoreType
    val gameName: Pattern
        get() = Pattern.compile(title.replace("(4)", "\\(\\d+\\)"))

    companion object {
        val ALL_RESTORES: Set<Restore> by lazy {
            val subclasses = listOf(
                PrayerRestore::class,
                EnergyRestore::class,
                HitpointsRestore::class,
                AntidoteRestore::class,
                AntiVenomRestore::class,
                AntiPoisonRestore::class
            )

            subclasses.flatMap { subclass ->
                subclass.sealedSubclasses.mapNotNull { it.objectInstance }
            }.toSet()
        }

        val RECOMMENDED_RESTORES: Set<Restore> by lazy {
            setOf(
                PrayerRestore.BlightedSuperRestore, PrayerRestore.SuperRestore, PrayerRestore.PrayerPotion,
                EnergyRestore.StaminaPotion, HitpointsRestore.SaradominBrew, AntiPoisonRestore.Antipoison
            )
        }

        val PRAYER_RESTORES: Set<Restore> by lazy {
            setOf(
                PrayerRestore.BlightedSuperRestore, PrayerRestore.SuperRestore, PrayerRestore.PrayerPotion
            )
        }

        val ANTIPOISONS: Set<Restore> by lazy {
            setOf(
                AntidoteRestore.AntidotePlus, AntidoteRestore.AntidotePlusPlus, AntidoteRestore.Antidote, AntiPoisonRestore.Antipoison
            )
        }

        val ENERGY_RESTORES: Set<Restore> by lazy {
            setOf(
                EnergyRestore.StaminaPotion, EnergyRestore.EnergyPotion, EnergyRestore.SuperEnergy
            )
        }

        fun getRestoreAmount(): Int = (Prayer.getMaximumPoints() / 4) + 8

        fun getVirtualInvPrayer(): Int {
            val restores = Inventory.newQuery().names(*PRAYER_RESTORES.map { it.gameName }.toTypedArray()).unnoted().results()
            val doses = restores.sumOf { it?.name?.let { it1 -> extractDoses(it1) } ?: 0 }
            return doses * getRestoreAmount()
        }

        private fun extractDoses(itemName: String): Int = itemName.split("(").getOrNull(1)?.split(")")?.getOrNull(0)?.trim()?.toInt() ?: 0

        fun isRestore(itemName: String): Boolean = ALL_RESTORES.any { it.gameName.matcher(itemName).matches() }


        fun isMaxDose(itemName: String): Boolean {
            return ALL_RESTORES.any { restore ->
                (restore.title == itemName)
            }
        }

        fun isPrayerRestoreAvailable(): Boolean {
            return Inventory.newQuery().filter {
                    item -> PRAYER_RESTORES.any {
                it.gameName.matcher(item.definition?.name ?: "").matches()
                        && util.blightedCheck(item.definition?.name ?: "")}
            }.results().isNotEmpty()
        }

        fun isAntiPoisonAvailable(): Boolean {
            return Inventory.newQuery().filter {
                    item -> ANTIPOISONS.any{ it.gameName.matcher(item.definition?.name ?: "").matches()}
            }.results().isNotEmpty()
        }

        fun getSarabrewHeal(hitpoints: Int): Int {
            return (hitpoints * 15 / 100) + 2
        }

        private fun restore(resourceType: Set<Restore>, condition: () -> Boolean): Boolean {
            val initialCondition = condition()
            for (b in resourceType) {
                if (util.blightedCheck(b.gameName)) {
                    if (util.consume(b.gameName, "Drink")) {
                        Execution.delayUntil({ condition() != initialCondition }, 600)
                        return true
                    }
                }
            }
            return false
        }

        fun restorePrayer(): Boolean {
            val initialPoints = Prayer.getPoints()
            return restore(PRAYER_RESTORES) { Prayer.getPoints() > initialPoints }
        }

        fun curePoison(): Boolean = restore(ANTIPOISONS) { !Health.isPoisoned() }

        fun restoreEnergy(): Boolean {
            val initialEnergy = Traversal.getRunEnergy()
            return restore(ENERGY_RESTORES) { Traversal.getRunEnergy() > initialEnergy }
        }
    }
}

sealed class PrayerRestore(title: String, id: Int) : Restore(title, id) {
    override val restoreType = RestoreType.PRAYER

    object BlightedSuperRestore : PrayerRestore("Blighted super restore(4)", 24598)
    object SuperRestore : PrayerRestore("Super restore(4)", 3024)
    object PrayerPotion : PrayerRestore("Prayer potion(4)", 2434)
}

sealed class EnergyRestore(title: String, id: Int) : Restore(title, id) {
    override val restoreType = RestoreType.ENERGY

    object StaminaPotion : EnergyRestore("Stamina potion(4)", 12625)
    object EnergyPotion : EnergyRestore("Energy potion(4)", 3010)
    object SuperEnergy : EnergyRestore("Super energy(4)", 3016)
}

sealed class HitpointsRestore(title: String, id: Int) : Restore(title, id) {
    override val restoreType = RestoreType.HITPOINTS

    object SaradominBrew : HitpointsRestore("Saradomin brew(4)", 6685)
    object SanfewSerum : HitpointsRestore("Sanfew serum(4)", 10925)
}

sealed class AntidoteRestore(title: String, id: Int) : Restore(title, id) {
    override val restoreType = RestoreType.ANTIDOTE

    object AntidotePlus : AntidoteRestore("Antidote+(4)", 5943)
    object AntidotePlusPlus : AntidoteRestore("Antidote++(4)", 5952)
    object Antidote : AntidoteRestore("Antidote(4)", 5945)
}

sealed class AntiVenomRestore(title: String, id: Int) : Restore(title, id) {
    override val restoreType = RestoreType.ANTI_VENOM

    object AntiVenom : AntiVenomRestore("Anti-venom(4)", 12907)
    object AntiVenomPlus : AntiVenomRestore("Anti-venom+(4)", 12913)
    object AntiVenomPlusPlus : AntiVenomRestore("Anti-venom++(4)", 12919)
}

sealed class AntiPoisonRestore(title: String, id: Int) : Restore(title, id) {
    override val restoreType = RestoreType.ANTIPOISON

    object Antipoison : AntiPoisonRestore("Antipoison(4)", 2446)
}

fun Restore.isAvailable(): Boolean {
    return Inventory.newQuery()
        .names(*Restore.ALL_RESTORES.map { it.gameName }.toTypedArray())
        .filter { util.blightedCheck(it.name ?: "") }
        .unnoted()
        .results()
        .isNotEmpty()
}

fun Restore.consume(): Boolean {
    val restore = Inventory.newQuery()
        .names(*Restore.ALL_RESTORES.map { it.gameName }.toTypedArray())
        .filter { util.blightedCheck(it.name ?: "") }
        .unnoted()
        .results()
        .firstOrNull()

    return restore?.let { util.consume(it, "Drink") } == true
}

fun Restore.SaradominBrewStatDrain(statLevel: Int): Int {
    return when (this) {
        is HitpointsRestore.SaradominBrew -> (statLevel / 10) + 2
        else -> 0
    }
}

fun Restore.SaradominBrewDefenseBoost(defenseLevel: Int): Int {
    return when (this) {
        is HitpointsRestore.SaradominBrew -> (defenseLevel * 15 / 100) + 2
        else -> 0
    }
}