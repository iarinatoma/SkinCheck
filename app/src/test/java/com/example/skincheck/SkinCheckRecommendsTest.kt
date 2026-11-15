package com.example.skincheck

import com.example.skincheck.data.IngredientInfo
import com.example.skincheck.data.ProductAnalysis
import com.example.skincheck.data.RatedIngredient
import com.example.skincheck.data.Rating
import com.example.skincheck.data.Verdict
import org.junit.Assert.assertEquals
import org.junit.Test

// Automatic checks for the rules of "SkinCheck recommends"
class SkinCheckRecommendsTest {

    private val fragrance = IngredientInfo(
        "Fragrance", mapOf("normal" to Rating.CAUTION, "sensitive" to Rating.AVOID), "", ""
    )
    private val glycerin = IngredientInfo(
        "Glycerin", mapOf("normal" to Rating.GOOD, "sensitive" to Rating.GOOD), "", ""
    )

    @Test
    fun worstRatingCountsForCombinedSkinTypes() {
        assertEquals(Rating.CAUTION, fragrance.ratingFor("normal"))
        assertEquals(Rating.AVOID, fragrance.ratingFor("normal,sensitive"))
    }

    @Test
    fun skinTypeNotInTheListIsNeutral() {
        assertEquals(Rating.NEUTRAL, fragrance.ratingFor("oily"))
    }

    @Test
    fun oneAvoidMakesTheProductNotRecommended() {
        val analysis = ProductAnalysis(
            listOf(
                RatedIngredient("Glycerin", glycerin, Rating.GOOD),
                RatedIngredient("Fragrance", fragrance, Rating.AVOID)
            )
        )
        assertEquals(Verdict.NOT_RECOMMENDED, analysis.verdict)
    }

    @Test
    fun onlyGoodIngredientsMeanRecommended() {
        val analysis = ProductAnalysis(listOf(RatedIngredient("Glycerin", glycerin, Rating.GOOD)))
        assertEquals(Verdict.RECOMMENDED, analysis.verdict)
    }

    @Test
    fun noKnownIngredientsMeanUnknown() {
        val analysis = ProductAnalysis(listOf(RatedIngredient("Water", null, Rating.NEUTRAL)))
        assertEquals(Verdict.UNKNOWN, analysis.verdict)
    }
}