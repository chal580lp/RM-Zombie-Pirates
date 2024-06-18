package com.runemate.common.settings

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.runemate.common.LoggerUtils
import com.runemate.common.framework.core.bot.BotConfig

import com.runemate.common.item.SetItem
import com.runemate.common.framework.core.TaskMachine
import com.runemate.game.api.hybrid.Environment
import java.io.File

data class InventoryItem(
    @JsonProperty("id") val id: Int,
    @JsonProperty("quantity") var quantity: Int,
    @JsonProperty("required") val required: Boolean = false
)

data class AccountSettings(
    @JsonProperty("inventoryItems") var inventoryItems: MutableList<SetItem>,
    @JsonProperty("lastUsedSetting") var lastUsedSetting: String,
    @JsonProperty("minLootValue") var minLootValue: Int
)

class AccountManager<TSettings : BotConfig>(val bot: TaskMachine<TSettings>) {
    private val log = LoggerUtils.getLogger("AccountManager")
    private val objectMapper = ObjectMapper()
    private val filePath = "${Environment.getStorageDirectory(bot)}/${Environment.getAccountAlias(bot)}_settings.json"

    // Lazy initialization for accountSettings
     val accountSettings: AccountSettings by lazy {
        loadAccountSettings()
    }

    init {
        // Ensure settings are loaded when AccountManager is initialized
        accountSettings
    }
    fun invokeOnManagerLoaded() {
        kotlin.runCatching {
            bot.onManagerLoaded()
        }.onFailure { e ->
            log.error("Error loading manager: ${e.message}")
        }
    }

    private fun loadAccountSettings(): AccountSettings {
        val file = File(filePath)
        return if (file.exists()) {
            val rawSettings = objectMapper.readValue(file, Map::class.java)

            val inventoryItems = rawSettings["inventoryItems"]?.let { inventoryItemsData ->
                (inventoryItemsData as? List<Map<String, Any>>)?.mapNotNull { itemMap ->
                    runCatching {
                        SetItem(
                            name = itemMap["name"] as? String ?: "",
                            id = itemMap["id"] as Int,
                            quantity = itemMap["quantity"] as? Int ?: 1
                        )
                    }.getOrNull()
                }?.toMutableList()
            } ?: mutableListOf()

            val lastUsedSetting = rawSettings["lastUsedSetting"] as? String ?: ""
            val minLootValue = rawSettings["minLootValue"] as? Int ?: bot.settings().minLootValue

            AccountSettings(
                inventoryItems = inventoryItems,
                lastUsedSetting = lastUsedSetting,
                minLootValue = minLootValue
            ).also {
                log.debug("Settings file found, loaded settings: $it")
            }
        } else {
            AccountSettings(mutableListOf(), "", bot.settings().minLootValue).also {
                log.debug("No settings file found at : $filePath")
            }
        }
    }

    fun saveAccountSettings() {
        kotlin.runCatching {
            updateAccountSettings()
            val file = File(filePath)
            objectMapper.writeValue(file, accountSettings)
        }.onSuccess { log.debug("Saved account settings at $filePath" ) }.onFailure { e ->
            log.error("Failed to save account settings: ${e.message}")
        }
    }

    private fun updateAccountSettings() {
        accountSettings.inventoryItems = bot.getInvManager().inventory.map { it }.toMutableList()
        accountSettings.minLootValue = bot.settings().minLootValue
    }

    fun setLastUsedSetting(setting: String) {
        accountSettings.lastUsedSetting = setting
    }
}