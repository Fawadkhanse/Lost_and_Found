package com.example.lostandfound.domain

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val detail: List<String>? = null,
    val message: String? = null,
    val non_field_errors: List<String>? = null,
    val errors: Map<String, List<String>>? = null
) {
    fun firstErrorMessage(): String? {
        when {
            !non_field_errors.isNullOrEmpty() -> return non_field_errors.first()
            !detail.isNullOrEmpty() -> return detail.first()        // ✅ fixed here
            !message.isNullOrEmpty() -> return message
        }
        errors?.forEach { (_, messages) ->
            if (!messages.isNullOrEmpty()) {
                return messages.first()
            }
        }
        return null
    }
}

