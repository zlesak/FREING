package com.uhk.fim.prototype.common.config

import kotlinx.coroutines.*
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class CoroutineConfig {

    @Bean
    fun coroutineExceptionHandler(): CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            println("Uncaught coroutine exception")
        }

    @Bean(destroyMethod = "close")
    fun rabbitDispatcher(): ExecutorCoroutineDispatcher =
        Executors.newFixedThreadPool(32).asCoroutineDispatcher()

    @Bean
    fun appScope(
        rabbitDispatcher: ExecutorCoroutineDispatcher,
        handler: CoroutineExceptionHandler
    ): CoroutineScope =
        CoroutineScope(SupervisorJob() + rabbitDispatcher + handler)

    @Bean
    fun appScopeDisposable(scope: CoroutineScope) = DisposableBean { scope.cancel() }

}

fun Runnable.asCoroutine(scope: CoroutineScope) {
    scope.launch {
        try {
            println("Running task in coroutine on thread: ${Thread.currentThread().name}")
            this@asCoroutine.run()
        } catch (ex: Exception) {
            println("Error in coroutine task: $ex")
        }
    }
}