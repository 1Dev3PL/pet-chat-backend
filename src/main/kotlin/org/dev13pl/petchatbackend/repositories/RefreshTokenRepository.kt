package org.dev13pl.petchatbackend.repositories

import org.dev13pl.petchatbackend.models.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository: JpaRepository<RefreshToken, Int> {
    fun findByToken(token: String): RefreshToken?

    fun findByUserId(userId: Int): RefreshToken?

    @Modifying
    fun deleteByUserId(userId: Int): Int
}