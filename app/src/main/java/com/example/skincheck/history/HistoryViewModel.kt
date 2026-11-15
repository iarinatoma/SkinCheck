package com.example.skincheck.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.skincheck.data.Product
import com.example.skincheck.data.ProductRepository
import com.example.skincheck.data.Session
import com.example.skincheck.data.UserRepository

// The "brain" behind "My products" (MVVM)
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val products = ProductRepository(application)
    private val users = UserRepository(application)
    private val userId = Session.userId(application)

    // false = "Recently viewed" tab, true = "My list" tab
    var showingMyList = false
        private set

    val items = MutableLiveData<List<Product>>()

    fun skinCode(): String? = userId?.let { users.getUser(it)?.skinType }

    fun showTab(myList: Boolean) {
        showingMyList = myList
        refresh()
    }

    fun refresh() {
        val id = userId ?: return
        items.value = if (showingMyList) products.favorites(id) else products.recentlyViewed(id)
    }

    fun clearHistory() {
        val id = userId ?: return
        products.clearHistory(id)
        refresh()
    }
}