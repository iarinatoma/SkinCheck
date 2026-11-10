package com.example.skincheck.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.skincheck.MainActivity
import com.example.skincheck.R
import com.example.skincheck.auth.AuthActivity
import com.example.skincheck.data.SkinType
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var viewModel: ProfileViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        val avatar = view.findViewById<TextView>(R.id.txt_avatar)
        val username = view.findViewById<TextView>(R.id.txt_username)
        val email = view.findViewById<TextView>(R.id.txt_email)
        val mySkin = view.findViewById<TextView>(R.id.txt_my_skin)
        val quizButton = view.findViewById<MaterialButton>(R.id.btn_quiz)

        // Fill the screen every time the user data changes
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                logout() // should not happen, but never show an empty profile
                return@observe
            }
            avatar.text = user.username.first().uppercase()
            username.text = user.username
            email.text = user.email

            val skinName = SkinType.displayName(requireContext(), user.skinType)
            if (skinName == null) {
                mySkin.text = getString(R.string.profile_skin_type_none)
                quizButton.text = getString(R.string.btn_take_quiz)
            } else {
                mySkin.text = getString(R.string.skin_type_result, skinName)
                quizButton.text = getString(R.string.btn_retake_quiz)
            }
        }

        quizButton.setOnClickListener {
            (requireActivity() as MainActivity).open(QuizFragment())
        }

        view.findViewById<MaterialButton>(R.id.btn_logout).setOnClickListener { logout() }
    }

    // Runs every time Profile becomes visible (also when coming back from the quiz)
    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    private fun logout() {
        viewModel.logout()
        val intent = Intent(requireContext(), AuthActivity::class.java)
        // Start fresh: "back" must not return to the app after logging out
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}