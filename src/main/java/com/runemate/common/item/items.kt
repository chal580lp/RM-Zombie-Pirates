package com.runemate.common.item

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.runemate.common.util
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem
import com.runemate.game.api.hybrid.util.Regex
import java.util.regex.Pattern

open class BaseItem(
    open val name: String,
    open val id: Int
)

data class SetItem @JsonCreator constructor(
    @JsonProperty("name") override val name: String,
    @JsonProperty("id") override val id: Int,
    @JsonProperty("quantity") var quantity: Int = 1,
    @JsonProperty("required") val required: Boolean = true
) : BaseItem(name, id) {
    val isNumbered: Boolean by lazy {
        util.isNumbered(name)
    }

    val price: Int by lazy {
        util.getPrice(id, name)
    }

    val isRestore: Boolean by lazy {
        Restore.isRestore(name)
    }

    val isBoost: Boolean by lazy {
        Boost.isBoost(name)
    }

    val isFood: Boolean by lazy {
        Food.isFood(name)
    }

    val numberPattern: Pattern by lazy {
        util.createPatternFromItemName(name)
    }

    val bankPattern: Pattern by lazy {
        if (isNumbered) {
            numberPattern
        } else {
            Pattern.compile(name)
        }
    }

    val invPattern: Pattern by lazy {
        if (isNumbered && !isRestore && !isBoost) {
            numberPattern
        } else {
            Regex.getPatternForExactString(name)
            //Pattern.compile(name)
        }
    }

    val purchaseString: String by lazy {
        name
    }

    override fun equals(other: Any?): Boolean = other is SetItem && id == other.id
    override fun hashCode(): Int = id.hashCode()
    fun contains(item: BaseItem): Boolean = id == item.id
}

data class EquipmentSetItem @JsonCreator constructor(
    @JsonProperty("setItem") val setItem: SetItem,
    @JsonProperty("slot") val slot: EquipmentSlot,
    @JsonProperty("requirements") val requirements: String
) {
    val name: String get() = setItem.name
    val id: Int get() = setItem.id
    var quantity: Int
        get() = setItem.quantity
        set(value) { setItem.quantity = value }
    val required: Boolean get() = setItem.required
}




object items {
    const val braceletOfSlaughter = "Bracelet of slaughter"
    const val conCapeTelePoh = "Tele to POH"
    const val crystalChime = "Crystal chime"
    const val desertAmulet4 = "Desert amulet 4"
    const val dwarvenRock = "Dwarven rock cake"
    const val expeditiousBracelet = "Expeditious bracelet"
    const val fremBoots4 = "Fremennik sea boots 4"
    const val grandSeedPod = "Grand seed pod"
    const val keyMasterTeleport = "Key master teleport"
    const val locatorOrb = "Locator orb"
    const val pharaohSceptre = "Pharaoh’s Sceptre"
    const val rope = "Rope"
    const val royalSeedPod = "Royal seed pod"
    const val spade = "Spade"
    const val wildPie = "Wild pie"
    const val wildPieHalf = "Half a wild pie"
    const val wildySword4 = "Wilderness sword 4"

    val amuletOfGlory = Pattern.compile("Amulet of glory.*\\(.*")
    val antiPoisonPPattern: List<Pattern> = listOf(
        Pattern.compile("Superantipoison.*"),
        Pattern.compile("Antipoison.*")
    )
    val argdougneCloak = Pattern.compile("Ardougne cloak.*")
    val ashSanctifier = Pattern.compile("^Ash sanctifier$")
    val bookOfDead = Pattern.compile("^Book of the dead|Kharedst's memoirs$")
    val bonecrusher = Pattern.compile("^Bonecrusher|Bonecrusher necklace$")
    val burningAmulet = Pattern.compile("Burning amulet.*\\(.*\\)")
    val craftingCape = Pattern.compile("Crafting cape.*")
    val constructionCape = Pattern.compile("Construct\\. cape.*")
    val drakans = Pattern.compile("^Drakan's medallion$")
    val dramenStaff = Pattern.compile("(?:(?:Dramen)|(?:Lunar)) staff")
    val enchantedLyre = Pattern.compile("Enchanted lyre\\(i\\)")
    val fishingCape = Pattern.compile("Fishing cape.*")
    val gamesNecklace = Pattern.compile("^Games necklace.*$")
    val gemBag = Pattern.compile(".*gem bag", Pattern.CASE_INSENSITIVE)
    val herbSack = Pattern.compile(".*herb sack", Pattern.CASE_INSENSITIVE)
    val houseTab = Pattern.compile("^Teleport to house$")
    val iceCooler = Pattern.compile("^Ice cooler$")
    val imbuedHeart = Pattern.compile("(?:(?:Imbued)|(?:Saturated)) heart")
    val questCape = Pattern.compile("Quest point cape.*")
    val radasBlessing = Pattern.compile("Rada.*")
    val radasBlessing4 = Pattern.compile("^Rada's blessing 4$")
    val ringOfDueling = Pattern.compile("Ring of dueling.*\\(.*\\)")
    val ringOfTheElements = Pattern.compile("Ring of the elements.*")
    val ringOfWealth = Pattern.compile("Ring of wealth.*\\(.*")
    val runePouch = Pattern.compile("(?:Divine rune|Rune) pouch.*")
    val skullSceptre = Pattern.compile("Skull sceptre.*", Pattern.CASE_INSENSITIVE)
    val slayerCape = Pattern.compile("Slayer cape.*")
    val slayerRing = Pattern.compile("Slayer ring.*")
    val soulBearer = Pattern.compile("Soul bearer")
    val spadeP = Pattern.compile("Spade")
    val stamina4Dose = Pattern.compile("Stamina potion\\(4\\)")
    val staminaPotions = Pattern.compile("Stamina potion.*")
    val xericsTalisman = Pattern.compile("^Xeric's talisman$", Pattern.CASE_INSENSITIVE)
}
