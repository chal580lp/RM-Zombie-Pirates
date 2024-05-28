package com.runemate.common

import com.runemate.game.api.hybrid.input.direct.MenuAction
import com.runemate.game.api.hybrid.input.direct.MenuOpcode
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceComponent
import com.runemate.game.api.hybrid.local.hud.interfaces.Interfaces
import com.runemate.game.api.osrs.local.hud.interfaces.Prayer
import com.runemate.game.api.script.Execution
import com.runemate.game.api.script.framework.listeners.EngineListener
import java.util.Arrays
import java.util.regex.Pattern

class PrayerFlicker : EngineListener {
    private val log: RMLogger = RMLogger.getLogger(this::class.java)
    companion object {
        private val TOGGLE_ACTION = Pattern.compile("^(Dea|A)ctivate$")
    }

    private var activePrayers: Array<Prayer> = arrayOf()
    private var quickPray = false

    private fun getQuickPrayerToggle(): InterfaceComponent? {
        return Interfaces.newQuery().containers(160).types(InterfaceComponent.Type.CONTAINER)
            .actions(TOGGLE_ACTION).results().first()
    }

    private fun directInputQuickPrayer(toggle: InterfaceComponent) {
        DI.send(MenuAction.forInterfaceComponent(toggle, 0, MenuOpcode.CC_OP))
    }

    fun getActivePrayers(): Array<Prayer> {
        return activePrayers
    }

    fun setActivePrayers(vararg _activePrayers: Prayer) {
        if (!Arrays.equals(_activePrayers, activePrayers)) {
            log.debug("Setting active prayers ${Arrays.toString(_activePrayers)}")
        }
        activePrayers = _activePrayers as Array<Prayer>
        quickPray = false
    }

    fun disable() {
        this.activePrayers = arrayOf()
    }

    fun setQuickPrayers(vararg _activePrayers: Prayer) {
        if (!Arrays.equals(_activePrayers, activePrayers)) {
            log.debug("Setting quick prayers ${Arrays.toString(_activePrayers)}")
        }
        Prayer.setQuickPrayers(*_activePrayers)
        activePrayers = _activePrayers as Array<Prayer>
        quickPray = true
    }

    override fun onTickStart() {
        try {
            if (quickPray) {
                val interfaceT = getQuickPrayerToggle()
                if (interfaceT == null) {
                    log.error("No quick prayer toggle")
                } else {
                    if (Prayer.getActivePrayers().isNotEmpty())
                        directInputQuickPrayer(interfaceT)
                    Execution.delay(10, 16)
                    if (getActivePrayers().isNotEmpty())
                        directInputQuickPrayer(interfaceT)
                    return
                }
            }
            // turn off
            for (p in Prayer.getActivePrayers()) {
                DI.send(MenuAction.forInterfaceComponent(p.component, 0, MenuOpcode.CC_OP))
            }
            // flick
            for (p in getActivePrayers()) {
                DI.send(MenuAction.forInterfaceComponent(p.component, 0, MenuOpcode.CC_OP))
            }
        } catch (e: Exception) {
            log.error("Prayer flicker " + e)
        }
    }

    fun getActiveProtectionPrayer(): Prayer? {
        for (p in this.activePrayers) {
            if (p == Prayer.PROTECT_FROM_MELEE ||
                p == Prayer.PROTECT_FROM_MAGIC ||
                p == Prayer.PROTECT_FROM_MISSILES) {
                return p
            }
        }
        return null
    }
}