package com.runemate.common.item

data class Loot(
    val gameName: String,
    val id: Int,
    val quantities: List<Int>,
    val noted: Boolean = false
) {
    fun minQuantity() = quantities.firstOrNull() ?: 0
    fun maxQuantity() = quantities.lastOrNull() ?: minQuantity()

    companion object {
        val lootList: List<Loot> = listOf(
            Loot("Teleport anchoring scroll", 29455, listOf(1)),
            Loot("Larran's key", 23490, listOf(1)),
            Loot("Zombie pirate key", 29449, listOf(1)),
            Loot("Dragon scimitar", 4587, listOf(1)),
            Loot("Dragon longsword", 1305, listOf(1)),
            Loot("Dragon dagger", 1215, listOf(1)),
            Loot("Rune warhammer", 1347, listOf(1)),
            Loot("Rune battleaxe", 1373, listOf(1)),
            Loot("Rune longsword", 1303, listOf(1)),
            Loot("Rune sword", 1289, listOf(1)),
            Loot("Rune mace", 1432, listOf(1)),
            Loot("Rune med helm", 1147, listOf(1)),
            Loot("Adamant platebody", 1123, listOf(1)),
            Loot("Cannonball", 2, listOf(20, 60)),
            Loot("Blighted ancient ice sack", 24607, listOf(10, 30)),
            Loot("Battlestaff", 1391, listOf(1)),
            Loot("Blighted super restore(4)", 24599, listOf(1, 3), noted = true),
            Loot("Blighted karambwan", 3145, listOf(5, 15), noted = true),
            Loot("Blighted anglerfish", 24593, listOf(5, 15), noted = true),
            Loot("Blighted manta ray", 24590, listOf(5, 15), noted = true),
            Loot("Gold ore", 445, listOf(5, 15), noted = true),
            Loot("Blood rune", 565, listOf(30, 60)),
            Loot("Death rune", 560, listOf(30, 90)),
            Loot("Chaos rune", 562, listOf(30, 90)),
            Loot("Mind rune", 558, listOf(30, 90)),
            Loot("Adamant seeds", 29458, listOf(5, 10, 15)),
            Loot("Coins", 995, listOf(250, 8000))
        )

        val ZOMBIE_PIRATES: Set<Loot> = lootList.toSet()
        val gameNames: List<String> = lootList.map { it.gameName.lowercase() }
    }
}