package com.runemate.common

import com.runemate.game.api.hybrid.Environment
import com.runemate.game.api.hybrid.input.direct.DirectInput
import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceComponent
import com.runemate.game.api.script.framework.listeners.EngineListener
import java.util.*

object DI : EventListener, EngineListener {
    const val LIMIT: Int = 8
    val log : RMLogger = RMLogger.getLogger(this::class.java)

    private var actionsThisTick = 0
    private var tick = 0

    fun send(action: MenuAction?): Boolean {
        if (action == null) return false
        if (diCheckIsFailed(action)) return false
        if (actionsThisTick >= LIMIT) return false
        actionsThisTick++
        DirectInput.send(action)
        return true
    }

    private fun diCheckIsFailed(menuAction: MenuAction): Boolean {
        val checkFailed = false
        if (menuAction.entity !is InterfaceComponent) log.debug("DI${menuAction}")
        return checkFailed
    }

    override fun onTickStart() {
        tick++
        actionsThisTick = 0
    }
}