package com.example.gatewayserver.filter

import com.example.gatewayserver.dto.EmailDto
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class JwtAuthenticationProcessingFilter : WebFilter {
    companion object {
        const val HEADER_KEY = "Authorization"
        const val BEAR = "Bearer "
        const val BLANK = ""
        const val AUTH_SERVICE_BASE_URL = "http://localhost:8000/api/v1/auth/"
        const val EMAIL = "email"
        const val PASSPORT = "passportToken"
        const val ALGORITHM = "HmacSHA256"
        const val SECRET_KEY = "this-is-secret"
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request

        if (!request.headers.containsKey(HEADER_KEY)) {
            return chain.filter(exchange)
        }

        val token = (request.headers[HEADER_KEY] ?: return chain.filter(exchange))[0].replace(BEAR, BLANK)
        val restTemplate = RestTemplate()
        val authenticatedEmail = getAuthenticatedEmail(restTemplate, token)
        val serverHttpRequest = exchange.request.mutate()
            .header(EMAIL, authenticatedEmail)
            .header(PASSPORT, createPassportToken(authenticatedEmail))
            .build()

        return chain.filter(exchange.mutate().request(serverHttpRequest).build())
    }

    private fun getAuthenticatedEmail(restTemplate: RestTemplate, token: String) =
        (restTemplate.exchange<EmailDto>("http://localhost:8000/api/v1/auth/$token", HttpMethod.GET)
            .body ?: throw IllegalArgumentException()).value

    private fun createPassportToken(email: String): String {
        try {
            val instance = Mac.getInstance(ALGORITHM)
            val secretKey = SECRET_KEY
            val keySpec = SecretKeySpec(secretKey.toByteArray(), ALGORITHM)
            instance.init(keySpec)
            instance.update(email.toByteArray())
            return Base64.getEncoder().encodeToString(instance.doFinal(email.toByteArray()))
        } catch (e: Exception) {
            println(e.message)
            return BLANK
        }
    }

}