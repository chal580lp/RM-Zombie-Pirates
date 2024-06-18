package com.runemate.common.traverse

import com.runemate.game.api.hybrid.location.Area
import com.runemate.game.api.hybrid.location.Coordinate

object areas {
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
    val FEROX_ENCLAVE: Area = Area.Rectangular(Coordinate(3124, 3639, 0), Coordinate(3154, 3622, 0))
    val SAFE_ZONE: Coordinate = Coordinate(3238, 3519, 0)
}