package com.example.skincheck.data

// Turns the raw ingredient text from the dataset into a clean list of names.
// The Sephora data is messy: marketing sentences, "Water (Aqua)", "Fragrance**", "1,2-Hexanediol"...
object IngredientParser {

    fun parse(raw: String): List<String> {
        val result = mutableListOf<String>()
        // Split at commas, but NOT inside chemical names like "1,2-Hexanediol"
        for (piece in raw.split(Regex(",(?![0-9])"))) {
            // A piece can still hold several sentences: "Propylparaben. Step II: Water"
            for (part in piece.split(Regex("\\.\\s+"))) {
                val name = cleanName(part) ?: continue
                result.add(name)
            }
        }
        return result
    }

    private fun cleanName(part: String): String? {
        var p = part.trim()
        if (p.isEmpty()) return null

        // Marketing bullets look like "-Hyaluronic Acid: moisturizes the skin" -> skip
        if (p.startsWith("-") && ':' in p) return null

        // "EGF Botanical Complex: Water" -> "Water"
        if (':' in p) p = p.substringAfterLast(':')

        p = p.replace(Regex("\\([^)]*\\)"), "")   // "Water (Aqua)" -> "Water"
            .replace(Regex("[*+]"), "")           // "Fragrance**" -> "Fragrance"
            .replace(Regex("\\s+"), " ")
            .trim().trimEnd('.').trim()

        // Whole sentences are not ingredient names
        if (p.isEmpty() || p.length > 60 || p.split(' ').size > 6) return null
        return p
    }
}