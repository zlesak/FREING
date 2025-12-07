package com.uhk.fim.prototype.common.messaging.preprocessor

import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

@Component
class MessageListenerProcessorChain(
    private val messageExtractor: MessageExtractor,
): MethodInterceptor {

    private val processors: MutableList<MessageListenerProcessor> = mutableListOf()
    private val logger = LoggerFactory.getLogger(MessageListenerProcessorChain::class.java)

    override fun invoke(invocation: MethodInvocation): Any? {
        val inputData = messageExtractor.extractMessage(invocation.arguments)?: return invocation.proceed()

        processors.forEach { it.process(inputData) }

        return invocation.proceed()
    }

    fun registerProcessor(processor: MessageListenerProcessor): MessageListenerProcessorChain{
        logger.debug("[MessageListenerProcessorChain] registered new processor: {}", processor::class.simpleName)
        processors.add(processor)
        return this
    }
}

data class MessageProcess(
    var correlationId: String,
    var message: Any? = null, // Can be MessageRequest<*> or MessageResponse
) {
    val messageResponse: MessageResponse?
        get() = message as? MessageResponse

    val messageRequest: MessageRequest<*>?
        get() = message as? MessageRequest<*>

    fun isResponse(): Boolean = message is MessageResponse
    fun isRequest(): Boolean = message is MessageRequest<*>
}
