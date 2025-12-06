package com.uhk.fim.prototype.common.messaging.enums.actions

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class InvoiceMessageAction : IMessageAction {
    GET
}