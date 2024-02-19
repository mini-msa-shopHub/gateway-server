package com.example.gatewayserver.dto

data class EmailDto(
    val value: String,
    val passportDto: PassportDto? = null
)
