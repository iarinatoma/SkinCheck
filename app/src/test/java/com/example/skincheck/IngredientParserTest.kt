package com.example.skincheck

import com.example.skincheck.data.IngredientParser
import org.junit.Assert.assertEquals
import org.junit.Test

// Automatic checks for the ingredient cleaner (run: right click -> Run)
class IngredientParserTest {

    @Test
    fun splitsAtCommas() {
        assertEquals(
            listOf("Water", "Glycerin", "Niacinamide"),
            IngredientParser.parse("Water, Glycerin, Niacinamide")
        )
    }

    @Test
    fun keepsCommasInsideChemicalNames() {
        assertEquals(
            listOf("Water", "1,2-Hexanediol"),
            IngredientParser.parse("Water, 1,2-Hexanediol")
        )
    }

    @Test
    fun removesBracketsAndStars() {
        assertEquals(
            listOf("Water", "Fragrance"),
            IngredientParser.parse("Water (Aqua), Fragrance**")
        )
    }

    @Test
    fun skipsMarketingText() {
        val raw = "-Hyaluronic Acid: Moisturizes the skin. EGF Complex: Water, Glycerin"
        assertEquals(listOf("Water", "Glycerin"), IngredientParser.parse(raw))
    }

    @Test
    fun skipsWholeSentences() {
        val raw = "Glycerin, Please be aware that ingredient lists may change from time to time"
        assertEquals(listOf("Glycerin"), IngredientParser.parse(raw))
    }
}