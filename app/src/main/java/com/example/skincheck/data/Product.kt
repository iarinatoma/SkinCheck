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
    val suitableFor: Set<String> // e.g. {"dry", "normal", "sensitive"}
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

    // The cleaned list of ingredient names (see IngredientParser)
    fun ingredientList(): List<String> = IngredientParser.parse(ingredients)
}