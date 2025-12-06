package com.uhk.fim.prototype.common.messaging.enums.actions

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class CustomerMessageAction : IMessageAction {
    GET_CUSTOMER_BY_ID,
    GET_SUPPLIER_BY_ID
}