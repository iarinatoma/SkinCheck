package com.example.skincheck.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// The app's local SQLite database (a file called skincheck.db on the phone)
class SkinCheckDatabase(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private val appContext = context.applicationContext

    // SQLite ignores foreign keys unless we turn them on
    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    // Runs only once, when the database does not exist yet (fresh install)
    override fun onCreate(db: SQLiteDatabase) {
        createUsersTable(db)
        createProductsTable(db)
        createListTables(db)
    }

    // Runs when DB_VERSION goes up on a phone that already has the database.
    // We only ADD what is new, so existing accounts are kept.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createProductsTable(db)          // already WITH search_text
        if (oldVersion < 3) createListTables(db)
        if (oldVersion in 2..3) addSearchText(db)            // old products table: add the column
    }

    private fun createUsersTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                username      TEXT NOT NULL UNIQUE,
                email         TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                salt          TEXT NOT NULL,
                skin_type     TEXT,
                created_at    INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun createProductsTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE products (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                category        TEXT NOT NULL,
                brand           TEXT NOT NULL,
                name            TEXT NOT NULL,
                price           REAL,
                rating          REAL,
                ingredients     TEXT NOT NULL,
                for_combination INTEGER NOT NULL DEFAULT 0,
                for_dry         INTEGER NOT NULL DEFAULT 0,
                for_normal      INTEGER NOT NULL DEFAULT 0,
                for_oily        INTEGER NOT NULL DEFAULT 0,
                for_sensitive   INTEGER NOT NULL DEFAULT 0,
                search_text     TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent()
        )
        // Fill the new table with the products from assets/cosmetics.csv
        ProductImporter.importInto(db, appContext)
    }

    // Version 3: which products each user opened, and which ones they saved
    private fun createListTables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE history (
                user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                viewed_at  INTEGER NOT NULL,
                PRIMARY KEY (user_id, product_id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE favorites (
                user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                added_at   INTEGER NOT NULL,
                PRIMARY KEY (user_id, product_id)
            )
            """.trimIndent()
        )
    }

    // Version 4: a search column without accents, filled for the products we already have
    private fun addSearchText(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE products ADD COLUMN search_text TEXT NOT NULL DEFAULT ''")
        db.rawQuery("SELECT id, brand, name FROM products", null).use { c ->
            while (c.moveToNext()) {
                val values = ContentValues().apply {
                    put("search_text", SearchText.normalize(c.getString(1) + " " + c.getString(2)))
                }
                db.update("products", values, "id = ?", arrayOf(c.getLong(0).toString()))
            }
        }
    }

    companion object {
        const val DB_NAME = "skincheck.db"
        // 1: users, 2: + products, 3: + history, favorites, 4: + products.search_text
        const val DB_VERSION = 4
    }
}