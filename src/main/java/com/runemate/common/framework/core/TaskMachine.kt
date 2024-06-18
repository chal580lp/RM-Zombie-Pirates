package com.runemate.common.framework.core

import com.google.common.base.Stopwatch
import com.runemate.common.*
import com.runemate.common.fx.bank.BankDisplayManager
import com.runemate.common.fx.inventory.InventoryDisplayManager
import com.runemate.common.LootManager
import com.runemate.common.settings.AccountManager
import com.runemate.common.settings.SharedManager
import com.runemate.common.framework.core.addons.BotState
import com.runemate.game.api.client.ClientUI
import com.runemate.game.api.script.framework.LoopingBot
import com.runemate.common.framework.core.bot.BotComponents
import com.runemate.common.framework.core.bot.BotConfig

import java.util.*


abstract class TaskMachine<TSettings : BotConfig> : LoopingBot() {
    abstract val botComponents: BotComponents<TSettings>
    private val log = LoggerUtils.getLogger("TaskState")
    private val inactivity: Inactivity = Inactivity(this)

    //var breakManager: BreakManager? = null
    private var timer: Stopwatch? = null
    private var started = false
    private var loaded = false
    lateinit var accountManager: AccountManager<TSettings>
    lateinit var sharedManager: SharedManager
    private var currentState: BotState? = null
    private var currentBaseState: BaseState? = null
    private var previousState: BotState? = null
    private var conditionalState: BotState? = null
    private var previousConditionalState: BotState? = null
    private var conditionalStateCondition: () -> Boolean = { false }

    init {
        subscribe<ConditionalStateCompletedEvent> { event ->
            if (event.state == currentState) {
                log.info("Conditional State completed: ${event.reason}")
                revertToPreviousState()
            } else {
                log.error("Conditional State completed: ${event.reason} but not the current state ${currentState}")
            }
        }
        subscribe<WarningAndPause> { event ->
            warningAndPause(event.reason)
        }
    }

    fun getCurrentState(): BotState? {
        return currentState
    }

    fun setConditionalState(state: BotState, condition: () -> Boolean) {
        conditionalState = state
        conditionalStateCondition = condition
    }

    private fun revertToPreviousState() {
        previousState?.let { setCurrentState(it) }
        previousState = null
        conditionalState = null
        conditionalStateCondition = { false }
    }

    protected abstract fun getStateInstance(state: BotState): BaseState

    fun setCurrentState(state: BotState) {
        log.debug("Setting current state: {}", state)
        val value = getStateInstance(state)
        currentBaseState?.onExit()
        eventDispatcher.getListeners().stream()
            .filter { listener: EventListener? -> listener is BaseState }
            .forEach { listener: EventListener? ->
                if (listener != null) {
                    log.debug("Removing listener: {}", listener)
                    eventDispatcher.removeListener(listener)
                }
            }
        currentBaseState = value
        currentState = state

        if (value is EventListener) eventDispatcher.addListener(value)

        currentBaseState?.defineTransitions()
        (currentBaseState as? TaskState)?.defineTasks()
        currentBaseState?.onStart()
    }


    override fun onLoop() {
        if (conditionalState != null && conditionalStateCondition()) {
            log.debug("Conditional state condition met, transitioning to conditional state: {}", conditionalState)
            previousState = currentState
            setCurrentState(conditionalState!!)
            conditionalState = null
            conditionalStateCondition = { false }
        }
        if (currentBaseState == null) {
            log.debug("Current state is null, setting default state")
            setCurrentState(defaultState())
        }

        val transitions: List<Transition> = currentBaseState!!.getTransitions()

        for (transition in transitions) {
            if (transition.validate()) {
                log.debug("Transitioning to: {}", transition.transitionTo())
                setCurrentState(transition.transitionTo())
                return
            }
        }
        kotlin.runCatching {
            currentBaseState!!.execute()
        }.onFailure { e ->
            warningAndPause("Error executing state: ${currentBaseState?.javaClass?.name} : $e")
            setCurrentState(defaultState())
        }
    }

    override fun onStart(vararg arguments: String?) {
        DIContainer.register(this)
        super.onStart(*arguments)
    }

    abstract fun defaultState(): BotState

    abstract fun onManagerLoaded()

    protected abstract fun initFromSettings(settings: TSettings)

    abstract fun settings(): TSettings

    open fun onSettingsConfirmed() {
//        this.log.debug("Settings confirmed")
//        this.started = true
//        if (!isPaused) {
//            //resetKill()
//            addListener(di)
////            for (l in this.getEventListenerForRunning()) {
////                addListener(l!!)
////            }
//        }
//        if (settings() != null) {
//            val timer = Stopwatch.createStarted()
//            this.updateStatus("Loading... can take a minute depending on number of loadouts")
//            initFromSettings(settings())
//            this.updateStatus("Loading Finished")
//            this.log.debug("Load settings done in: " + timer.elapsed(TimeUnit.MILLISECONDS) + " ms")
////            if (settings() is LootSettings) {
////                Loot.dumpSettings(settings() as LootSettings)
////            }
//        } else {
//            this.log.error("Null settings")
//            this.updateStatus("Null settings")
//            this.pause("Null settings")
//        }
//        this.loaded = true
//        inactivity.start()
    }

    fun addListener(l: EventListener) {
        if (this.eventDispatcher.listeners.none { it::class.java.name == l::class.java.name }) {
            log.debug("Add listener ${l::class.java.name}")
            this.eventDispatcher.addListener(l)
        }
    }

    fun warningAndPause(reason: String) {
        ClientUI.showAlert(ClientUI.AlertLevel.ERROR, reason)
        log.error("Pausing: $reason")
        pause(reason)
    }

    fun removeListener(l: EventListener?) {
        if (l == null) return
        log.debug("Remove listener ${l::class.java.name}")
        this.eventDispatcher.removeListener(l)
    }

    fun startPauseAndEndBotTimeout(reason: String) {
        val msg = "Ending bot in 5 minutes, resume bot to stop timer. Reason: $reason"
        log.warn(msg)
        updateStatus(msg)
        pause(msg)
        ClientUI.showAlert(ClientUI.AlertLevel.ERROR, msg)
        //executor.schedule({ inactivity.startTimeout(5, msg) }, 2, TimeUnit.SECONDS)
    }

    abstract fun updateStatus(s: String?)

    fun getCombatManager(): CombatManager<TSettings> = botComponents.combatManager

    fun getLootManager(): LootManager<TSettings> = botComponents.lootManager

    fun getBankManager(): BankManager<TSettings> = botComponents.bankManager

    fun getInvManager(): InventoryManager = botComponents.invManager

    fun getEquipmentManager(): EquipmentManager = botComponents.equipmentManager

    fun getBankDManager(): BankDisplayManager<TSettings> = botComponents.bankDManager

    fun getInvDManager(): InventoryDisplayManager<TSettings> = botComponents.invDManager
}
