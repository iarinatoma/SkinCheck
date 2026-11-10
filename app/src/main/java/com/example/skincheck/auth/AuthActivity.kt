package com.example.skincheck.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.skincheck.MainActivity
import com.example.skincheck.R
import com.example.skincheck.data.Session
import com.example.skincheck.data.User

// The activity that shows Login / Register before the main app
class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)

        // Keep space for the status bar, navigation bar and keyboard
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.auth_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, keyboard.bottom))
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_container, LoginFragment())
                .commit()
        }
    }

    fun showRegister() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_container, RegisterFragment())
            .addToBackStack(null)
            .commit()
    }

    fun showLogin() {
        supportFragmentManager.popBackStack()
    }

    // Called by Login or Register when it worked
    fun onLoggedIn(user: User) {
        Session.save(this, user.id)
        startActivity(Intent(this, MainActivity::class.java))
        finish() // closes this activity, so "back" does not return to Login
    }
}