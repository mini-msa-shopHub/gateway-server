package com.example.gatewayserver.filter

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationProcessingFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request.headers.getFirst("Authorization")
        val headerToken = exchange.request.headers.getFirst("Authorization")
        val webClient = WebClient.create()
        if (request == null) {
            println("Fuck")
            return chain.filter(exchange)
        }
        val token = request.replace("Bearer ", "")
        if (true) {
            val authentication = getAuthentication(webClient, token).block()
            SecurityContextHolder.getContext().authentication = authentication
        }
        return chain.filter(exchange)
    }

    private fun getAuthentication(webClient: WebClient, headerToken: String): Mono<Authentication?> {
        return webClient.get()
            .uri("http://localhost:8000/api/v1/auth/authentication/$headerToken")
            .retrieve()
            .bodyToMono(Authentication::class.java)
    }
    private fun isTokenValid(webClient: WebClient, headerToken: String?): Mono<Boolean> {
        return webClient.get()
            .uri("http://localhost:8000/api/v1/auth/check-token/$headerToken")
            .retrieve()
            .bodyToMono(Boolean::class.java)
    }
}