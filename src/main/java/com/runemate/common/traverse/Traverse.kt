package com.runemate.common.traverse


import com.runemate.common.LoggerUtils.getLogger
import com.runemate.game.api.hybrid.entities.details.Locatable
import com.runemate.game.api.hybrid.web.WebPath
import com.runemate.game.api.hybrid.web.WebPathRequest

object Traverse {

    private val pathCache: HashMap<String, WebPath> = HashMap()
    private val log = getLogger("Traverse")

    fun getPathDestination(
        destination: Locatable,
        usingTeleports: Boolean
    ): WebPath? {
        // Create a unique key for the destination and the usingTeleports flag
        val key: String = "${destination.hashCode()}-$usingTeleports"

        // Try to get the cached path
        var cachedPath: WebPath? = pathCache[key]

        // If there is no cached path, build a new one and cache it
        if (cachedPath == null) {
            log.debug("Building new path to {}", destination)
            cachedPath = WebPathRequest.builder()
                .setDestination(destination)
                .setUsingTeleports(usingTeleports)
                .build() ?: run {
                log.debug("Failed to build path to {}", destination)
                    return null
                }
            // Cache the new path
            pathCache[key] = cachedPath
        }

        // Return the cached or newly built path
        return cachedPath
    }
}