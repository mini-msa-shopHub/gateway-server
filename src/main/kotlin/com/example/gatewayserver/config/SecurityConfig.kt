package com.example.gatewayserver.config

import com.example.gatewayserver.filter.JwtAccessDeniedHandler
import com.example.gatewayserver.filter.JwtAuthenticationEntryPoint
import com.example.gatewayserver.filter.JwtAuthenticationProcessingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
) {

    @Bean
    fun webFluxSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .exceptionHandling { exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            }
            .authorizeExchange {
                it.pathMatchers("/api/v1/users/join").permitAll()
                it.pathMatchers("/api/v1/users/refresh-token").permitAll()
                it.pathMatchers("/api/v1/users/user-info").permitAll()
                it.pathMatchers("/api/v1/auth/login").permitAll()
                it.pathMatchers("/api/v1/auth/encode-password").permitAll()
                it.pathMatchers("/api/v1/auth/authentication/**").permitAll()
                it.pathMatchers("/api/v1/auth/check-token/**").permitAll()
                it.anyExchange().authenticated()
            }
            .addFilterAt(jwtAuthenticationProcessingFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }

    @Bean
    fun jwtAuthenticationProcessingFilter(): JwtAuthenticationProcessingFilter {
        return JwtAuthenticationProcessingFilter()
    }

}