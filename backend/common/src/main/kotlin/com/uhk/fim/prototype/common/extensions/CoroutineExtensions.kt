package com.uhk.fim.prototype.common.extensions

import com.uhk.fim.prototype.common.messaging.coroutines.CorrelationId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.amqp.core.Message
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> T.processInCoroutineWithContext(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    process: suspend (T) -> Unit
) {
    scope.launch(scope.coroutineContext + context) {
        println("Running task in coroutine on thread: ${Thread.currentThread().name}")
        process(this@processInCoroutineWithContext)
    }
}

fun <T : Message> T.processInCoroutine(
    scope: CoroutineScope,
    process: suspend (T) -> Unit
) {
    val correlationId = CorrelationId(this.messageProperties.correlationId)
    processInCoroutineWithContext(scope, correlationId, process)
}

fun <T> CompletableFuture<T>.getUnwrapped(timeoutSeconds: Long, timeUnit: TimeUnit): T {
    return try {
        this.get(timeoutSeconds, timeUnit)
    } catch (ex: java.util.concurrent.ExecutionException) {
        throw ex.cause ?: ex
    } catch (ex: java.util.concurrent.TimeoutException) {
        throw ex
    }
}