package com.runemate.zombiepirates

import com.runemate.game.api.hybrid.location.Area
import com.runemate.game.api.hybrid.location.Coordinate
import java.util.regex.Pattern

object BotConfig {
    val food = "Blighted manta ray"
    val foodHeal = 20
    val minFoodBeforeBank = 3
    val restorePotion: Pattern = Pattern.compile("Blighted super restore.*\\(.*")
    val minRestoreBeforeBank = 1
    val CHAOS_TEMPLE_AREA: Area = Area.Polygonal(
        Coordinate(3232, 3631, 0),
        Coordinate(3240, 3631, 0),
        Coordinate(3256, 3614, 0),
        Coordinate(3257, 3607, 0),
        Coordinate(3252, 3603, 0),
        Coordinate(3250, 3591, 0),
        Coordinate(3232, 3591, 0),
        Coordinate(3220, 3609, 0),
        Coordinate(3219, 3617, 0)
    )
    val CHAOS_TEMPLE: Area = Area.Rectangular(Coordinate(3257, 3588, 0), Coordinate(3215, 3634, 0))
    val FEROX_ENCLAVE: Area = Area.Rectangular(Coordinate(3124, 3639, 0), Coordinate(3154, 3622, 0))
    val SAFE_ZONE: Coordinate = Coordinate(3238, 3519, 0)
}