package com.example.skincheck.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.skincheck.data.Product
import com.example.skincheck.data.ProductRepository
import com.example.skincheck.data.Session
import com.example.skincheck.data.UserRepository

// The "brain" behind the product page (MVVM)
class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val products = ProductRepository(application)
    private val users = UserRepository(application)

    val product = MutableLiveData<Product?>()

    // The user's skin type, e.g. "normal" (null = quiz not taken)
    var skinCode: String? = null
        private set

    fun load(productId: Long) {
        val userId = Session.userId(getApplication<Application>())
        skinCode = userId?.let { users.getUser(it)?.skinType }
        product.value = products.getById(productId)
    }
}