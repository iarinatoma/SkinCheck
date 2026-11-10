package com.example.skincheck.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.skincheck.R
import com.example.skincheck.data.AuthResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val usernameLayout = view.findViewById<TextInputLayout>(R.id.layout_username)
        val emailLayout = view.findViewById<TextInputLayout>(R.id.layout_email)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.layout_password)
        val repeatLayout = view.findViewById<TextInputLayout>(R.id.layout_password_repeat)

        val usernameInput = view.findViewById<TextInputEditText>(R.id.input_username)
        val emailInput = view.findViewById<TextInputEditText>(R.id.input_email)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.input_password)
        val repeatInput = view.findViewById<TextInputEditText>(R.id.input_password_repeat)

        view.findViewById<MaterialButton>(R.id.btn_register).setOnClickListener {
            listOf(usernameLayout, emailLayout, passwordLayout, repeatLayout).forEach { it.error = null }

            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val repeat = repeatInput.text.toString()

            // 1. Check every field
            var valid = true
            if (username.length < 3) {
                usernameLayout.error = getString(R.string.error_short_username); valid = false
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = getString(R.string.error_invalid_email); valid = false
            }
            if (password.length < 6) {
                passwordLayout.error = getString(R.string.error_short_password); valid = false
            }
            if (repeat != password) {
                repeatLayout.error = getString(R.string.error_passwords_differ); valid = false
            }

            // 2. Create the account
            if (valid) viewModel.register(username, email, password)
        }

        view.findViewById<MaterialButton>(R.id.btn_go_login).setOnClickListener {
            (requireActivity() as AuthActivity).showLogin()
        }

        // 3. React to the answer: show the error under the right field
        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthResult.Success -> (requireActivity() as AuthActivity).onLoggedIn(result.user)
                is AuthResult.Error -> when (result.messageRes) {
                    R.string.error_username_taken -> usernameLayout.error = getString(result.messageRes)
                    R.string.error_email_taken -> emailLayout.error = getString(result.messageRes)
                    else -> Toast.makeText(requireContext(), result.messageRes, Toast.LENGTH_SHORT).show()
                }
                null -> return@observe
            }
            viewModel.consume()
        }
    }
}