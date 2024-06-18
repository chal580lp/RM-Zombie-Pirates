package com.runemate.zombiepirates

import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.CombatStyle
import com.runemate.common.item.*
import com.runemate.common.traverse.areas
import com.runemate.game.api.hybrid.location.Area
import com.runemate.ui.setting.annotation.open.*
import com.runemate.ui.setting.open.Settings

@SettingsGroup(group = "ZombiePirates")
interface ZombieSettings : BotConfig, Settings {
    companion object {
        @SettingsProvider
        @SettingsSection(title = "Bank Settings", description = "Banking logic", order = 0)
        const val CONFIGSECTION = "Bank Settings"

        @SettingsProvider
        @SettingsSection(title = "Other", description = "Misc...", order = 1)
        const val OTHERSETTINGS = "Other"

        @set:Setting(
            key = "testVal",
            title = "Test Val",
            description = "Test Value",
            section = OTHERSETTINGS
        )
        var testVal = 50
        init {
            println("ZombieSettings init")
        }


    }
    override val settingsGroup: String
        get() = "ZombiePirates"

    // Player Settings
    override val food: Food
        get() = Food.BlightedMantaRay

    override val restore: Restore
        get() = PrayerRestore.BlightedSuperRestore

    override val boost: Boost
        get() = Boost.DivineSuperCombat

    override val combatStyle: CombatStyle
        get() = CombatStyle.Melee

    @get:Range(min = 50, max = 200, step = 5)
    @get:Setting(
        key = "minVirtualHealth",
        title = "Minimum Virtual Health",
        description = "The amount of health (Current + Food) before we bank",
        section = CONFIGSECTION
    )
    override val minVirtualHealth: Int
        get() = 100

    @get:Range(min = 50, max = 200, step = 5)
    @get:Setting(
        key = "minVirtualPrayer",
        title = "Minimum Virtual Prayer",
        description = "The amount of prayer (Current + Restores) before we bank",
        section = CONFIGSECTION
    )
    override val minVirtualPrayer: Int
        get() = 100

    //@get:Suffix("k")
    @get:Range(min = 50, max = 10000, step = 10)
    @get:Setting(
        key = "minTripValue",
        title = "Minimum Trip Value",
        description = "The minimum value of loot before we bank",
        section = CONFIGSECTION
    )
    override val minTripValue: Int
        get() = 150_000

    override val minLootValue: Int
        get() = 3000

    @get:Setting(
        key = "prayerFlick",
        title = "Flick Prayer (DI REQUIRED)",
        description = "1Tick flicks prayer, NOT ACTIVE DURING ANTIPK so make sure to bring restores",
        section = OTHERSETTINGS
    )
    override val prayerFlick: Boolean
        get() = true

    override val inventory: MutableList<SetItem>
        get() = mutableListOf()

    override val directInput: Boolean
        get() = true

    // Bot Settings
    override val geUse5PercentButton: Boolean
        get() = true

    override val geAmountMultiplier: Int
        get() = 3

    override val combatArea: Area
        get() = areas.CHAOS_TEMPLE_AREA

    override val npcNames: List<String>
        get() = listOf("Zombie pirate")

    @get:Setting(
        key = "testVal",
        title = "Test Val",
        description = "Test Value",
        section = OTHERSETTINGS
    )
    val testValu: Int
        get() = 3000

    fun setTestVal(value: Int) {
        testVal = value
    }
}
