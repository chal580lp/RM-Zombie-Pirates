package com.runemate.common.framework.taskstate.task


import com.runemate.common.BankManager
import com.runemate.common.item.items
import com.runemate.common.framework.core.Task
import com.runemate.common.traverse.RingOfWealthTraverse
import com.runemate.common.util
import com.runemate.game.api.hybrid.entities.Npc
import com.runemate.game.api.hybrid.local.Camera
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank
import com.runemate.game.api.hybrid.location.navigation.Landmark
import com.runemate.game.api.hybrid.region.Npcs
import com.runemate.game.api.hybrid.web.WebPath
import com.runemate.game.api.hybrid.web.WebPathRequest
import com.runemate.game.api.script.Execution
import com.runemate.common.LoggerUtils.getLogger

class TraverseToGrandExchange : Task {
    private val pathCache: MutableMap<String, WebPath> = HashMap()
    private val log = getLogger("TraverseToGrandExchange")
    private var checkForWealth = true
    private var exchangeCleric: Npc? = null

    override fun validate(): Boolean {
        if (exchangeCleric == null) {
            exchangeCleric = Npcs.newQuery().names("Grand Exchange Clerk").results().nearest()
        }
        return (exchangeCleric == null || exchangeCleric?.isVisible == false)
    }

    override fun execute() {
        if (exchangeCleric != null) {
            log.debug("Walking to Grand Exchange Clerk")
            Camera.turnTo(exchangeCleric)
            util.walkToNearbyGE()
            return
        }
        if (util.isItemInInventoryOrEquipment(items.ringOfWealth)) {
            log.debug("Using ring of wealth to teleport to Grand Exchange")
            RingOfWealthTraverse.traverse(RingOfWealthTraverse.Destination.GrandSexchange)
            return
        }
        if (checkForWealth && Bank.open()) {
            log.debug("Checking for ring of wealth")

            if (!Bank.contains(items.ringOfWealth)) {
                checkForWealth = false
            } else {
                if (Bank.withdraw(items.ringOfWealth, 1)) {
                    Execution.delayUntil({ util.isItemInInventoryOrEquipment(items.ringOfWealth) }, 2000)
                }
            }
            return
        }
        if (checkForWealth && util.walkToNearbyBank()) {
            log.debug("Walking to bank to check for ring of wealth")
            return
        }
        log.debug("Walking to Grand Exchange")
        val path = getPathLandmark(Landmark.GRAND_EXCHANGE_CLERK, true)
        path?.step()
    }

    private fun getPathLandmark(landmark: Landmark, usingTeleports: Boolean): WebPath? {
        val key = landmark.toString() + usingTeleports
        var cachedPath: WebPath? = pathCache[key]

        if (cachedPath == null) {
            log.debug("Generating new cachedPath to $landmark")
            cachedPath = WebPathRequest.builder()
                .setLandmark(landmark)
                .setUsingTeleports(usingTeleports)
                .build() ?: run {
                log.debug("Failed to build path to $landmark")
                return null
            }
            pathCache[key] = cachedPath
        }
        return cachedPath
    }
}