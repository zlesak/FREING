package com.uhk.fim.prototype.common.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun <T> T.processInCoroutine(
    scope: CoroutineScope,
    process: suspend (T) -> Unit
) {
    scope.launch {
        try {
            println("Running task in coroutine on thread: ${Thread.currentThread().name}")
            process(this@processInCoroutine)
        } catch (ex: Exception) {
            println("Error in coroutine task: $ex")
        }
    }
}