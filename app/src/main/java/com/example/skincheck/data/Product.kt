package com.example.skincheck.data

import java.util.Locale

// One row of the "products" table
data class Product(
    val id: Long,
    val category: String,
    val brand: String,
    val name: String,
    val price: Double?,
    val rating: Double?,
    val ingredients: String,
    val suitableFor: Set<String>, // e.g. {"dry", "normal", "sensitive"}
    val loves: Int = 0,           // how many people "loved" it on Sephora (popularity)
    val reviews: Int = 0          // how many reviews it has on Sephora
) {
    // Is this product suitable for a skin type code like "oily,sensitive"?
    // Returns null if the user has not taken the quiz yet.
    fun isSuitableFor(skinCode: String?): Boolean? {
        if (skinCode.isNullOrBlank()) return null
        if (suitableFor.isEmpty()) return null // the dataset has no skin type info for this product
        val parts = skinCode.split(",")
        val baseOk = parts[0] in suitableFor
        val sensitiveOk = "sensitive" !in parts || "sensitive" in suitableFor
        return baseOk && sensitiveOk
    }

    // "$68", or null if the price is unknown
    fun priceText(): String? = price?.let { String.format(Locale.US, "$%.0f", it) }

    // "★ 4.3", or null if nobody rated it (0 in the dataset)
    fun ratingText(): String? =
        rating?.takeIf { it > 0 }?.let { String.format(Locale.US, "★ %.1f", it) }

    // 12345 -> "♥ 12.3k", null if we have no popularity data
    fun lovesText(): String? = when {
        loves <= 0 -> null
        loves >= 1000 -> String.format(Locale.US, "♥ %.1fk", loves / 1000.0)
        else -> "♥ $loves"
    }

    // The cleaned list of ingredient names (see IngredientParser)
    fun ingredientList(): List<String> = IngredientParser.parse(ingredients)
}