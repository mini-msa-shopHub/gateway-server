package com.example.gatewayserver.error

import org.springframework.http.HttpStatus

data class CustomProblemDetail(
    val title: String,
    val status: Int,
    val detail: String,
){
    companion object {
        fun forStatusAndDetail(
            status: HttpStatus,
            detail: String,
        ): CustomProblemDetail {
            return CustomProblemDetail(
                title = status.reasonPhrase,
                status = status.value(),
                detail = detail
            )
        }
    }
}