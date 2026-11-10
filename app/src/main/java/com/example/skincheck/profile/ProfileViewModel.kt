package com.example.skincheck.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.skincheck.data.Session
import com.example.skincheck.data.User
import com.example.skincheck.data.UserRepository

// The "brain" behind Profile and the quiz (MVVM)
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    // The logged-in user, read from the database
    val user = MutableLiveData<User?>()

    fun load() {
        val id = Session.userId(getApplication<Application>())
        user.value = if (id == null) null else repository.getUser(id)
    }

    fun saveSkinType(code: String) {
        val id = Session.userId(getApplication<Application>()) ?: return
        repository.updateSkinType(id, code)
    }

    fun logout() {
        Session.clear(getApplication<Application>())
    }
}