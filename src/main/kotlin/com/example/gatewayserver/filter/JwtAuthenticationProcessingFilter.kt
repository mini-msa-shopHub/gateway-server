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
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request

        if (!request.headers.containsKey("Authorization")) {
            return chain.filter(exchange)
        }

        val token = (request.headers["Authorization"] ?: return chain.filter(exchange))[0].replace("Bearer ", "")
        val restTemplate = RestTemplate()
        val exchange1 =
            restTemplate.exchange<EmailDto>("http://localhost:8000/api/v1/auth/${token}", HttpMethod.GET)
                .body!!.value
        val serverHttpRequest = exchange.request.mutate()
            .header("email", exchange1)
            .header("passportToken", createPassportToken(exchange1))
            .build()

        return chain.filter(exchange.mutate().request(serverHttpRequest).build())
    }

    private fun createPassportToken(email: String): String {
        try {
            val instance = Mac.getInstance("HmacSHA256")
            val secretKey = "this-is-secret"
            val keySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
            instance.init(keySpec)
            instance.update(email.toByteArray())
            return Base64.getEncoder().encodeToString(instance.doFinal(email.toByteArray()))
        } catch (e: Exception) {
            println(e.message)
            return ""
        }
    }

}