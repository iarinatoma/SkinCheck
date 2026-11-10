package com.example.skincheck.data

// One row of the "users" table, as a Kotlin object (no password here!)
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val skinType: String?
)