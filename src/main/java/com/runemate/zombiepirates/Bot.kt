package com.runemate.zombiepirates

import com.runemate.common.*
import com.runemate.common.fx.FileManager
import com.runemate.common.item.*
import com.runemate.common.framework.core.TaskMachine
import com.runemate.game.api.hybrid.RuneScape
import com.runemate.game.api.hybrid.local.Wilderness
import com.runemate.game.api.hybrid.local.hud.interfaces.Health
import com.runemate.game.api.osrs.local.hud.interfaces.Prayer
import com.runemate.game.api.script.framework.listeners.SettingsListener
import com.runemate.game.api.script.framework.listeners.events.SettingChangedEvent
import com.runemate.common.fx.bank.BankDisplay
import com.runemate.common.fx.inventory.InventoryDisplay
import com.runemate.common.settings.AccountManager
import com.runemate.common.settings.SharedManager
import com.runemate.common.framework.state.HoppingState
import com.runemate.common.framework.core.BaseState
import com.runemate.common.framework.core.addons.BotState
import com.runemate.common.framework.core.bot.BotComponents
import com.runemate.common.framework.taskstate.GEState
import com.runemate.game.api.client.ClientUI
import com.runemate.ui.DefaultUI
import com.runemate.ui.setting.annotation.open.SettingsProvider
import com.runemate.zombiepirates.state.*
import javafx.scene.layout.HBox
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis
import com.runemate.common.LoggerUtils.getLogger
import com.runemate.game.api.script.Execution

class Bot : TaskMachine<ZombieSettings>(), SettingsListener {
    override val botComponents = BotComponents(this)
    private val log = getLogger("Bot")
    private var started: Boolean = false
    private var setPlayerInfo: Boolean = false
    private val container = HBox()
    private lateinit var bankDisplay: BankDisplay<ZombieSettings>
    private lateinit var inventoryDisplay: InventoryDisplay<ZombieSettings>

    @SettingsProvider(updatable = true)
    private lateinit var ZombieSettings: ZombieSettings


    override fun getStateInstance(state: BotState): BaseState {
        return when (state) {
            BotState.StartState -> StartState()
            BotState.BankState -> BankState()
            BotState.CombatState -> CombatState()
            BotState.AdvanceState -> AdvanceState()
            BotState.RetreatState -> RetreatState()
            BotState.AntiPkState -> AntiPkState()
            BotState.GEState -> GEState(this)
            BotState.HopState -> HoppingState(this)
        }
    }


    fun needToBank(): Boolean {
        runCatching {
            if ((Food.getVirtualInvHealth() + Health.getCurrent()) < ZombieSettings.minVirtualHealth) {
                log.debug("needToBank: Virtual Health less than minVirtualHealth")
                return true
            }
            if ((Restore.getVirtualInvPrayer() + Prayer.getPoints()) < ZombieSettings.minVirtualPrayer) {
                log.debug("needToBank: Virtual Prayer less than minVirtualPrayer")
                return true
            }
            if (getLootManager().tripLootValue() > ZombieSettings.minTripValue && getLootManager().validateTripLoot()) {
                log.debug("needToBank: Value of loot > minTripValue")
                return true
            }
            if (((Food.getVirtualInvHealth() - Health.getMaximum() - 10) < ZombieSettings.minVirtualHealth) && !Wilderness.isInWilderness()) {
                log.debug("needToBank: VirtualHealth is less than MinVirtualHealth OUTSIDE of Wilderness")
                return true
            }
            if (((Restore.getVirtualInvPrayer() - Prayer.getMaximumPoints() - 5) < ZombieSettings.minVirtualPrayer) && !Wilderness.isInWilderness()) {
                log.debug("needToBank: VirtualPrayer is less than MinVirtualPrayer OUTSIDE of Wilderness")
                return true
            }
            if (!Wilderness.isInWilderness() && !util.isItemInInventoryOrEquipment(items.burningAmulet)) {
                log.debug("needToBank: Not in Wilderness && no Burning Amulet")
                return true
            }
        }.onFailure { log.error("Failure runCatching needToBank: $it") }
        return false
    }

    override fun onStart(vararg arguments: String?) {
        log.debug("onStart")
        super.onStart(*arguments)
        this.setLoopDelay(10, 25)
        addListener(this)
        kotlin.runCatching {
            initAccountManager()
        }.onFailure { e ->
            log.error("Error loading managers: ${e.message}")
        }
        // Don't do this lol
        Execution.delayUntil({ ::ZombieSettings.isInitialized }, 10000)
        setUI()
    }

    private fun initAccountManager() {
        accountManager = AccountManager(this)
        sharedManager = SharedManager(this)
        accountManager.invokeOnManagerLoaded()
    }

    private val customExecutor: ExecutorService = Executors.newFixedThreadPool(3)
    private fun setLootItemsAsync(bankDisplay: BankDisplay<ZombieSettings>, entries: List<Loot>) {
        val future = CompletableFuture.runAsync({
            val timeTaken = measureTimeMillis {
                botComponents.bankDManager.setLootItems(bankDisplay, entries)
            }
            log.info("setLootItems() took $timeTaken ms")
        }, customExecutor)

        // Optionally, handle completion or errors
        future.whenComplete { _, throwable ->
            if (throwable != null) {
                log.error("Failed to set loot items: ${throwable.message}", throwable)
            }
        }
    }

    fun setUI() {
        log.debug("setUI")
        val itemOptions = listOf(
            Food.ZOMBIE_PIRATE_FOODS.map { SetItem(it.gameName, it.id) },
            Restore.RECOMMENDED_RESTORES.map { SetItem(it.title, it.id) },
            Boost.RECOMMENDED_BOOSTS.map { SetItem(it.title, it.id) }
        )

        val lootOptions = Loot.lootList
        log.debug("Setting Bank Items")
        botComponents.bankDManager.setBankItems(itemOptions)
        botComponents.bankDManager.setRecommendedItemIds(
            listOf(
                PrayerRestore.BlightedSuperRestore.id,
                Food.BlightedMantaRay.id
            )
        )
        log.debug("Preloading images")
        FileManager.preloadImages(Food.ALL_FOODS.map { it.id })
        kotlin.runCatching {
            log.debug("Setting UI")
            bankDisplay = BankDisplay(this)
            inventoryDisplay = InventoryDisplay(this)
            setLootItemsAsync(bankDisplay, lootOptions)
            log.debug("Setting Loot Items")
            container.children.addAll(bankDisplay, inventoryDisplay)
            log.debug("Adding Panels")
            DefaultUI.addPanel(0, this, "Bank & Inventory", container, true)
            log.debug("setUI complete")
        }.onFailure { log.error("Failed to set UI: $it") }.onSuccess { log.debug("UI set successfully") }
    }


    override fun onStop(reason: String?) {
        kotlin.runCatching { accountManager.saveAccountSettings() }
            .onFailure { log.error("Failed to save account settings: $it") }
        removeListener(this)
        container.isDisable = true
        //botComponents.bankDManager.saveLootItemIds()
        super.onStop(reason)
    }

    override fun onLoop() {
        if (isPaused) return
        if (!started) return
        if (!setPlayerInfo) return setPlayerInfo()
        super.onLoop()

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun defaultState(): BotState {
        return BotState.StartState
    }

    override fun onManagerLoaded() {
        log.debug("onManagersLoaded")
        botComponents.invDManager.loadInventoryItems(accountManager.accountSettings)
        log.debug("before inventory manager ${botComponents.invManager.inventory.size}")
        botComponents.invManager.inventory = botComponents.invDManager.getInventory().toMutableList()
        log.debug("after inventory manager ${botComponents.invManager.inventory.size}")
        //botComponents.bankDManager.setMinLootValue()
    }

    override fun initFromSettings(settings: ZombieSettings) {
        TODO("Not yet implemented")
    }

    override fun onSettingChanged(p0: SettingChangedEvent?) {
        p0?.key?.let { log.debug(it) }

    }

    override fun onSettingsConfirmed() {
        //log.debug(zombieSettings.inventory.toString())
        val foodAmount = getInvManager().getFoodAmount()
        val restoreAmount = getInvManager().getRestoreAmount()
        if (foodAmount < 5 || restoreAmount < 1) {
            val errorMessage = when {
                foodAmount < 5 && restoreAmount < 1 -> "Not enough food & restore items to start: Minimum 5 food and 1 restore potion required"
                foodAmount < 5 -> "Not enough food to start: Minimum 5 food required"
                else -> "Not enough restore items to start: Minimum 1 restore item required"

            }
            ClientUI.showAlert(ClientUI.AlertLevel.ERROR, errorMessage)
            log.warn(errorMessage)
            return
        }
        started = true
        log.debug("Updated Settings Confirmed")
    }

    private fun setPlayerInfo() {
        if (RuneScape.isLoggedIn()) {
            log.debug("SetPlayerInfo started")
            val minHealth = Health.getMaximum() - (ZombieSettings.food.heal + 3)
            val minPrayer = Prayer.getMaximumPoints() - (Restore.getRestoreAmount() + 2)
            PrayerFlicker.initialize(ZombieSettings)
            PrayerFlicker.setQuickPrayers(Prayer.PROTECT_ITEM, Prayer.PROTECT_FROM_MAGIC)
            Consumeables.initialize(this, minHealth, minPrayer)
            addListener(Consumeables)
            AntiPkManager.initialize(this)
            addListener(AntiPkManager)
            getEquipmentManager().setEquipment()
            getInvManager().inventory.forEach {
                log.debug("Item: ${it.name} Amount: ${it.quantity}")

            }
            setPlayerInfo = true
            log.debug("SetPlayerInfo complete")
        }
    }

    override fun settings(): ZombieSettings {
        return ZombieSettings
    }


    override fun updateStatus(s: String?) {
        if (s != null) {
            DefaultUI.setStatus(s)
        }
    }
}