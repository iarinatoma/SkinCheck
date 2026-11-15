package com.example.skincheck.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.skincheck.data.Product
import com.example.skincheck.data.ProductRepository
import com.example.skincheck.data.Session
import com.example.skincheck.data.SortOrder
import com.example.skincheck.data.UserRepository

// The "brain" behind the search screen (MVVM)
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val products = ProductRepository(application)
    private val users = UserRepository(application)

    // The logged-in user's skin type, e.g. "normal" (null = quiz not taken)
    val skinCode: String? = Session.userId(application)?.let { users.getUser(it)?.skinType }

    // How many products are in the database (read once)
    val totalCount: Int by lazy { products.count() }

    // What the user chose on the screen (kept when coming back from a product page)
    var query = ""
        private set
    var sort = SortOrder.TOP_RATED
        private set
    var onlyForMe = false
        private set

    // How many products match in total (the list shows at most 50)
    var totalMatches = 0
        private set

    // The screen watches this list and redraws when it changes
    val results = MutableLiveData<List<Product>>()

    fun search(newQuery: String) {
        query = newQuery.trim()
        refresh()
    }

    fun sortBy(newSort: SortOrder) {
        sort = newSort
        refresh()
    }

    fun showOnlyForMe(only: Boolean) {
        onlyForMe = only
        refresh()
    }

    private fun refresh() {
        val skin = if (onlyForMe) skinCode else null
        totalMatches = products.countMatching(query, skin)
        results.value = products.search(query, sort, skin)
    }
}