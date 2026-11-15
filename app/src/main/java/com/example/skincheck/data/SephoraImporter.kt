package com.example.skincheck.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

// Reads assets/sephora_products.csv and assets/community.csv
// (Kaggle "Sephora Products and Skincare Reviews", CC BY 4.0, prepared in our Kaggle notebook)
object SephoraImporter {

    fun importInto(db: SQLiteDatabase, context: Context) {
        db.beginTransaction()
        try {
            val localIds = importProducts(db, context)   // Sephora id ("P107306") -> our id
            importCommunity(db, context, localIds)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun readCsv(context: Context, file: String): List<List<String>> {
        val text = context.assets.open(file).bufferedReader().use { it.readText() }
            .removePrefix("\uFEFF")
        return ProductImporter.parseCsv(text)
    }

    private fun importProducts(db: SQLiteDatabase, context: Context): Map<String, Long> {
        val rows = readCsv(context, "sephora_products.csv")
        if (rows.isEmpty()) return emptyMap()

        val header = rows[0].map { it.trim() }
        fun col(name: String) = header.indexOf(name)
        val iId = col("product_id")
        val iBrand = col("brand")
        val iName = col("name")
        val iCategory = col("category")
        val iPrice = col("price")
        val iRating = col("rating")
        val iReviews = col("reviews")
        val iLoves = col("loves")
        val iIngredients = col("ingredients")

        val localIds = mutableMapOf<String, Long>()
        for (row in rows.drop(1)) {
            if (row.size < header.size) continue

            val sephoraId = row[iId].trim()
            val brand = row[iBrand].trim()
            val name = row[iName].trim()
            val searchText = SearchText.normalize("$brand $name")
            val loves = row[iLoves].trim().toIntOrNull() ?: 0
            val reviews = row[iReviews].trim().toIntOrNull() ?: 0

            val existing = findBySearchText(db, searchText)
            if (existing != null) {
                // Same product already came from cosmetics.csv -> only add the new information
                val values = ContentValues().apply {
                    put("sephora_id", sephoraId)
                    put("loves", loves)
                    put("reviews", reviews)
                }
                db.update("products", values, "id = ?", arrayOf(existing.toString()))
                localIds[sephoraId] = existing
            } else {
                val values = ContentValues().apply {
                    put("category", row[iCategory].trim())
                    put("brand", brand)
                    put("name", name)
                    put("price", row[iPrice].trim().toDoubleOrNull())
                    put("rating", row[iRating].trim().toDoubleOrNull())
                    put("ingredients", row[iIngredients].trim())
                    put("search_text", searchText)
                    put("sephora_id", sephoraId)
                    put("loves", loves)
                    put("reviews", reviews)
                    // for_dry, for_oily, ... stay 0: this dataset has no skin types from the brand
                }
                val newId = db.insert("products", null, values)
                if (newId != -1L) localIds[sephoraId] = newId
            }
        }
        return localIds
    }

    private fun findBySearchText(db: SQLiteDatabase, searchText: String): Long? =
        db.rawQuery("SELECT id FROM products WHERE search_text = ? LIMIT 1", arrayOf(searchText))
            .use { c -> if (c.moveToFirst()) c.getLong(0) else null }

    private fun importCommunity(db: SQLiteDatabase, context: Context, localIds: Map<String, Long>) {
        val rows = readCsv(context, "community.csv")
        if (rows.isEmpty()) return

        val header = rows[0].map { it.trim() }
        fun col(name: String) = header.indexOf(name)
        val iId = col("product_id")
        val iSkin = col("skin_type")
        val iReviews = col("reviews")
        val iAvg = col("avg_rating")
        val iPct = col("recommended_pct")

        for (row in rows.drop(1)) {
            if (row.size < header.size) continue
            val productId = localIds[row[iId].trim()] ?: continue
            val values = ContentValues().apply {
                put("product_id", productId)
                put("skin_type", row[iSkin].trim())
                put("reviews", row[iReviews].trim().toIntOrNull() ?: 0)
                put("avg_rating", row[iAvg].trim().toDoubleOrNull())
                put("recommended_pct", row[iPct].trim().toIntOrNull())
            }
            db.insertWithOnConflict("community_ratings", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }
}