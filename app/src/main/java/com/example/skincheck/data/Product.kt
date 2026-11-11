package com.example.skincheck.data

// One row of the "products" table
data class Product(
    val id: Long,
    val category: String,
    val brand: String,
    val name: String,
    val price: Double?,
    val rating: Double?,
    val ingredients: String,
    val suitableFor: Set<String> // e.g. {"dry", "normal", "sensitive"}
) {
    // Is this product suitable for a skin type code like "oily,sensitive"?
    // Returns null if the user has not taken the quiz yet.
    fun isSuitableFor(skinCode: String?): Boolean? {
        if (skinCode.isNullOrBlank()) return null
        val parts = skinCode.split(",")
        val baseOk = parts[0] in suitableFor
        val sensitiveOk = "sensitive" !in parts || "sensitive" in suitableFor
        return baseOk && sensitiveOk
    }
}