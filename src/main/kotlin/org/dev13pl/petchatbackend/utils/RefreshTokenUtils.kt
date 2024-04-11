package org.dev13pl.petchatbackend.utils

import jakarta.transaction.Transactional
import org.dev13pl.petchatbackend.models.RefreshToken
import org.dev13pl.petchatbackend.repositories.RefreshTokenRepository
import org.dev13pl.petchatbackend.repositories.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*

@Service
class RefreshTokenUtils(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    @Value("\${dev13pl.app.jwtRefreshExpirationMs}")
    private val refreshTokenDurationMs: Long
) {
    fun generateRefreshToken(email: String): RefreshToken {
        val refreshToken = RefreshToken(
            UUID.randomUUID().toString(),
            Instant.now().plusMillis(refreshTokenDurationMs),
            userRepository.findByEmail(email)
        )
        return refreshTokenRepository.save(refreshToken)
    }

    fun findByToken(token: String): RefreshToken? {
        return refreshTokenRepository.findByToken(token)
    }

    fun findByUserId(userId: Int): RefreshToken? {
        return refreshTokenRepository.findByUserId(userId)
    }

    @Transactional
    fun deleteByUserId(userId: Int): Int {
        return refreshTokenRepository.deleteByUserId(userId)
    }

    fun verifyExpiration(token: RefreshToken): RefreshToken {
        if (token.expiryDate!! < Instant.now()) {
            refreshTokenRepository.delete(token)
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                token.token + " Refresh token is expired. Please make a new login."
            )
        }
        return token
    }
}