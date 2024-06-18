package com.runemate.common.item

sealed class Weapon {
    sealed class ChargedItem(val chargeType: String) {
        abstract val itemName: String
        abstract val itemId: Int
        abstract val maxCharges: Int
        abstract val degradedName: String

        sealed class Ether : ChargedItem("Revenant ether") {
            object CrawsBow : Ether() {
                override val itemName: String = "Craw's bow"
                override val itemId: Int = 22550
                override val maxCharges: Int = 16000
                override val degradedName: String = "Craw's bow (u)"
            }
            object WebWeaverBow : Ether() {
                override val itemName: String = "Web Weaver bow"
                override val itemId: Int = 30164
                override val maxCharges: Int = 16000
                override val degradedName: String = "Web Weaver bow (u)"
            }
            object UrsineChainmace : Ether() {
                override val itemName: String = "Ursine chainmace"
                override val itemId: Int = 30198
                override val maxCharges: Int = 16000
                override val degradedName: String = "Ursine chainmace (u)"
            }
            object ViggorasChainmace : Ether() {
                override val itemName: String = "Viggora's chainmace"
                override val itemId: Int = 30171
                override val maxCharges: Int = 16000
                override val degradedName: String = "Viggora's chainmace (u)"
            }
            object ThammaronsSceptre : Ether() {
                override val itemName: String = "Thammaron's sceptre"
                override val itemId: Int = 30207
                override val maxCharges: Int = 16000
                override val degradedName: String = "Thammaron's sceptre (u)"
            }
            object AccursedSceptre : Ether() {
                override val itemName: String = "Accursed sceptre"
                override val itemId: Int = 30220
                override val maxCharges: Int = 16000
                override val degradedName: String = "Accursed sceptre (u)"
            }
        }

        sealed class Charges : ChargedItem("Charges") {
            object AbyssalTentacle : Charges() {
                override val itemName: String = "Abyssal tentacle"
                override val itemId: Int = 12006
                override val maxCharges: Int = 10000
                override val degradedName: String = "Abyssal whip"
            }
            object CrystalBow : Charges() {
                override val itemName: String = "Crystal bow"
                override val itemId: Int = 4212
                override val maxCharges: Int = 2500
                override val degradedName: String = "Crystal seed"
            }
            object ZaryteBow : Charges() {
                override val itemName: String = "Zaryte bow"
                override val itemId: Int = 32040
                override val maxCharges: Int = 10000
                override val degradedName: String = "Zaryte bow (uncharged)"
            }
        }

        sealed class BloodRunes : ChargedItem("Blood rune") {
            object SanguinestiStaff : BloodRunes() {
                override val itemName: String = "Sanguinesti staff"
                override val itemId: Int = 22481
                override val maxCharges: Int = 20000
                override val degradedName: String = "Sanguinesti staff (uncharged)"
            }
        }

        sealed class Scales : ChargedItem("Zulrah's scales") {
            object ToxicBlowpipe : Scales() {
                override val itemName: String = "Toxic blowpipe"
                override val itemId: Int = 12924
                override val maxCharges: Int = 16383
                override val degradedName: String = "Toxic blowpipe (empty)"
            }
        }

        sealed class Runes : ChargedItem("Runes") {
            object TridentOfTheSeas : Runes() {
                override val itemName: String = "Trident of the seas"
                override val itemId: Int = 11905
                override val maxCharges: Int = 2500
                override val degradedName: String = "Uncharged trident"
            }
            object TridentOfTheSwamp : Runes() {
                override val itemName: String = "Trident of the swamp"
                override val itemId: Int = 12899
                override val maxCharges: Int = 2500
                override val degradedName: String = "Uncharged toxic trident"
            }
        }
    }

    companion object {
        fun fromItemId(itemId: Int): ChargedItem? {
            return listOf(
                ChargedItem.Ether.CrawsBow,
                ChargedItem.Ether.UrsineChainmace,
                ChargedItem.Ether.ViggorasChainmace,
                ChargedItem.Charges.AbyssalTentacle,
                ChargedItem.Charges.CrystalBow,
                ChargedItem.Charges.ZaryteBow,
                ChargedItem.BloodRunes.SanguinestiStaff,
                ChargedItem.Scales.ToxicBlowpipe,
                ChargedItem.Runes.TridentOfTheSeas,
                ChargedItem.Runes.TridentOfTheSwamp
            ).find { it.itemId == itemId }
        }

        fun fromItemName(name: String): ChargedItem? {
            return listOf(
                ChargedItem.Ether.CrawsBow,
                ChargedItem.Ether.UrsineChainmace,
                ChargedItem.Ether.ViggorasChainmace,
                ChargedItem.Charges.AbyssalTentacle,
                ChargedItem.Charges.CrystalBow,
                ChargedItem.Charges.ZaryteBow,
                ChargedItem.BloodRunes.SanguinestiStaff,
                ChargedItem.Scales.ToxicBlowpipe,
                ChargedItem.Runes.TridentOfTheSeas,
                ChargedItem.Runes.TridentOfTheSwamp
            ).find { it.itemName.equals(name, ignoreCase = true) }
        }
    }
}
