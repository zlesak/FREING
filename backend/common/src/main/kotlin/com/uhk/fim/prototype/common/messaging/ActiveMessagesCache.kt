package com.uhk.fim.prototype.common.messaging

import java.util.concurrent.CompletableFuture

/**
 * Cache for active messages interface. Classes which implement this interface should has different cacheType
 * cacheType: T means that this cache is responsible for responses which are of type T
 */
interface ActiveMessagesCache<T> {
    val messagesCache: MessagesCache<T>

    fun register(correlationId: String, future: CompletableFuture<T>){
        messagesCache.register(correlationId, future)
    }
    fun unregister(correlationId: String): CompletableFuture<T>? {
       return messagesCache.unregister(correlationId)
    }
}