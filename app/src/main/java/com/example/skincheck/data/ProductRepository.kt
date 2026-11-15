package com.example.skincheck.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

// The only place in the app that talks to the "products", "history" and "favorites" tables
class ProductRepository(context: Context) {

    private val db = SkinCheckDatabase(context.applicationContext)

    // "products.id" (not just "id") because some queries JOIN other tables too
    private val columns =
        "products.id, category, brand, name, price, rating, ingredients, " +
                "for_combination, for_dry, for_normal, for_oily, for_sensitive"

    // ---------- Products ----------

    // Search by name or brand, with sorting and an optional "only for my skin" filter
    fun search(
        query: String,
        sort: SortOrder = SortOrder.TOP_RATED,
        skinCode: String? = null,
        limit: Int = 50
    ): List<Product> {
        val pattern = "%${query.trim()}%"
        return queryProducts(
            "SELECT $columns FROM products ${where(skinCode)} ORDER BY ${sort.sql} LIMIT $limit",
            pattern, pattern
        )
    }

    // How many products match in total (the list itself shows at most 50)
    fun countMatching(query: String, skinCode: String? = null): Int {
        val pattern = "%${query.trim()}%"
        val cursor = db.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM products ${where(skinCode)}", arrayOf(pattern, pattern)
        )
        return cursor.use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
    }

    fun getById(id: Long): Product? =
        queryProducts("SELECT $columns FROM products WHERE products.id = ?", id.toString())
            .firstOrNull()

    fun count(): Int {
        val cursor = db.readableDatabase.rawQuery("SELECT COUNT(*) FROM products", null)
        return cursor.use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
    }

    // ---------- Recently viewed (table "history") ----------

    // Opening a product again only updates the time (thanks to the PRIMARY KEY + REPLACE)
    fun markViewed(userId: Long, productId: Long) {
        val values = ContentValues().apply {
            put("user_id", userId)
            put("product_id", productId)
            put("viewed_at", System.currentTimeMillis())
        }
        db.writableDatabase.insertWithOnConflict(
            "history", null, values, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun recentlyViewed(userId: Long, limit: Int = 30): List<Product> = queryProducts(
        "SELECT $columns FROM products " +
                "JOIN history ON history.product_id = products.id " +
                "WHERE history.user_id = ? " +
                "ORDER BY history.viewed_at DESC LIMIT $limit",
        userId.toString()
    )

    fun clearHistory(userId: Long) {
        db.writableDatabase.delete("history", "user_id = ?", arrayOf(userId.toString()))
    }

    // ---------- My list (table "favorites") ----------

    fun isFavorite(userId: Long, productId: Long): Boolean {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT 1 FROM favorites WHERE user_id = ? AND product_id = ?",
            arrayOf(userId.toString(), productId.toString())
        )
        return cursor.use { it.moveToFirst() }
    }

    fun setFavorite(userId: Long, productId: Long, favorite: Boolean) {
        if (favorite) {
            val values = ContentValues().apply {
                put("user_id", userId)
                put("product_id", productId)
                put("added_at", System.currentTimeMillis())
            }
            db.writableDatabase.insertWithOnConflict(
                "favorites", null, values, SQLiteDatabase.CONFLICT_IGNORE
            )
        } else {
            db.writableDatabase.delete(
                "favorites", "user_id = ? AND product_id = ?",
                arrayOf(userId.toString(), productId.toString())
            )
        }
    }

    fun favorites(userId: Long): List<Product> = queryProducts(
        "SELECT $columns FROM products " +
                "JOIN favorites ON favorites.product_id = products.id " +
                "WHERE favorites.user_id = ? " +
                "ORDER BY favorites.added_at DESC",
        userId.toString()
    )

    // ---------- Helpers ----------

    // The WHERE part shared by search() and countMatching().
    // Column names come from OUR fixed list, never from what the user typed -> no SQL injection.
    private fun where(skinCode: String?): String {
        var sql = "WHERE (name LIKE ? OR brand LIKE ?)"
        if (!skinCode.isNullOrBlank()) {
            val parts = skinCode.split(",")
            val column = when (parts[0]) {
                "dry" -> "for_dry"
                "oily" -> "for_oily"
                "combination" -> "for_combination"
                else -> "for_normal"
            }
            sql += " AND $column = 1"
            if ("sensitive" in parts) sql += " AND for_sensitive = 1"
        }
        return sql
    }

    // Runs a SELECT and turns every row into a Product
    private fun queryProducts(sql: String, vararg args: String): List<Product> {
        val cursor = db.readableDatabase.rawQuery(sql, args)
        return cursor.use { c -> buildList { while (c.moveToNext()) add(c.toProduct()) } }
    }

    // Turns the current database row into a Product object
    private fun Cursor.toProduct(): Product {
        val suitable = mutableSetOf<String>()
        if (getInt(7) == 1) suitable.add("combination")
        if (getInt(8) == 1) suitable.add("dry")
        if (getInt(9) == 1) suitable.add("normal")
        if (getInt(10) == 1) suitable.add("oily")
        if (getInt(11) == 1) suitable.add("sensitive")
        return Product(
            id = getLong(0),
            category = getString(1),
            brand = getString(2),
            name = getString(3),
            price = if (isNull(4)) null else getDouble(4),
            rating = if (isNull(5)) null else getDouble(5),
            ingredients = getString(6),
            suitableFor = suitable
        )
    }
}