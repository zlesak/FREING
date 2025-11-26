package com.uhk.fim.prototype.common.handlers

import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.coroutines.CorrelationId
import kotlinx.coroutines.CoroutineExceptionHandler
import org.springframework.stereotype.Service
import kotlin.coroutines.CoroutineContext

@Service
class CoroutinesExceptionHandler(
    val activeMessagingManager: ActiveMessagingManager
) : CoroutineExceptionHandler {

    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler.Key


    override fun handleException(context: CoroutineContext, exception: Throwable) {
        val correlationId = context[CorrelationId]?.id
        if (correlationId == null) {
            println("[[CoroutineExceptionHandler] Exception $exception but correlationId is null]")
            return
        }

        println("[CoroutineExceptionHandler] Exception handler $exception with correlationID $correlationId")
        activeMessagingManager.completeExceptionally(correlationId, exception)
    }
}