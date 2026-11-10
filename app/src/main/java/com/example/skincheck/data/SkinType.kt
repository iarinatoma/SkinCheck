package com.example.skincheck.data

import android.content.Context
import com.example.skincheck.R

// In the database we store a short code, e.g. "combination,sensitive".
// This turns it into a readable text in the phone's language.
object SkinType {

    fun displayName(context: Context, code: String?): String? {
        if (code.isNullOrBlank()) return null
        val parts = code.split(",")
        var name = context.getString(
            when (parts[0]) {
                "dry" -> R.string.skin_dry
                "oily" -> R.string.skin_oily
                "combination" -> R.string.skin_combination
                else -> R.string.skin_normal
            }
        )
        if ("sensitive" in parts) {
            name += ", " + context.getString(R.string.skin_sensitive)
        }
        return name
    }
}