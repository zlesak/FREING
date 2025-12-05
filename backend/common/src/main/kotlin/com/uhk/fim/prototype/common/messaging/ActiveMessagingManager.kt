package com.uhk.fim.prototype.common.messaging

import com.uhk.fim.prototype.common.extensions.getUnwrapped
import com.uhk.fim.prototype.common.messaging.dto.MessageIds
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

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
            future.getUnwrapped(timeoutSeconds, TimeUnit.SECONDS)
        }catch (ex: TimeoutException){
            unregisterMessage(activeCorrelationId)
            throw ex
        }
    }

    fun unregisterMessage(correlationId: String, response: MessageResponse? = null): MessageResponse? {
       println("Unregistering message with correlationId: $correlationId")
       val message = activeMessageRegistry.unregister(correlationId)

        if (message == null){
         println("unregistering was not successful because there was no active message for $correlationId")
       }

       message?.complete(response)
       return message?.get()
    }

    fun completeExceptionally(correlationId: String, ex: Throwable): Boolean {
        println("Unregistering message with correlationId: $correlationId exceptionally")
        val message = activeMessageRegistry.unregister(correlationId)
        message?.completeExceptionally(ex)
        return message != null
    }


    private fun generateIds() = Pair(UUID.randomUUID().toString(), UUID.randomUUID().toString())
}