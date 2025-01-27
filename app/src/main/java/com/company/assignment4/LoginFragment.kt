package com.company.assignment4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.company.assignment4.firebase.FirebaseManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputLayoutPassword: TextInputLayout
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonGoToRegister: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var firebaseManager: FirebaseManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseManager = (requireActivity().application as StudentsApplication).firebaseManager

        if (firebaseManager.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_studentsListFragment)
            return
        }

        initViews(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        textInputLayoutEmail = view.findViewById(R.id.textInputLayoutEmail)
        textInputLayoutPassword = view.findViewById(R.id.textInputLayoutPassword)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        buttonLogin = view.findViewById(R.id.buttonLogin)
        buttonGoToRegister = view.findViewById(R.id.buttonGoToRegister)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        buttonLogin.setOnClickListener {
            if (validateInput()) {
                performLogin()
            }
        }

        buttonGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (email.isEmpty()) {
            textInputLayoutEmail.error = getString(R.string.error_empty_email)
            isValid = false
        } else {
            textInputLayoutEmail.error = null
        }

        if (password.isEmpty()) {
            textInputLayoutPassword.error = getString(R.string.error_empty_password)
            isValid = false
        } else {
            textInputLayoutPassword.error = null
        }

        return isValid
    }

    private fun performLogin() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        setLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                firebaseManager.signIn(email, password)
                findNavController().navigate(R.id.action_loginFragment_to_studentsListFragment)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.message ?: getString(R.string.login_failed),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        buttonLogin.isEnabled = !loading
        buttonGoToRegister.isEnabled = !loading
    }
}
