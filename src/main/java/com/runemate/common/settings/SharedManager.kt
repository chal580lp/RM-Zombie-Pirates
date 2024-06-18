package com.runemate.common.settings

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.runemate.game.api.hybrid.Environment
import com.runemate.game.api.script.framework.AbstractBot
import java.io.File
import java.time.LocalDateTime

data class ItemPrice(
    @JsonProperty("id") val id: Int,
    @JsonProperty("price") var price: Int,
    @JsonProperty("timestamp") val timestamp: LocalDateTime
)

class SharedManager(bot: AbstractBot) {

    private val objectMapper = ObjectMapper()
    private val filePath = Environment.getStorageDirectory(bot).path + "/shared_data.json"
    private val itemPrices = mutableListOf<ItemPrice>()
    // Add other shared data properties here

    fun addItemPrice(itemId: Int, price: Int) {
        itemPrices.add(ItemPrice(itemId, price, LocalDateTime.now()))
    }

    fun getLatestPrices(): Map<Int, Int> {
        return itemPrices
            .groupBy { it.id }
            .mapValues { (_, prices) -> prices.maxByOrNull { it.timestamp }?.price ?: 0 }
    }

    fun saveToFile() {
        val file = File(filePath)
        val sharedData = SharedData(itemPrices)
        objectMapper.writeValue(file, sharedData)
    }

    fun loadFromFile() {
        val file = File(filePath)
        if (file.exists()) {
            val sharedData = objectMapper.readValue(file, SharedData::class.java)
            itemPrices.clear()
            itemPrices.addAll(sharedData.itemPrices)
        }
    }

    // Add other shared data methods here
}

data class SharedData(
    @JsonProperty("itemPrices") val itemPrices: List<ItemPrice>
    // Add other shared data properties here
)