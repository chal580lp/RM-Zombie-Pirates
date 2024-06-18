package com.runemate.common.framework.core

import com.runemate.common.framework.core.addons.BotState
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object DIContainer {
    private val registry = ConcurrentHashMap<KClass<*>, Any>()
    private val customEventListeners = ConcurrentHashMap<KClass<*>, MutableList<(Any) -> Unit>>()

    fun <T : Any> register(instance: T) {
        registry[instance::class] = instance
    }

    fun unregister(klass: KClass<*>) {
        registry.remove(klass)
    }

    fun <T : Any> resolve(klass: KClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return registry[klass] as? T ?: error("No instance registered for $klass")
    }

    fun <T : Any> subscribe(eventClass: KClass<T>, listener: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        customEventListeners.getOrPut(eventClass) { mutableListOf() }.add(listener as (Any) -> Unit)
    }

    fun <T : Any> unsubscribe(eventClass: KClass<T>, listener: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        customEventListeners[eventClass]?.remove(listener as (Any) -> Unit)
    }

    fun <T : Any> publish(event: T) {
        customEventListeners[event::class]?.forEach { listener ->
            listener(event)
        }
    }
}

inline fun <reified T : Any> injected() = lazy { DIContainer.resolve(T::class) }

inline fun <reified T : Any> subscribe(noinline listener: (T) -> Unit) {
    DIContainer.subscribe(T::class, listener)
}

inline fun <reified T : Any> unsubscribe(noinline listener: (T) -> Unit) {
    DIContainer.unsubscribe(T::class, listener)
}

inline fun <reified T : Any> publish(event: T) {
    DIContainer.publish(event)
}

// Custom event classes
data class WarningAndPause(val reason: String)
data class ConditionalStateCompletedEvent(val reason: String, val state: BotState)
data class ProgressUpdatedEvent(val progress: Int)
// Add more custom event classes as needed
