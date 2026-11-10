package com.example.skincheck.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.skincheck.data.AuthResult
import com.example.skincheck.data.UserRepository

// The "brain" behind the Login and Register screens (MVVM):
// the screen asks the ViewModel, the ViewModel asks the Repository.
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    // The screen "watches" this value and reacts when it changes
    val result = MutableLiveData<AuthResult?>()

    fun login(email: String, password: String) {
        result.value = repository.login(email, password)
    }

    fun register(username: String, email: String, password: String) {
        result.value = repository.register(username, email, password)
    }

    // Called after the screen has shown the result, so it is not shown twice
    fun consume() {
        result.value = null
    }
}