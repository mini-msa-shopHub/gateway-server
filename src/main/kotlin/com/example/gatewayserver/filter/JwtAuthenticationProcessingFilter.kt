package com.example.gatewayserver.filter

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
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

        val token = request.headers["Authorization"] ?: return chain.filter(exchange)
        val tokenString = token[0].replace("Bearer ", "")

        if (!isTokenValid(tokenString)) {
            return handleUnAuthorized(exchange)
        }
        val extractEmail = extractEmail(tokenString) ?: return handleUnAuthorized(exchange)


        val serverHttpRequest = exchange.request.mutate()
            .header("email", extractEmail)
            .header("passportToken", createPassportToken(extractEmail))
            .build()

        return chain.filter(exchange.mutate().request(serverHttpRequest).build())
    }

    private fun handleUnAuthorized(exchange: ServerWebExchange): Mono<Void> {
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        return exchange.response.setComplete()
    }

    private fun isTokenValid(token: String?): Boolean {
        if (token == null) {
            return false
        }
        return try {
            JWT.require(Algorithm.HMAC512("abcdefg"))
                .build().verify(token)
            true
        } catch (e: TokenExpiredException) {
            println(e.message)
            false
        } catch (e: AlgorithmMismatchException) {
            println(e.message)
            false
        } catch (e: SignatureVerificationException) {
            println(e.message)
            false
        } catch (e: JWTVerificationException) {
            println(e.message)
            false
        }
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

    fun extractEmail(token: String?): String? {
        return try {
            JWT.require(Algorithm.HMAC512("abcdefg"))
                .withIssuer("admin")
                .build()
                .verify(token)
                .getClaim("email")
                .asString()
        } catch (e: Exception) {
            println(e.message)
            println(e.stackTrace)
            null
        }
    }

}