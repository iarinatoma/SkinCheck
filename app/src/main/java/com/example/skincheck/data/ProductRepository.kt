package com.example.skincheck.data

import android.content.Context
import android.database.Cursor

// The only place in the app that talks to the "products" table
class ProductRepository(context: Context) {

    private val db = SkinCheckDatabase(context.applicationContext)

    private val columns =
        "id, category, brand, name, price, rating, ingredients, " +
                "for_combination, for_dry, for_normal, for_oily, for_sensitive"

    // Search by product name or brand, best rated first
    fun search(query: String, limit: Int = 50): List<Product> {
        val pattern = "%${query.trim()}%"
        val cursor = db.readableDatabase.rawQuery(
            "SELECT $columns FROM products " +
                    "WHERE name LIKE ? OR brand LIKE ? " +
                    "ORDER BY rating DESC LIMIT $limit",
            arrayOf(pattern, pattern)
        )
        return cursor.use { c -> buildList { while (c.moveToNext()) add(c.toProduct()) } }
    }

    fun getById(id: Long): Product? {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT $columns FROM products WHERE id = ?", arrayOf(id.toString())
        )
        return cursor.use { c -> if (c.moveToFirst()) c.toProduct() else null }
    }

    fun count(): Int {
        val cursor = db.readableDatabase.rawQuery("SELECT COUNT(*) FROM products", null)
        return cursor.use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
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