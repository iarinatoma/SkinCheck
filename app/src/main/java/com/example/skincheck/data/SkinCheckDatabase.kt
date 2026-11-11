package com.example.skincheck.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// The app's local SQLite database (a file called skincheck.db on the phone)
class SkinCheckDatabase(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private val appContext = context.applicationContext

    // Runs only once, when the database does not exist yet (fresh install)
    override fun onCreate(db: SQLiteDatabase) {
        createUsersTable(db)
        createProductsTable(db)
    }

    // Runs when DB_VERSION goes up on a phone that already has the database.
    // We only ADD what is new, so existing accounts are kept.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createProductsTable(db)
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
                for_sensitive   INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        // Fill the new table with the products from assets/cosmetics.csv
        ProductImporter.importInto(db, appContext)
    }

    companion object {
        const val DB_NAME = "skincheck.db"
        const val DB_VERSION = 2 // was 1; going up makes onUpgrade run
    }
}