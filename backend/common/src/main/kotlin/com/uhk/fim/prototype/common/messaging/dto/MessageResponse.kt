package com.uhk.fim.prototype.common.messaging.dto

import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import kotlin.reflect.full.primaryConstructor

class MessageResponse(
    val requestId: String,
    val targetId: Long? = null, //customerId, invoiceId etc.
    val status: MessageStatus,
    val payload: Map<String, Any?> = emptyMap(),
    val error: ErrorProps? = null
)

class ErrorProps(
    val message: String,
    val type: Class<*>,
)

fun ErrorProps.throwError(): Nothing {
    val constructor = this.type.getConstructor(String::class.java)
    val exception = constructor.newInstance(this.message) as Throwable
    throw exception
}

fun ErrorProps.buildException(): Throwable {
    val kClass = type.kotlin

    kClass.primaryConstructor?.let { constructor ->
        val args = try {
            constructor.parameters.associateWith { param ->
                when (param.name) {
                    "message" -> message
                    else -> null
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }

        return constructor.callBy(args) as Throwable
    }

    return try {
        type.getConstructor(String::class.java).newInstance(message) as Throwable
    } catch (_: NoSuchMethodException) {
        try {
            type.getDeclaredConstructor().newInstance() as Throwable
        } catch (ex: Exception) {
            throw IllegalArgumentException("Cannot instantiate exception of type $type", ex)
        }
    }
}
