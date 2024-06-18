package com.runemate.common.fx.bank.components

import com.runemate.common.framework.core.bot.BotConfig
import com.runemate.common.fx.bank.BankDisplay
import com.runemate.common.framework.core.TaskMachine
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent

class DisplayEventHandler<TSettings : BotConfig>(
    private val display: BankDisplay<TSettings>,
    private val bot: TaskMachine<TSettings>
) : EventHandler<MouseEvent> {
    private val settings: TSettings
        get() = bot.settings()
    // Implement event handling logic here
    override fun handle(event: MouseEvent?) {
        TODO("Not yet implemented")
    }

    fun handleDepositAllClick() {
        bot.botComponents.invDManager.getInventory().forEach { bot.botComponents.invDManager.removeFromInventory(it, removeAll = true) }
        //display.messageOverlay.message("Welcome to Recursive Zombie Pirates. Expect there to be bugs!")
    }

    fun handleLootTabClick() {
        display.bankGrid.updateBankForLoot()
        display.header.titleLabel.text = "Loot Tab"
        //display.messageOverlay.clearMessages()
        display.footer.showMinLootButtons()
    }

    fun handleAllItemsTabClick() {
        display.bankGrid.updateBank()
        display.header.titleLabel.text = "Bank of Recursion"
        display.footer.hideMinLootButtons()
    }
    fun updateLootDisplay(value: Int) {
        //settings.minLootValue = value
        display.updateLootItemsStyle()
    }

}