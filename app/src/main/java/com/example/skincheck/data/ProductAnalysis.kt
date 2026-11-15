package com.example.skincheck.data

// The overall result shown in the "SkinCheck recommends" card
enum class Verdict { RECOMMENDED, CAREFUL, NOT_RECOMMENDED, UNKNOWN }

// "SkinCheck recommends" for one product and one skin type
data class ProductAnalysis(val ingredients: List<RatedIngredient>) {

    val good = ingredients.count { it.rating == Rating.GOOD }
    val caution = ingredients.count { it.rating == Rating.CAUTION }
    val avoid = ingredients.count { it.rating == Rating.AVOID }

    // One "avoid" is enough for a red verdict, one "caution" for a yellow one
    val verdict: Verdict
        get() = when {
            avoid > 0 -> Verdict.NOT_RECOMMENDED
            caution > 0 -> Verdict.CAREFUL
            good > 0 -> Verdict.RECOMMENDED
            else -> Verdict.UNKNOWN
        }

    // Known ingredients grouped by knowledge-base entry, worst first.
    // e.g. "Fragrance allergens (EU list)" -> [Linalool, Limonene]
    fun groups(): List<Pair<IngredientInfo, List<RatedIngredient>>> =
        ingredients
            .filter { it.info != null }
            .groupBy { it.info!! }
            .toList()
            .sortedBy { (_, items) -> severity(items.first().rating) }

    private fun severity(rating: Rating) = when (rating) {
        Rating.AVOID -> 0
        Rating.CAUTION -> 1
        Rating.GOOD -> 2
        Rating.NEUTRAL -> 3
    }
}