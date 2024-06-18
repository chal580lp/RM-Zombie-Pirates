package com.runemate.common.item

import com.runemate.game.api.hybrid.local.Quest

enum class EquipmentSlot {
    HEAD, BODY, LEGS, HANDS, FEET, SHIELD, CAPE, NECK, RING, AMMO, WEAPON
}

enum class Skill {
    ATTACK, STRENGTH, DEFENCE, RANGED, PRAYER, MAGIC, HITPOINTS, AGILITY, HERBLORE, THIEVING, CRAFTING, FLETCHING, SLAYER, MINING, SMITHING, FISHING, COOKING, FIREMAKING, WOODCUTTING, FARMING, RUNECRAFTING, HUNTER, CONSTRUCTION
}

sealed class Armor(
    open val gameName: String,
    open val id: Int,
    open val equipmentSlot: EquipmentSlot,
    open val requiredStats: Map<Skill, Int>,
    open val tradeable: Boolean = true,
    open val quest: Quest? = null
) {
    // Non-degradable items
    data class NonDegradable(
        override val gameName: String,
        override val id: Int,
        override val equipmentSlot: EquipmentSlot,
        override val requiredStats: Map<Skill, Int>,
        override val tradeable: Boolean = true,
        override val quest: Quest? = null
    ) : Armor(gameName, id, equipmentSlot, requiredStats, tradeable, quest)

    // Degradable items
    data class Degradable(
        override val gameName: String,
        override val id: Int,
        override val equipmentSlot: EquipmentSlot,
        override val requiredStats: Map<Skill, Int>,
        override val tradeable: Boolean = true,
        override val quest: Quest? = null
    ) : Armor(gameName, id, equipmentSlot, requiredStats, tradeable, quest)

    companion object {
        fun getEquipmentBySlot(armors: List<Armor>): Map<EquipmentSlot, List<Armor>> {
            return armors.groupBy { it.equipmentSlot }
        }
    }
}

val allArmors = listOf(
    // Non-degradable items
    Armor.NonDegradable("Proselyte Helm", 9672, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 30, Skill.PRAYER to 20), quest = Quest.OSRS.THE_SLUG_MENACE),
    Armor.NonDegradable("Neitiznot Faceguard", 10828, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 70), quest = Quest.OSRS.THE_FREMENNIK_EXILES),
    Armor.NonDegradable("Slayer Helm", 11864, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 10)),
    Armor.NonDegradable("Proselyte Armor", 9678, EquipmentSlot.BODY, mapOf(Skill.DEFENCE to 30, Skill.PRAYER to 20), quest = Quest.OSRS.THE_SLUG_MENACE),
    Armor.NonDegradable("Fighter Torso", 10551, EquipmentSlot.BODY, mapOf(Skill.DEFENCE to 40), tradeable = false),
    Armor.NonDegradable("Bandos Chestplate", 11832, EquipmentSlot.BODY, mapOf(Skill.DEFENCE to 65)),
    Armor.NonDegradable("Proselyte Legs", 9674, EquipmentSlot.LEGS, mapOf(Skill.DEFENCE to 30, Skill.PRAYER to 20), quest = Quest.OSRS.THE_SLUG_MENACE),
    Armor.NonDegradable("Bandos Tassets", 11834, EquipmentSlot.LEGS, mapOf(Skill.DEFENCE to 65)),
    Armor.NonDegradable("Dagon'hai Robes Bottom", 24210, EquipmentSlot.LEGS, mapOf(Skill.DEFENCE to 70, Skill.MAGIC to 70)),
    Armor.NonDegradable("Barrows Gloves", 7462, EquipmentSlot.HANDS, mapOf(Skill.DEFENCE to 40), tradeable = false, quest = Quest.OSRS.RECIPE_FOR_DISASTER),
    Armor.NonDegradable("Ferocious Gloves", 22981, EquipmentSlot.HANDS, mapOf(Skill.DEFENCE to 80)),
    Armor.NonDegradable("Dragon Boots", 11840, EquipmentSlot.FEET, mapOf(Skill.DEFENCE to 60)),
    Armor.NonDegradable("Primordial Boots", 13239, EquipmentSlot.FEET, mapOf(Skill.STRENGTH to 75, Skill.DEFENCE to 75)),
    Armor.NonDegradable("Dragon Defender", 12954, EquipmentSlot.SHIELD, mapOf(Skill.ATTACK to 65, Skill.STRENGTH to 65)),
    Armor.NonDegradable("Avernic Defender", 22322, EquipmentSlot.SHIELD, mapOf(Skill.ATTACK to 70, Skill.STRENGTH to 70)),
    Armor.NonDegradable("Arcane Spirit Shield", 12825, EquipmentSlot.SHIELD, mapOf(Skill.PRAYER to 70, Skill.DEFENCE to 75)),
    Armor.NonDegradable("Fire Cape", 6570, EquipmentSlot.CAPE, emptyMap()),
    Armor.NonDegradable("Ava's Accumulator", 10499, EquipmentSlot.CAPE, mapOf(Skill.RANGED to 50), tradeable = false, quest = Quest.OSRS.ANIMAL_MAGNETISM),
    Armor.NonDegradable("Infernal Cape", 21295, EquipmentSlot.CAPE, emptyMap(), tradeable = false),
    Armor.NonDegradable("Amulet of Fury", 6585, EquipmentSlot.NECK, mapOf(Skill.DEFENCE to 70)),
    Armor.NonDegradable("Occult Necklace", 12002, EquipmentSlot.NECK, mapOf(Skill.MAGIC to 70)),
    Armor.NonDegradable("Berserker Ring", 6737, EquipmentSlot.RING, mapOf(Skill.DEFENCE to 70)),
    Armor.NonDegradable("Archers' Ring", 6733, EquipmentSlot.RING, mapOf(Skill.DEFENCE to 70)),
    Armor.NonDegradable("Amethyst Arrows", 21326, EquipmentSlot.AMMO, mapOf(Skill.RANGED to 75)),
    Armor.NonDegradable("Rune Arrows", 892, EquipmentSlot.AMMO, mapOf(Skill.RANGED to 40)),
    Armor.NonDegradable("Bonecrusher", 13116, EquipmentSlot.NECK, mapOf(Skill.PRAYER to 40), tradeable = false),
    Armor.NonDegradable("Slayer Helm (i)", 11864, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 10, Skill.SLAYER to 55)),
    Armor.NonDegradable("Obsidian Helm", 21298, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 60)),
    Armor.NonDegradable("Void Melee Helm", 11665, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 42), tradeable = false),
    Armor.NonDegradable("Elder Chaos Robes", 20595, EquipmentSlot.BODY, mapOf(Skill.DEFENCE to 40)),

    // Degradable items
    Armor.Degradable("Torag's Helm", 4745, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Verac's Helm", 4753, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Dharok's Helm", 4716, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Guthan's Helm", 4724, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Karil's Coif", 4732, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Ahrim's Hood", 4708, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 70, Skill.MAGIC to 70)),
    Armor.Degradable("Serpentine Helm", 12931, EquipmentSlot.HEAD, mapOf(Skill.DEFENCE to 75)),
    Armor.Degradable("Dharok's Platebody", 4720, EquipmentSlot.BODY, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Torag's Platebody", 4749, EquipmentSlot.BODY, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Guthan's Platebody", 4738, EquipmentSlot.BODY, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Verac's Brassard", 4976, EquipmentSlot.BODY, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Karil's Leathertop", 4736, EquipmentSlot.BODY, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Ahrim's Robe Top", 4708, EquipmentSlot.BODY, mapOf(Skill.DEFENCE to 70, Skill.MAGIC to 70)),
    Armor.Degradable("Dharok's Platelegs", 4722, EquipmentSlot.LEGS, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Torag's Platelegs", 4751, EquipmentSlot.LEGS, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Guthan's Chainskirt", 4730, EquipmentSlot.LEGS, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Verac's Plateskirt", 4759, EquipmentSlot.LEGS, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Karil's Skirt", 4738, EquipmentSlot.LEGS, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Dagon'hai Robes Bottom", 24210, EquipmentSlot.LEGS, mapOf(Skill.DEFENCE to 70, Skill.MAGIC to 70)),
    Armor.Degradable("Barrows Gloves", 7462, EquipmentSlot.HANDS, mapOf(Skill.DEFENCE to 40), tradeable = false, quest = Quest.OSRS.RECIPE_FOR_DISASTER),
    Armor.Degradable("Infernal Boots", 13239, EquipmentSlot.FEET, mapOf(Skill.DEFENCE to 75)),
    Armor.Degradable("Ranger Boots", 2577, EquipmentSlot.FEET, mapOf(Skill.DEFENCE to 40, Skill.RANGED to 40)),
    Armor.Degradable("Crystal Shield", 4225, EquipmentSlot.SHIELD, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Infernal Cape", 21295, EquipmentSlot.CAPE, emptyMap(), tradeable = false),
    Armor.Degradable("Amulet of the Damned", 12851, EquipmentSlot.NECK, mapOf(Skill.DEFENCE to 70)),
    Armor.Degradable("Ring of Recoil", 2550, EquipmentSlot.RING, emptyMap()),
    Armor.Degradable("Ring of Wealth", 2572, EquipmentSlot.RING, mapOf(Skill.DEFENCE to 50))
)

val equipmentBySlot = Armor.getEquipmentBySlot(allArmors)