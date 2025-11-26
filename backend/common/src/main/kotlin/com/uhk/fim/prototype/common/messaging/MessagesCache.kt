package com.uhk.fim.prototype.common.messaging

import com.uhk.fim.prototype.common.exceptions.PendingMessageException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class MessagesCache<T> {

    private val messagesCache = ConcurrentHashMap<String, CompletableFuture<T>>()

    fun register(correlationId: String, future: CompletableFuture<T>) {
        println("Registering active message for $correlationId")
        messagesCache[correlationId] = future
    }

    fun unregister(correlationId: String): CompletableFuture<T>? {
        return messagesCache.remove(correlationId)
    }
}