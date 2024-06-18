package com.runemate.common.framework.core.addons

import com.runemate.common.framework.core.BaseState


sealed class BotState : BaseState() {
    data object StartState : BotState()
    data object BankState : BotState()
    data object CombatState : BotState()
    data object RetreatState : BotState()
    data object AdvanceState : BotState()
    data object AntiPkState : BotState()
    data object GEState : BotState()
    data object HopState : BotState()
}