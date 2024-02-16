package com.example.gatewayserver.error

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.lang.Nullable

class ForbiddenException(
    message: String? = "Forbidden.",
    @Nullable cause: Throwable? = null
) : RuntimeException(message, cause){

    fun getStatusCode(): HttpStatusCode {
        return HttpStatus.FORBIDDEN
    }

    fun getBody(): CustomProblemDetail {
        return CustomProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, message!!)
    }

}
