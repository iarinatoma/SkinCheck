package com.example.skincheck

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.skincheck.auth.AuthActivity
import com.example.skincheck.data.Session
import com.example.skincheck.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Not logged in? Go to the Login screen instead
        if (Session.userId(this) == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Leave space for the status bar at the top
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }

        // When a bottom button is pressed, show the matching screen
        val nav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_history -> show(HistoryFragment())
                R.id.nav_scan -> show(ScanFragment())
                R.id.nav_community -> show(CommunityFragment())
                R.id.nav_profile -> show(ProfileFragment())
                else -> show(HomeFragment())
            }
            true
        }

        // Back: 1) close an opened screen (e.g. quiz)  2) go to Home  3) close the app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    supportFragmentManager.backStackEntryCount > 0 ->
                        supportFragmentManager.popBackStack()
                    nav.selectedItemId != R.id.nav_home ->
                        nav.selectedItemId = R.id.nav_home
                    else -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })

        // The first time the app opens, show Home
        if (savedInstanceState == null) show(HomeFragment())
    }

    // Shows a main tab screen (and forgets any screens opened on top)
    private fun show(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Opens a screen ON TOP of the current one; back returns to the previous screen
    fun open(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}