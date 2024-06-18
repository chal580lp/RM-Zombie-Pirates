package com.runemate.common.fx

import javafx.application.Platform
import javafx.scene.Node
import com.runemate.game.api.script.framework.AbstractBot
import com.runemate.ui.DefaultUI
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.Labeled
import javafx.scene.control.TextInputControl
import javafx.scene.control.TitledPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox

fun AbstractBot.getPaneByTitle(title: String): TitledPane? {
    val controlPanel = this.embeddableUI as? DefaultUI
        ?: return null
    val container = controlPanel.botInterfaceProperty().get()?.contentContainer
        ?: return null

    return container.children.filterIsInstance<TitledPane>().find { it.text == title }
}

fun AbstractBot.addItemToPane(title: String, item: Node, first: Boolean) {
    val pane = this.getPaneByTitle(title)
    val vbox = pane?.content as? VBox
    Platform.runLater {
        VBox.setVgrow(item, javafx.scene.layout.Priority.ALWAYS)
        if (first) {
            vbox?.children?.add(0, item)
        } else {
            vbox?.children?.add(item)
        }
    }
}
fun AbstractBot.getAllPanes(): List<TitledPane>? {
    val controlPanel = this.embeddableUI as? DefaultUI ?: return null
    val container = controlPanel.botInterfaceProperty().get()?.contentContainer ?: return null

    return container.children.filterIsInstance<TitledPane>()
}
fun Parent.getAllComponents(): List<Node> {
    val nodes = mutableListOf<Node>()
    this.childrenUnmodifiable.forEach { node ->
        nodes.add(node)
        if (node is Parent) {
            nodes.addAll(node.getAllComponents())
        }
    }
    return nodes
}