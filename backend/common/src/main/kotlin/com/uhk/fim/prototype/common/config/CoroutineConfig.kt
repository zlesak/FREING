package com.uhk.fim.prototype.common.config

import com.uhk.fim.prototype.common.handlers.CoroutinesExceptionHandler
import kotlinx.coroutines.*
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class CoroutineConfig {


    @Bean(destroyMethod = "close")
    fun rabbitDispatcher(): ExecutorCoroutineDispatcher =
        Executors.newFixedThreadPool(32).asCoroutineDispatcher()

    @Bean
    fun rabbitScope(
        rabbitDispatcher: ExecutorCoroutineDispatcher,
        handler: CoroutinesExceptionHandler
    ): CoroutineScope =
        CoroutineScope(Job() + rabbitDispatcher + handler)

    @Bean
    fun appScopeDisposable(scope: CoroutineScope) = DisposableBean { scope.cancel() }

}