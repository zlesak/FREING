package com.uhk.fim.prototype.common.messaging

import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class MessagesCache<T> {

    private val messagesCache = ConcurrentHashMap<String, CompletableFuture<T>>()
    private val logger = LoggerFactory.getLogger(MessagesCache::class.java)

    fun register(correlationId: String, future: CompletableFuture<T>) {
        logger.info("Registering active message for {}", correlationId)
        messagesCache[correlationId] = future
    }

    fun unregister(correlationId: String): CompletableFuture<T>? {
        return messagesCache.remove(correlationId)
    }
}