package com.example.skincheck

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast

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
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val skinGroups = listOf(R.id.rg_q1, R.id.rg_q2, R.id.rg_q3, R.id.rg_q4)
            .map { view.findViewById<RadioGroup>(it) }
        val sensitiveGroup = view.findViewById<RadioGroup>(R.id.rg_q5)
        val resultText = view.findViewById<TextView>(R.id.txt_skin_type)

        view.findViewById<MaterialButton>(R.id.btn_calculate).setOnClickListener {
            // 1. Read the hidden tag of the chosen answer in each question
            val answers = skinGroups.map { group ->
                view.findViewById<RadioButton>(group.checkedRadioButtonId)?.tag as String?
            }
            val sensitiveAnswer =
                view.findViewById<RadioButton>(sensitiveGroup.checkedRadioButtonId)?.tag as String?

            // 2. Check that every question has an answer
            if (answers.contains(null) || sensitiveAnswer == null) {
                Toast.makeText(requireContext(), R.string.quiz_incomplete, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. The skin type that was chosen most often wins
            val skinType = answers.filterNotNull().groupingBy { it }.eachCount().maxBy { it.value }.key

            // 4. Turn the tag into a readable text
            var typeName = when (skinType) {
                "dry" -> getString(R.string.skin_dry)
                "oily" -> getString(R.string.skin_oily)
                "combination" -> getString(R.string.skin_combination)
                else -> getString(R.string.skin_normal)
            }
            if (sensitiveAnswer == "yes") {
                typeName += ", " + getString(R.string.skin_sensitive)
            }

            // 5. Show the result
            resultText.text = getString(R.string.skin_type_result, typeName)
        }
    }
}