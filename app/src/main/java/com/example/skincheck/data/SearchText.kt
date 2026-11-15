package com.example.skincheck.data

import java.text.Normalizer

// Text used only for searching: no accents, lowercase, no punctuation.
// "LANCÔME Crème" -> "lancome creme", "KIEHL'S" -> "kiehls", "Dr. Jart+" -> "dr jart"
object SearchText {

    fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)   // "ô" -> "o" + "^" (separate mark)
            .replace(Regex("\\p{Mn}+"), "")               // remove the marks -> "o"
            .lowercase()
            .replace("'", "").replace("’", "")            // "kiehl's" -> "kiehls"
            .replace(Regex("[^a-z0-9]+"), " ")            // other signs -> space
            .trim()
}