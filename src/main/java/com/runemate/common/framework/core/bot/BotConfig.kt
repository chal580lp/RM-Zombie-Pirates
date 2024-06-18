package com.runemate.common.framework.core.bot

import com.runemate.common.CombatStyle
import com.runemate.common.item.Food
import com.runemate.common.item.Boost
import com.runemate.common.item.Restore
import com.runemate.common.item.SetItem
import com.runemate.game.api.hybrid.location.Area
import kotlin.reflect.KProperty

interface BotConfig {

    val settingsGroup : String
    // Player Settings
    val food : Food
    val restore : Restore
    val boost : Boost
    val combatStyle : CombatStyle
    val minVirtualHealth : Int// Virtual health is current health + food.
    val minVirtualPrayer : Int // Virtual prayer is current prayer + restores.
    val minTripValue : Int // Minimum value of loot to bank
    val minLootValue : Int
    val prayerFlick : Boolean
    val inventory : MutableList<SetItem>
    val combatArea : Area
    val npcNames : List<String>
    val directInput : Boolean
    val geAmountMultiplier : Int
    val geUse5PercentButton : Boolean
    fun tester(int: Int) : Int

}
//class BotConfigDelegate<T>(private val getter: () -> T, private val setter: (T) -> Unit) : ReadWriteProperty<Any?, T> {
//    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
//        return getter()
//    }
//
//    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
//        setter(value)
//    }
//}
//class BotConfigDelegate<T>(
//    private val getter: () -> T,
//    private val setter: (T) -> Unit
//) {
//    private val value: T = getter()
//
//    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
//        return value
//    }
//
//    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
//        setter(value)
//        this.value = value
//    }
//}