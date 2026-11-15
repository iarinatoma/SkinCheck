package com.example.skincheck.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.skincheck.data.Product
import com.example.skincheck.data.ProductRepository
import com.example.skincheck.data.Session
import com.example.skincheck.data.UserRepository

// The "brain" behind the search screen (MVVM)
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val products = ProductRepository(application)
    private val users = UserRepository(application)

    // The screen watches this list and redraws when it changes
    val results = MutableLiveData<List<Product>>()

    // How many products are in the database (read once)
    val totalCount: Int by lazy { products.count() }

    fun search(query: String) {
        results.value = products.search(query)
    }

    // The logged-in user's skin type, or null if the quiz was not taken
    fun skinCode(): String? {
        val id = Session.userId(getApplication<Application>()) ?: return null
        return users.getUser(id)?.skinType
    }
}