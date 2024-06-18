package com.runemate.common.fx

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.runemate.game.api.hybrid.Environment
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import java.io.File
import javax.imageio.ImageIO

object FileManager {
    private const val LOOT_ITEMS_FILE = "loot_ids.json"
    private const val IMAGES_DIRECTORY = "/images/items"

    fun saveLootItemIds(lootItemIds: List<Int>) {
        val mapper = jacksonObjectMapper()
        val json = mapper.writeValueAsString(lootItemIds)
        val file = Environment.getStorageDirectory().resolve(LOOT_ITEMS_FILE)
        file.writeText(json)
    }

    fun loadLootItemIds(): List<Int> {
        println(Environment.getStorageDirectory().toString())
        val file = Environment.getStorageDirectory().resolve(LOOT_ITEMS_FILE)
        return if (file.exists()) {
            val mapper = jacksonObjectMapper()
            val json = file.readText()
            mapper.readValue(json)
        } else {
            emptyList()
        }
    }
    fun getImageForItem(itemId: Int): Image {
        if (itemId == 995) return getImageForItem(1003)
        val basedir = Environment.getSharedStorageDirectory().toString()
        val imageFile = File("$basedir$IMAGES_DIRECTORY/$itemId.png")
        return if (imageFile.exists()) {
            Image(imageFile.toURI().toString())
        } else {
            println("Downloading image for item $itemId")
            val item = SpriteItem(itemId, 0)
            val image = item.image.get() ?: return getImageForItem(1003)
            val fxImage = SwingFXUtils.toFXImage(image, null)
            saveImage(fxImage, imageFile)
            fxImage
        }
    }

    private fun saveImage(image: Image, file: File) {
        val bufferedImage = SwingFXUtils.fromFXImage(image, null)
        ImageIO.write(bufferedImage, "png", file)
    }

    fun preloadImages(itemIds: Collection<Int>) {
        itemIds.forEach { itemId ->
            getImageForItem(itemId)
        }
    }
}