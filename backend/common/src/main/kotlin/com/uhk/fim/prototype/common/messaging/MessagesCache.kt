package com.uhk.fim.prototype.common.messaging

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class MessagesCache<T> {

    private val messagesCache = ConcurrentHashMap<String, CompletableFuture<T>>()

    fun register(correlationId: String, future: CompletableFuture<T>) {
        println("Registering active message for $correlationId")
        messagesCache[correlationId] = future
    }

    fun unregister(correlationId: String): CompletableFuture<T>? {
        val unregistered = messagesCache.remove(correlationId)
        if (unregistered == null) println("unregistering was not successful because there was no active message for $correlationId")
        return unregistered
    }
}