package com.example.skincheck

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // The "Scan a product" button switches to the Scan tab
        view.findViewById<MaterialButton>(R.id.btn_scan).setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
                .selectedItemId = R.id.nav_scan
        }
    }
}

class HistoryFragment : Fragment(R.layout.fragment_history)
class ScanFragment : Fragment(R.layout.fragment_scan)
class CommunityFragment : Fragment(R.layout.fragment_community)
