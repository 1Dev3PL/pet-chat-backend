package org.dev13pl.petchatbackend.repositories

import org.dev13pl.petchatbackend.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: JpaRepository<User, Int> {
    fun findByEmail(email: String): User?
}