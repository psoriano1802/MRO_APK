package com.example.lacteos_flores.models

data class LoginRequest(
    val login: Login
)

data class Login(
    val user: String,
    val pass: String
)
