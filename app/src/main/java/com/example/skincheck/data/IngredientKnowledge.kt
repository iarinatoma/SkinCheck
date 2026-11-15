package com.example.skincheck.data

import android.content.Context
import org.json.JSONObject

// The colors of SkinCheck recommends
enum class Rating { GOOD, CAUTION, AVOID, NEUTRAL }

// One entry of assets/ingredients.json
data class IngredientInfo(
    val name: String,
    val ratings: Map<String, Rating>,   // "dry" -> GOOD, ... (a missing skin type = neutral)
    val why: String,
    val source: String
) {
    // Rating for a skin code like "combination,sensitive": the WORST one counts.
    // No quiz yet -> we judge for normal skin.
    fun ratingFor(skinCode: String?): Rating {
        val parts = (skinCode ?: "normal").split(",")
        val found = parts.mapNotNull { ratings[it] }
        return when {
            Rating.AVOID in found -> Rating.AVOID
            Rating.CAUTION in found -> Rating.CAUTION
            Rating.GOOD in found -> Rating.GOOD
            else -> Rating.NEUTRAL
        }
    }
}

// One ingredient of a product + what we know about it (info = null if it is not in our list)
data class RatedIngredient(val name: String, val info: IngredientInfo?, val rating: Rating)

// The knowledge base, read ONCE from assets/ingredients.json and then kept in memory
class IngredientKnowledge private constructor(
    private val byAlias: Map<String, IngredientInfo>,
    val sources: Map<String, String>
) {

    fun find(ingredient: String): IngredientInfo? {
        val key = ingredient.lowercase().trim()
        byAlias[key]?.let { return it }
        // "Parfum/Fragrance" or "Mineral Oil/Paraffinum Liquidum" -> try each part
        return key.split("/").firstNotNullOfOrNull { byAlias[it.trim()] }
    }

    // Every ingredient of the product, with its color for this skin type
    fun analyze(product: Product, skinCode: String?): List<RatedIngredient> =
        product.ingredientList().map { name ->
            val info = find(name)
            RatedIngredient(name, info, info?.ratingFor(skinCode) ?: Rating.NEUTRAL)
        }

    companion object {
        @Volatile
        private var instance: IngredientKnowledge? = null

        // Singleton: the file is read only the first time, after that we reuse it
        fun get(context: Context): IngredientKnowledge =
            instance ?: synchronized(this) {
                instance ?: load(context.applicationContext).also { instance = it }
            }

        private fun load(context: Context): IngredientKnowledge {
            val text = context.assets.open("ingredients.json").bufferedReader().use { it.readText() }
            val root = JSONObject(text)

            val sources = mutableMapOf<String, String>()
            val sourcesJson = root.getJSONObject("sources")
            for (key in sourcesJson.keys()) sources[key] = sourcesJson.getString(key)

            val byAlias = mutableMapOf<String, IngredientInfo>()
            val list = root.getJSONArray("ingredients")
            for (i in 0 until list.length()) {
                val obj = list.getJSONObject(i)

                val ratings = mutableMapOf<String, Rating>()
                for (skin in listOf("dry", "oily", "combination", "normal", "sensitive")) {
                    when (obj.optString(skin)) {
                        "good" -> ratings[skin] = Rating.GOOD
                        "caution" -> ratings[skin] = Rating.CAUTION
                        "avoid" -> ratings[skin] = Rating.AVOID
                    }
                }

                val info = IngredientInfo(
                    name = obj.getString("name"),
                    ratings = ratings,
                    why = obj.optString("why"),
                    source = obj.optString("source")
                )
                val aliases = obj.getJSONArray("aliases")
                for (j in 0 until aliases.length()) byAlias[aliases.getString(j)] = info
            }
            return IngredientKnowledge(byAlias, sources)
        }
    }
}