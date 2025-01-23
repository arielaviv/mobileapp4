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

class RegisterFragment : Fragment() {

    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputLayoutPassword: TextInputLayout
    private lateinit var textInputLayoutConfirmPassword: TextInputLayout
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var buttonRegister: Button
    private lateinit var buttonGoToLogin: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var firebaseManager: FirebaseManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseManager = (requireActivity().application as StudentsApplication).firebaseManager

        initViews(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        textInputLayoutEmail = view.findViewById(R.id.textInputLayoutEmail)
        textInputLayoutPassword = view.findViewById(R.id.textInputLayoutPassword)
        textInputLayoutConfirmPassword = view.findViewById(R.id.textInputLayoutConfirmPassword)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword)
        buttonRegister = view.findViewById(R.id.buttonRegister)
        buttonGoToLogin = view.findViewById(R.id.buttonGoToLogin)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        buttonRegister.setOnClickListener {
            if (validateInput()) {
                performRegister()
            }
        }

        buttonGoToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val confirmPassword = editTextConfirmPassword.text.toString().trim()

        if (email.isEmpty()) {
            textInputLayoutEmail.error = getString(R.string.error_empty_email)
            isValid = false
        } else {
            textInputLayoutEmail.error = null
        }

        if (password.isEmpty()) {
            textInputLayoutPassword.error = getString(R.string.error_empty_password)
            isValid = false
        } else if (password.length < 6) {
            textInputLayoutPassword.error = getString(R.string.error_short_password)
            isValid = false
        } else {
            textInputLayoutPassword.error = null
        }

        if (confirmPassword != password) {
            textInputLayoutConfirmPassword.error = getString(R.string.error_passwords_mismatch)
            isValid = false
        } else {
            textInputLayoutConfirmPassword.error = null
        }

        return isValid
    }

    private fun performRegister() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        setLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                firebaseManager.register(email, password)
                findNavController().navigate(R.id.action_registerFragment_to_studentsListFragment)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.message ?: getString(R.string.register_failed),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        buttonRegister.isEnabled = !loading
        buttonGoToLogin.isEnabled = !loading
    }
}
