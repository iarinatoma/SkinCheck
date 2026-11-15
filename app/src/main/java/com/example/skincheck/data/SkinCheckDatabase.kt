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
        createCommunityTable(db)
        SephoraImporter.importInto(db, appContext)
    }

    // Runs when DB_VERSION goes up on a phone that already has the database.
    // We only ADD what is new, so existing accounts are kept.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createProductsTable(db)          // already with all new columns
        if (oldVersion < 3) createListTables(db)
        if (oldVersion in 2..3) addSearchText(db)
        if (oldVersion in 2..4) addSephoraColumns(db)
        if (oldVersion < 5) {
            createCommunityTable(db)
            SephoraImporter.importInto(db, appContext)
        }
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
                search_text     TEXT NOT NULL DEFAULT '',
                sephora_id      TEXT,
                loves           INTEGER NOT NULL DEFAULT 0,
                reviews         INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_products_search_text ON products(search_text)")
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

    // Version 5: popularity from Sephora 2023 + an index that makes search_text lookups fast
    private fun addSephoraColumns(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE products ADD COLUMN sephora_id TEXT")
        db.execSQL("ALTER TABLE products ADD COLUMN loves INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE products ADD COLUMN reviews INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_products_search_text ON products(search_text)")
    }

    // Version 5: what people with each skin type think of a product (from 870,000+ reviews)
    private fun createCommunityTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE community_ratings (
                product_id      INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                skin_type       TEXT NOT NULL,
                reviews         INTEGER NOT NULL,
                avg_rating      REAL,
                recommended_pct INTEGER,
                PRIMARY KEY (product_id, skin_type)
            )
            """.trimIndent()
        )
    }

    companion object {
        const val DB_NAME = "skincheck.db"
        // 1: users, 2: + products, 3: + history, favorites, 4: + search_text,
        // 5: + Sephora 2023 products, popularity and community ratings
        const val DB_VERSION = 5
    }
}