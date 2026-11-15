package com.example.skincheck.data

// The ways the product list can be sorted. "sql" is what goes after ORDER BY.
enum class SortOrder(val sql: String) {
    TOP_RATED("rating DESC, name"),
    PRICE_LOW("price IS NULL, price ASC, name"),   // products without a price go last
    PRICE_HIGH("price IS NULL, price DESC, name")
}