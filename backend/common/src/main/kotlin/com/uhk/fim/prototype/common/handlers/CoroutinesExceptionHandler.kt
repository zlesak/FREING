package com.uhk.fim.prototype.common.handlers

import kotlinx.coroutines.CoroutineExceptionHandler
import org.springframework.stereotype.Service
import kotlin.coroutines.CoroutineContext

@Service
class CoroutinesExceptionHandler : CoroutineExceptionHandler {

    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler.Key


    override fun handleException(context: CoroutineContext, exception: Throwable) {
        println("[CoroutineExceptionHandler] Exception handler $exception")
    }
}