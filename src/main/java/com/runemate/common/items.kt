package com.runemate.common

import java.util.*
import java.util.regex.Pattern

object items {
    val houseTab: Pattern = Pattern.compile("^Teleport to house$")
    const val pharaohSceptre: String = "Pharaoh’s Sceptre"
    const val spectralSpiritShield: String = "Spectral spirit shield"
    const val keyMasterTeleport: String = "Key master teleport"
    val xericsTalisman: Pattern = Pattern.compile("^Xeric's talisman$", Pattern.CASE_INSENSITIVE)
    const val wildySword4: String = "Wilderness sword 4"
    const val desertAmulet4: String = "Desert amulet 4"
    const val fremBoots4: String = "Fremennik sea boots 4"
    val enchantedLyre: Pattern = Pattern.compile("Enchanted lyre\\(i\\)")
    val craftingCape: Pattern = Pattern.compile("Crafting cape.*")
    val stamina4Dose: Pattern = Pattern.compile("Stamina potion\\(4\\)")
    val wildySword: Pattern = Pattern.compile("Wilderness sword.*")
    val prayer4Dose: Pattern = Pattern.compile("Prayer potion\\(4\\)")
    val constructionCape: Pattern = Pattern.compile("Construct\\. cape.*")
    val soulBearer: Pattern = Pattern.compile("Soul bearer")
    val bonecrusher: Pattern = Pattern.compile("^Bonecrusher|Bonecrusher necklace$")
    val herbSack: Pattern = Pattern.compile(".*herb sack", Pattern.CASE_INSENSITIVE)
    val radasBlessing: Pattern = Pattern.compile("Rada.*")
    val radasBlessing4: Pattern = Pattern.compile("^Rada's blessing 4$")
    val drakans: Pattern = Pattern.compile("^Drakan's medallion$")
    val argdougneCloak: Pattern = Pattern.compile("Ardougne cloak.*")
    val ringOfWealth: Pattern = Pattern.compile("Ring of wealth.*\\(.*")
    val amuletOfGlory: Pattern = Pattern.compile("Amulet of glory.*\\(.*")
    val questCape: Pattern = Pattern.compile("Quest point cape.*")
    const val rope: String = "Rope"
    const val crystalChime: String = "Crystal chime"
    const val conCapeTelePoh: String = "Tele to POH"
    val runePouch: Pattern = Pattern.compile("(?:(?:Divine rune)|(?:Rune)) pouch.*")
    val gemBag: Pattern = Pattern.compile(".*gem bag", Pattern.CASE_INSENSITIVE)
    val bookOfDead: Pattern = Pattern.compile("^Book of the dead|Kharedst's memoirs$")
    val staminaPotions: Pattern = Pattern.compile("Stamina potion.*")
    const val spade: String = "Spade"
    const val wildPie: String = "Wild pie"
    const val wildPieHalf: String = "Half a wild pie"
    val ashSanctifier: Pattern = Pattern.compile("^Ash sanctifier$")
    val spadeP: Pattern = Pattern.compile("Spade")
    val dramenStaff: Pattern = Pattern.compile("(?:(?:Dramen)|(?:Lunar)) staff")
    const val locatorOrb: String = "Locator orb"
    const val dwarvenRock: String = "Dwarven rock cake"
    val rangedBoosts: List<Int> = mutableListOf(
        23742,  // divine
        23739,
        23736,
        23733,
        173,  // ranged
        171,
        169,
        2444
    )
    val combatBoosts: List<Int> = mutableListOf(
        23694,  //1 dose divine
        23691,  //2 dose divine
        23688,  //3 dose divine
        23685,  //4 dose divine
        12701,  //1 dose
        12699,  //2 dose
        12697,  //3 dose
        12695 //4 dose
    )
    val attackBoosts: List<Int> = mutableListOf(
        23706,  //1 dose divine
        23703,  //2 dose divine
        23700,  //3 dose divine
        23697,  //4 dose divine
        149,  //1 dose
        147,  //2 dose
        145,  //3 dose
        2436 //4 dose
    )
    val strengthBoosts: List<Int> = mutableListOf(
        23718,  //1 dose divine
        23715,  //2 dose divine
        23712,  //3 dose divine
        23709,  //4 dose divine
        161,  //1 dose
        159,  //2 dose
        157,  //3 dose
        2440 //4 dose
    )
    val defenceBoosts: List<Int> = mutableListOf(
        23730,  //1 dose divine
        23727,  //2 dose divine
        23724,  //3 dose divine
        23721,  //4 dose divine
        167,  //1 dose
        165,  //2 dose
        163,  //3 dose
        2442 //4 dose
    )
    val prayerRestores: List<Int> = mutableListOf(
        143,  //1 dose prayer
        141,  //2 dose prayer
        139,  //3 dose prayer
        2434,  //4 dose prayer
        3030,  //1 dose super restore
        3028,  //2 dose super restore
        3026,  //3 dose super restore
        3024 //4 dose super restore
    )
    val antiPoisonPP: List<Int> = mutableListOf(
        5958,
        5956,
        5954,
        5952
    )
    val antiPoisonPPattern: List<Pattern> = Arrays.asList(
        Pattern.compile("Superantipoison.*"),
        Pattern.compile("Antipoison.*")
    )
    val imbuedHeart: Pattern = Pattern.compile("(?:(?:Imbued)|(?:Saturated)) heart")
    val ringOfDueling: Pattern = Pattern.compile("Ring of dueling.*\\(.*\\)")
    val burningAmulet: Pattern = Pattern.compile("Burning amulet.*\\(.*\\)")
    val gamesNecklace: Pattern = Pattern.compile("^Games necklace.*$")
    val iceCooler: Pattern = Pattern.compile("^Ice cooler$")
    val ringOfTheElements: Pattern = Pattern.compile("Ring of the elements.*")
    val digsitePendant: Pattern = Pattern.compile("Digsite pendant.*", Pattern.CASE_INSENSITIVE)
    val slayerRing: Pattern = Pattern.compile("Slayer ring.*")
    const val invigorate: String = "Invigorate"
    const val braceletOfSlaughter: String = "Bracelet of slaughter"
    const val expeditiousBracelet: String = "Expeditious bracelet"
    val skullSceptre: Pattern = Pattern.compile("Skull sceptre.*", Pattern.CASE_INSENSITIVE)
    const val royalSeedPod: String = "Royal seed pod"
    const val grandSeedPod: String = "Grand seed pod"
    var skillsNecklace: Pattern = Pattern.compile("Skills necklace.*\\(.*")
    var fishingCape: Pattern = Pattern.compile("Fishing cape.*")
    var farmingCape: Pattern = Pattern.compile("Farming cape.*")
    var slayerCape: Pattern = Pattern.compile("Slayer cape.*")
}
enum class ChargedItem(val itemName: String, val chargeType: String, val maxCharges: Int) {
    TRIDENT_OF_THE_SEAS("Trident of the seas", "Runes", 2500),
    TRIDENT_OF_THE_SWAMP("Trident of the swamp", "Runes", 2500),
    SANGUINESTI_STAFF("Sanguinesti staff", "Blood rune", 20000),
    CRAWS_BOW("Craw's bow", "Ether", 16000),
    URSINE_CHAINMACE("Ursine chainmace", "Ether", 16000),
    VIGGORAS_CHAINMACE("Viggora's chainmace", "Ether", 16000),
    ZARYTE_BOW("Zaryte bow", "Charges", 10000),
    CRYSTAL_BOW("Crystal bow", "Charges", 2500),
    ABYSSAL_TENTACLE("Abyssal tentacle", "Charges", 10000),
    TOXIC_BLOWPIPE("Toxic blowpipe", "Scales", 16383);

    companion object {
        fun fromItemName(name: String): ChargedItem? {
            return values().find { it.itemName.equals(name, ignoreCase = true) }
        }
    }
}
