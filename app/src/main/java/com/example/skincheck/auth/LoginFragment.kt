package com.example.skincheck.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.skincheck.R
import com.example.skincheck.data.AuthResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val emailLayout = view.findViewById<TextInputLayout>(R.id.layout_email)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.layout_password)
        val emailInput = view.findViewById<TextInputEditText>(R.id.input_email)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.input_password)

        view.findViewById<MaterialButton>(R.id.btn_login).setOnClickListener {
            emailLayout.error = null
            passwordLayout.error = null
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            // 1. Check the fields before asking the database
            var valid = true
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = getString(R.string.error_invalid_email)
                valid = false
            }
            if (password.isEmpty()) {
                passwordLayout.error = getString(R.string.error_empty_password)
                valid = false
            }

            // 2. Ask the ViewModel to log in
            if (valid) viewModel.login(email, password)
        }

        view.findViewById<MaterialButton>(R.id.btn_go_register).setOnClickListener {
            (requireActivity() as AuthActivity).showRegister()
        }

        // 3. React to the answer
        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthResult.Success -> (requireActivity() as AuthActivity).onLoggedIn(result.user)
                is AuthResult.Error -> passwordLayout.error = getString(result.messageRes)
                null -> return@observe
            }
            viewModel.consume()
        }
    }
}