package com.example.skincheck.profile

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.skincheck.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class QuizFragment : Fragment(R.layout.fragment_quiz) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Back arrow in the top bar
        view.findViewById<MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val skinGroups = listOf(R.id.rg_q1, R.id.rg_q2, R.id.rg_q3, R.id.rg_q4)
            .map { view.findViewById<RadioGroup>(it) }
        val sensitiveGroup = view.findViewById<RadioGroup>(R.id.rg_q5)

        view.findViewById<MaterialButton>(R.id.btn_calculate).setOnClickListener {
            // 1. Read the hidden tag of each chosen answer
            val answers = skinGroups.map { group ->
                view.findViewById<RadioButton>(group.checkedRadioButtonId)?.tag as String?
            }
            val sensitiveAnswer =
                view.findViewById<RadioButton>(sensitiveGroup.checkedRadioButtonId)?.tag as String?

            // 2. Every question must have an answer
            if (answers.contains(null) || sensitiveAnswer == null) {
                Toast.makeText(requireContext(), R.string.quiz_incomplete, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. The skin type chosen most often wins
            val skinType = answers.filterNotNull().groupingBy { it }.eachCount().maxBy { it.value }.key

            // 4. Build the code for the database, e.g. "oily" or "oily,sensitive"
            val code = if (sensitiveAnswer == "yes") "$skinType,sensitive" else skinType

            // 5. Save it in the user's row and go back to Profile
            viewModel.saveSkinType(code)
            Toast.makeText(requireContext(), R.string.quiz_saved, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
}
