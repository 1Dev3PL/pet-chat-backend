package org.dev13pl.petchatbackend.services

import jakarta.servlet.http.HttpServletResponse
import org.dev13pl.petchatbackend.errors.NoTokenException
import org.dev13pl.petchatbackend.errors.RegistrationException
import org.dev13pl.petchatbackend.models.*
import org.dev13pl.petchatbackend.repositories.UserRepository
import org.dev13pl.petchatbackend.utils.JwtUtils
import org.dev13pl.petchatbackend.utils.RefreshTokenUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val repository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtils,
    private val refreshTokenUtils: RefreshTokenUtils,
    private val authenticationManager: AuthenticationManager,
    @Value("\${dev13pl.app.jwtExpirationMs}")
    private val jwtExpirationMs: Long,
    @Value("\${dev13pl.app.jwtRefreshExpirationMs}")
    private val jwtRefreshExpirationMs: Long,
) {
    fun fetchAuthData(accessToken: String): LoginResponse {
        val userEmail: String = jwtUtils.extractUsername(accessToken)!!
        val user: User = repository.findByEmail(userEmail)!!

        return LoginResponse(user.userName, user.email, user.role!!)
    }

    fun register(requestBody: RegisterRequest): MessageResponse {
        if (repository.findByEmail(requestBody.email) != null)
            throw RegistrationException()

        val user = User(
            requestBody.userName,
            requestBody.email,
            passwordEncoder.encode(requestBody.password),
            Role.USER,
        )
        repository.save(user)
        return MessageResponse("User successfully created")
    }

    fun login(requestBody: LoginRequest, response: HttpServletResponse): LoginResponse {
        // TODO Check isAuthenticated?
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(requestBody.email, requestBody.password))
        val user: User = repository.findByEmail(requestBody.email)!!
        val accessToken: String = jwtUtils.generateToken(user)

        if (refreshTokenUtils.findByUserId(user.id!!) != null) {
            refreshTokenUtils.deleteByUserId(user.id)
        }
        val refreshToken: RefreshToken = refreshTokenUtils.generateRefreshToken(requestBody.email)

        val accessTokenCookie: ResponseCookie = ResponseCookie.from("accessToken", accessToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(jwtExpirationMs / 1000)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())

        val refreshTokenCookie: ResponseCookie = ResponseCookie.from("refreshToken", refreshToken.token)
            .httpOnly(true)
            .secure(true)
            .path("/api/auth/refresh")
            .maxAge(jwtRefreshExpirationMs / 1000)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())

        return LoginResponse(user.userName, user.email, user.role!!)
    }

    fun refresh(refreshToken: String?, response: HttpServletResponse) {
        if (refreshToken == null) throw NoTokenException()
        //TODO Catch this
        val token: RefreshToken =
            refreshTokenUtils.findByToken(refreshToken) ?: throw RuntimeException("Refresh Token is not in DB!")
        refreshTokenUtils.verifyExpiration(token)
        val accessToken: String = jwtUtils.generateToken(token.user!!)
        val accessTokenCookie: ResponseCookie = ResponseCookie.from("accessToken", accessToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(jwtExpirationMs / 1000)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
    }

    fun logout(requestBody: LogoutRequest, response: HttpServletResponse) {
        val user: User = repository.findByEmail(requestBody.email)!!
        refreshTokenUtils.deleteByUserId(user.id!!)

        val accessTokenCookie: ResponseCookie = ResponseCookie.from("accessToken", "none")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(1)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())

        val refreshTokenCookie: ResponseCookie = ResponseCookie.from("refreshToken", "none")
            .httpOnly(true)
            .secure(true)
            .path("/api/auth/refresh")
            .maxAge(1)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
    }
}