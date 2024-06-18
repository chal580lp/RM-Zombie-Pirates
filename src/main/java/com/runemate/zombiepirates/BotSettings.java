//package com.runemate.zombiepirates;
//
//
//import com.runemate.ui.setting.annotation.open.*;
//import com.runemate.ui.setting.open.Settings;
//
//@SettingsGroup(group = "ZombiePirates")
//public interface BotSettings extends Settings {
//
//    @SettingsProvider
//    @SettingsSection(title = "Bank Settings", description = "Banking logic", order = 0)
//    static final String CONFIGSECTION = "Bank Settings";
//
//    @SettingsProvider
//    @SettingsSection(title = "Other", description = "Misc...", order = 1)
//    static final String OTHERSETTINGS = "Other";
//
//    @Range(min = 50, max = 200, step = 5)
//    @Setting(
//            key = "minVirtualHealth",
//            title = "Minimum Virtual Health",
//            description = "The amount of health (Current + Food) before we bank",
//            section = CONFIGSECTION
//    )
//    default int minVirtualHealth() {
//        return ZombieSettings.INSTANCE.getMinVirtualHealth();
//    }
//
//    @Range(min = 50, max = 200, step = 5)
//    @Setting(
//            key = "minVirtualPrayer",
//            title = "Minimum Virtual Prayer",
//            description = "The amount of prayer (Current + Restores) before we bank",
//            section = CONFIGSECTION
//    )
//    default int minVirtualPrayer() {
//        return ZombieSettings.INSTANCE.getMinVirtualPrayer();
//    }
//
//    @Suffix("k")
//    @Range(min = 50, max = 10000, step = 10)
//    @Setting(
//            key = "minTripValue",
//            title = "Minimum Trip Value",
//            description = "The minimum value of loot before we bank",
//            section = CONFIGSECTION
//    )
//    default int minTripValue() {
//        return ZombieSettings.INSTANCE.getMinTripValue() / 1000;
//    }
//
//    @Setting(
//            key = "prayerFlick",
//            title = "Flick Prayer (DI REQUIRED)",
//            description = "1Tick flicks prayer, NOT ACTIVE DURING ANTIPK so make sure to bring restores",
//            section = OTHERSETTINGS
//    )
//    default boolean getFlickPrayer() {
//        return ZombieSettings.INSTANCE.getPrayerFlick();
//    }
//}