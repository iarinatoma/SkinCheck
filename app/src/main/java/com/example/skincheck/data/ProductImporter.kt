package com.example.skincheck.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

// Reads assets/cosmetics.csv (Kaggle "Cosmetics datasets", GPL 2) into the products table
object ProductImporter {

    fun importInto(db: SQLiteDatabase, context: Context) {
        val text = context.assets.open("cosmetics.csv")
            .bufferedReader().use { it.readText() }
            .removePrefix("\uFEFF") // invisible marker some CSV files start with

        val rows = parseCsv(text)
        if (rows.isEmpty()) return

        // Find each column by its name in the first row (the header)
        val header = rows[0].map { it.trim() }
        fun col(name: String) = header.indexOf(name)
        val iCategory = col("Label")
        val iBrand = col("Brand")
        val iName = col("Name")
        val iPrice = col("Price")
        val iRating = col("Rank")
        val iIngredients = col("Ingredients")
        val iCombination = col("Combination")
        val iDry = col("Dry")
        val iNormal = col("Normal")
        val iOily = col("Oily")
        val iSensitive = col("Sensitive")

        // One transaction = all 1472 rows are written in one go (much faster)
        db.beginTransaction()
        try {
            for (row in rows.drop(1)) {
                if (row.size < header.size) continue // skip broken or empty lines
                // Skip products without a real ingredient list ("No Info", "#NAME?", ...)
                val ingredients = row[iIngredients].trim()
                if (ingredients.length < 15 ) continue
                val values = ContentValues().apply {
                    put("category", row[iCategory].trim())
                    put("brand", row[iBrand].trim())
                    put("name", row[iName].trim())
                    put("price", row[iPrice].trim().toDoubleOrNull())
                    put("rating", row[iRating].trim().toDoubleOrNull())
                    put("ingredients", row[iIngredients].trim())
                    put("for_combination", row[iCombination].trim().toIntOrNull() ?: 0)
                    put("for_dry", row[iDry].trim().toIntOrNull() ?: 0)
                    put("for_normal", row[iNormal].trim().toIntOrNull() ?: 0)
                    put("for_oily", row[iOily].trim().toIntOrNull() ?: 0)
                    put("for_sensitive", row[iSensitive].trim().toIntOrNull() ?: 0)
                }
                db.insert("products", null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // Splits CSV text into rows and fields.
    // Handles fields in "quotes" that contain commas, and "" inside quotes.
    fun parseCsv(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < text.length) {
            val c = text[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length && text[i + 1] == '"') {
                        field.append('"') // "" means one real quote character
                        i++
                    } else {
                        inQuotes = false // closing quote
                    }
                } else {
                    field.append(c)
                }
            } else {
                when (c) {
                    '"' -> inQuotes = true
                    ',' -> { row.add(field.toString()); field.clear() }
                    '\n' -> {
                        row.add(field.toString()); field.clear()
                        rows.add(row); row = mutableListOf()
                    }
                    '\r' -> { /* Windows line ending, ignore */ }
                    else -> field.append(c)
                }
            }
            i++
        }
        // Last line (if the file does not end with a new line)
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            rows.add(row)
        }
        return rows
    }
}