package com.uhk.fim.prototype.common.messaging

import com.uhk.fim.prototype.common.messaging.dto.ErrorProps
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.actions.IMessageAction
import org.springframework.stereotype.Component

@Component
class InvalidMessageActionHandler(
    private val messageSender: MessageSender
) {
    fun handleInvalidMessageAction(
        request: MessageRequest<IMessageAction>,
        correlationId: String,
        replyTo: String
    ) {
        val response = MessageResponse(
            requestId = request.requestId,
            targetId = request.targetId,
            status = MessageStatus.UNSUPPORTED_ACTION,
            error = ErrorProps(
                "Unsupported action: ${request.action}",
                IllegalStateException::class.java
            )
        )
        messageSender.sendResponse(response, replyTo, correlationId)
    }
}