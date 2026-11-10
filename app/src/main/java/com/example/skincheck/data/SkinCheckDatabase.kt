package com.example.skincheck.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// The app's local SQLite database (a file called skincheck.db on the phone)
class SkinCheckDatabase(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    // Runs only once, the first time the database is created
    override fun onCreate(db: SQLiteDatabase) {
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

    // Runs when DB_VERSION goes up (for now we simply start fresh)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    companion object {
        const val DB_NAME = "skincheck.db"
        const val DB_VERSION = 1
    }
}