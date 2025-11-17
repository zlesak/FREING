package com.uhk.fim.prototype.common.messaging

import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.springframework.stereotype.Service

@Service
class ActiveMessageRegistry : ActiveMessagesCache<MessageResponse> {
    override val messagesCache: MessagesCache<MessageResponse> = MessagesCache()
}