package org.dev13pl.petchatbackend.models

data class RegisterRequest(
    val userName: String,
    val email: String,
    val password: String
)
