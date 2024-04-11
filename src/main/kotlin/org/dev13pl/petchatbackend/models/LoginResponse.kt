package org.dev13pl.petchatbackend.models

data class LoginResponse(
    val userName: String,
    val email: String,
    val role: Role
)