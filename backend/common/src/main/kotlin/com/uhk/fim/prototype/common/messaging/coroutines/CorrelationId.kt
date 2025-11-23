package com.uhk.fim.prototype.common.messaging.coroutines

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class CorrelationId(val id: String):
    AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<CorrelationId>
}
