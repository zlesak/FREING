package com.uhk.fim.prototype.common.messaging

import com.uhk.fim.prototype.common.messaging.dto.MessageIds
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Service
class ActiveMessagingManager(
    private val activeMessageRegistry: ActiveMessageRegistry
) {

    fun registerMessage(timeoutSeconds: Long = 5L, correlationId: String?= null, requestId: String?= null, send: (MessageIds)-> Unit): MessageResponse {
        val future = CompletableFuture<MessageResponse>()
        val (generatedCorrelationId, generatedRequestId) = generateIds()
        val activeCorrelationId = correlationId ?: generatedCorrelationId
        val activeRequestId = requestId ?: generatedRequestId
        activeMessageRegistry.register(activeCorrelationId, future)
        send(MessageIds(activeCorrelationId, activeRequestId))
        return try {
            future.get(timeoutSeconds, TimeUnit.SECONDS)
        }catch (ex: Exception){
            unregisterMessage(activeCorrelationId)
            throw ex
        }
    }

    fun unregisterMessage(correlationId: String, response: MessageResponse? = null): MessageResponse? {
       println("Unregistering message with correlationId: $correlationId")
       val message = activeMessageRegistry.unregister(correlationId)
       message?.complete(response)
       return message?.get()
    }

    fun completeExceptionally(correlationId: String, ex: Throwable) {
        val message = activeMessageRegistry.unregister(correlationId)
        message?.completeExceptionally(ex)
    }


    private fun generateIds() = Pair(UUID.randomUUID().toString(), UUID.randomUUID().toString())
}