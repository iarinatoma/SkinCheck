package com.example.skincheck.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.skincheck.data.CommunityRating
import com.example.skincheck.data.IngredientKnowledge
import com.example.skincheck.data.Product
import com.example.skincheck.data.ProductAnalysis
import com.example.skincheck.data.ProductRepository
import com.example.skincheck.data.Session
import com.example.skincheck.data.UserRepository

// The "brain" behind the product page (MVVM)
class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val products = ProductRepository(application)
    private val users = UserRepository(application)
    private val knowledge = IngredientKnowledge.get(application)
    private val userId = Session.userId(application)

    val product = MutableLiveData<Product?>()
    val analysis = MutableLiveData<ProductAnalysis?>()                     // SkinCheck recommends
    val community = MutableLiveData<List<CommunityRating>>(emptyList())   // Community recommends
    val isFavorite = MutableLiveData(false)

    // The user's skin type, e.g. "normal" (null = quiz not taken)
    var skinCode: String? = null
        private set

    fun load(productId: Long) {
        skinCode = userId?.let { users.getUser(it)?.skinType }
        val loaded = products.getById(productId)
        product.value = loaded
        analysis.value = loaded?.let { ProductAnalysis(knowledge.analyze(it, skinCode)) }
        community.value = loaded?.let { products.community(it.id) } ?: emptyList()

        if (userId != null && loaded != null) {
            products.markViewed(userId, loaded.id)          // goes to "Recently viewed"
            isFavorite.value = products.isFavorite(userId, loaded.id)
        }
    }

    // ♡ pressed: save if not saved, remove if saved
    fun toggleFavorite() {
        val id = userId ?: return
        val current = product.value ?: return
        val newValue = isFavorite.value != true
        products.setFavorite(id, current.id, newValue)
        isFavorite.value = newValue
    }
}