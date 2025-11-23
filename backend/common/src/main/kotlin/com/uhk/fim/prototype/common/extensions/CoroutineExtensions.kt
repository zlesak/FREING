package com.uhk.fim.prototype.common.extensions

import com.uhk.fim.prototype.common.messaging.coroutines.CorrelationId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.amqp.core.Message
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> T.processInCoroutineWithContext(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    process: suspend (T) -> Unit
) {
    scope.launch(scope.coroutineContext + context) {
        try {
            println("Running task in coroutine on thread: ${Thread.currentThread().name}")
            process(this@processInCoroutineWithContext)
        } catch (ex: Exception) {
            println("Error in coroutine task: $ex")
        }
    }
}

fun <T: Message> T.processInCoroutine(
    scope: CoroutineScope,
    process: suspend (Message) -> Unit){
    val correlationId = CorrelationId(this.messageProperties.correlationId)
    processInCoroutineWithContext(scope, correlationId, process)
}