package com.example.agroag_mro.models

data class LoginRequest(
    val login: Login
)

data class Login(
    val user: String,
    val pass: String
)
