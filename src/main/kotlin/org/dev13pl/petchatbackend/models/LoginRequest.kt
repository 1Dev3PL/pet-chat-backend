package org.dev13pl.petchatbackend.models

data class LoginRequest(
    val email: String,
    val password: String
)
