package com.company.assignment4

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.company.assignment4.model.Student
import com.company.assignment4.model.StudentRepository
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class NewStudentFragment : Fragment() {

    private lateinit var imageViewAvatar: ImageView
    private lateinit var textInputLayoutName: TextInputLayout
    private lateinit var textInputLayoutId: TextInputLayout
    private lateinit var editTextName: TextInputEditText
    private lateinit var editTextId: TextInputEditText
    private lateinit var editTextPhone: TextInputEditText
    private lateinit var editTextAddress: TextInputEditText
    private lateinit var editTextBirthDate: TextInputEditText
    private lateinit var editTextBirthTime: TextInputEditText
    private lateinit var checkBoxChecked: CheckBox
    private lateinit var buttonCancel: Button
    private lateinit var buttonSave: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var repository: StudentRepository
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(imageViewAvatar)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_student, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = (requireActivity().application as StudentsApplication).repository

        initViews(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        imageViewAvatar = view.findViewById(R.id.imageViewAvatar)
        textInputLayoutName = view.findViewById(R.id.textInputLayoutName)
        textInputLayoutId = view.findViewById(R.id.textInputLayoutId)
        editTextName = view.findViewById(R.id.editTextName)
        editTextId = view.findViewById(R.id.editTextId)
        editTextPhone = view.findViewById(R.id.editTextPhone)
        editTextAddress = view.findViewById(R.id.editTextAddress)
        editTextBirthDate = view.findViewById(R.id.editTextBirthDate)
        editTextBirthTime = view.findViewById(R.id.editTextBirthTime)
        checkBoxChecked = view.findViewById(R.id.checkBoxChecked)
        buttonCancel = view.findViewById(R.id.buttonCancel)
        buttonSave = view.findViewById(R.id.buttonSave)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        imageViewAvatar.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        buttonSave.setOnClickListener {
            if (validateInput()) {
                saveStudent()
            }
        }

        editTextBirthDate.setOnClickListener {
            showDatePicker()
        }

        editTextBirthTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(
                    "%02d/%02d/%04d",
                    selectedDay,
                    selectedMonth + 1,
                    selectedYear
                )
                editTextBirthDate.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                editTextBirthTime.setText(formattedTime)
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private fun validateInput(): Boolean {
        var isValid = true

        val name = editTextName.text.toString().trim()
        val id = editTextId.text.toString().trim()

        if (name.isEmpty()) {
            textInputLayoutName.error = getString(R.string.error_empty_name)
            isValid = false
        } else {
            textInputLayoutName.error = null
        }

        if (id.isEmpty()) {
            textInputLayoutId.error = getString(R.string.error_empty_id)
            isValid = false
        } else {
            textInputLayoutId.error = null
        }

        return isValid
    }

    private fun saveStudent() {
        setLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val studentId = editTextId.text.toString().trim()

                if (repository.exists(studentId)) {
                    textInputLayoutId.error = getString(R.string.error_duplicate_id)
                    setLoading(false)
                    return@launch
                }

                var imageUrl = ""
                var localImagePath = ""

                selectedImageUri?.let { uri ->
                    // Save locally
                    localImagePath = saveImageLocally(uri, studentId)
                    // Upload to Firebase
                    try {
                        imageUrl = repository.uploadImage(uri, studentId)
                    } catch (_: Exception) {
                        // Upload failed, will use local image
                    }
                }

                val student = Student(
                    id = studentId,
                    name = editTextName.text.toString().trim(),
                    phone = editTextPhone.text.toString().trim(),
                    address = editTextAddress.text.toString().trim(),
                    isChecked = checkBoxChecked.isChecked,
                    birthDate = editTextBirthDate.text.toString().trim(),
                    birthTime = editTextBirthTime.text.toString().trim(),
                    imageUrl = imageUrl,
                    localImagePath = localImagePath
                )

                repository.add(student)
                showSuccessDialog()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun saveImageLocally(uri: Uri, studentId: String): String {
        val imagesDir = File(requireContext().filesDir, "student_images")
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val file = File(imagesDir, "$studentId.jpg")
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        buttonSave.isEnabled = !loading
        buttonCancel.isEnabled = !loading
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.success)
            .setMessage(R.string.student_saved_successfully)
            .setPositiveButton(R.string.ok) { _, _ ->
                findNavController().popBackStack()
            }
            .setCancelable(false)
            .show()
    }
}
