package com.uhk.fim.prototype.common.handlers

import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.coroutines.CorrelationId
import kotlinx.coroutines.CoroutineExceptionHandler
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

@Service
class CoroutinesExceptionHandler(
    val activeMessagingManager: ActiveMessagingManager
) : CoroutineExceptionHandler {

    private val logger = LoggerFactory.getLogger(CoroutinesExceptionHandler::class.java)

    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler.Key


    override fun handleException(context: CoroutineContext, exception: Throwable) {
        val correlationId = context[CorrelationId]?.id
        if (correlationId == null) {
            logger.warn("[CoroutineExceptionHandler] Exception but correlationId is null", exception)
            return
        }

        logger.error("[CoroutineExceptionHandler] Exception handler {} with correlationID {}", exception, correlationId)
        activeMessagingManager.completeExceptionally(correlationId, exception)
    }
}