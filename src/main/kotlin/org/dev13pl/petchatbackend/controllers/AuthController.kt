package org.dev13pl.petchatbackend.controllers

import jakarta.servlet.http.HttpServletResponse
import org.dev13pl.petchatbackend.errors.NoTokenException
import org.dev13pl.petchatbackend.errors.RegistrationException
import org.dev13pl.petchatbackend.models.*
import org.dev13pl.petchatbackend.services.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<MessageResponse> {
        val response = MessageResponse("Incorrect email or password")
        return ResponseEntity<MessageResponse>(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(RegistrationException::class)
    fun handleRegistrationException(ex: RegistrationException): ResponseEntity<MessageResponse> {
        val response = MessageResponse(ex.message!!)
        return ResponseEntity<MessageResponse>(response, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(NoTokenException::class)
    fun handleNoTokenException(ex: NoTokenException): ResponseEntity<MessageResponse> {
        val response = MessageResponse(ex.message!!)
        return ResponseEntity<MessageResponse>(response, HttpStatus.UNAUTHORIZED)
    }

    @GetMapping("/data")
    fun fetchAuthData(@CookieValue(name = "accessToken") accessToken: String): ResponseEntity<LoginResponse> =
        ResponseEntity.ok(authService.fetchAuthData(accessToken))

    @PostMapping("/register")
    fun register(@RequestBody body: RegisterRequest): ResponseEntity<MessageResponse> =
        ResponseEntity.ok(authService.register(body))

    @PostMapping("/login")
    fun login(@RequestBody body: LoginRequest, response: HttpServletResponse): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(authService.login(body, response))
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(name = "refreshToken", required = false) refreshToken: String?,
        response: HttpServletResponse
    ): ResponseEntity<Void> {
        authService.refresh(refreshToken, response)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/logout")
    fun logout(@RequestBody body: LogoutRequest, response: HttpServletResponse): ResponseEntity<Void> {
        authService.logout(body, response)
        return ResponseEntity.ok().build()
    }
}