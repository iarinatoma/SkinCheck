package com.example.skincheck.data

// What people with one skin type think of a product (summary of Sephora reviews)
data class CommunityRating(
    val skinType: String,          // "dry", "oily", "combination" or "normal"
    val reviews: Int,
    val avgRating: Double?,
    val recommendedPct: Int?
)