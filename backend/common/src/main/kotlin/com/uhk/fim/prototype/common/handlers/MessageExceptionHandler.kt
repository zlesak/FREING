package com.uhk.fim.prototype.common.handlers

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component


@Aspect
@Component
class MessageExceptionHandler {

    @Before("@annotation(rabbitListener)")
    fun beforeRabbitListener(joinPoint: JoinPoint) {
        println("Exception handle RabbitListener: " + joinPoint.signature.name)

        val args = joinPoint.args
    }
}