package org.dev13pl.petchatbackend.models

import jakarta.persistence.*
import java.time.Instant

@Entity
data class RefreshToken(
    val token: String = "",
    val expiryDate: Instant? = null,
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
)