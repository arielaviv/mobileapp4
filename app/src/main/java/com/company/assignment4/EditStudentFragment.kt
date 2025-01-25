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

class EditStudentFragment : Fragment() {

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
    private lateinit var buttonDelete: Button
    private lateinit var buttonSave: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var repository: StudentRepository
    private var originalStudentId: String? = null
    private var originalStudent: Student? = null
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
        return inflater.inflate(R.layout.fragment_edit_student, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = (requireActivity().application as StudentsApplication).repository
        originalStudentId = arguments?.getString("studentId")

        initViews(view)
        loadStudentData()
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
        buttonDelete = view.findViewById(R.id.buttonDelete)
        buttonSave = view.findViewById(R.id.buttonSave)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun loadStudentData() {
        setLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            originalStudent = originalStudentId?.let { repository.getById(it) }

            if (originalStudent == null) {
                findNavController().popBackStack()
                return@launch
            }

            originalStudent?.let { student ->
                // Load image with Glide
                val imageSource = student.localImagePath.ifEmpty { student.imageUrl }
                if (imageSource.isNotEmpty()) {
                    Glide.with(this@EditStudentFragment)
                        .load(imageSource)
                        .placeholder(R.drawable.ic_student_avatar)
                        .error(R.drawable.ic_student_avatar)
                        .circleCrop()
                        .into(imageViewAvatar)
                }

                editTextName.setText(student.name)
                editTextId.setText(student.id)
                editTextPhone.setText(student.phone)
                editTextAddress.setText(student.address)
                editTextBirthDate.setText(student.birthDate)
                editTextBirthTime.setText(student.birthTime)
                checkBoxChecked.isChecked = student.isChecked
            }

            setLoading(false)
        }
    }

    private fun setupListeners() {
        imageViewAvatar.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        buttonDelete.setOnClickListener {
            showDeleteConfirmation()
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

        val existingDate = editTextBirthDate.text.toString()
        if (existingDate.isNotEmpty()) {
            val parts = existingDate.split("/")
            if (parts.size == 3) {
                calendar.set(Calendar.DAY_OF_MONTH, parts[0].toIntOrNull() ?: 1)
                calendar.set(Calendar.MONTH, (parts[1].toIntOrNull() ?: 1) - 1)
                calendar.set(Calendar.YEAR, parts[2].toIntOrNull() ?: calendar.get(Calendar.YEAR))
            }
        }

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

        val existingTime = editTextBirthTime.text.toString()
        if (existingTime.isNotEmpty()) {
            val parts = existingTime.split(":")
            if (parts.size == 2) {
                calendar.set(Calendar.HOUR_OF_DAY, parts[0].toIntOrNull() ?: 12)
                calendar.set(Calendar.MINUTE, parts[1].toIntOrNull() ?: 0)
            }
        }

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
                val newId = editTextId.text.toString().trim()

                if (newId != originalStudentId && repository.exists(newId)) {
                    textInputLayoutId.error = getString(R.string.error_duplicate_id)
                    setLoading(false)
                    return@launch
                }

                var imageUrl = originalStudent?.imageUrl ?: ""
                var localImagePath = originalStudent?.localImagePath ?: ""

                selectedImageUri?.let { uri ->
                    localImagePath = saveImageLocally(uri, newId)
                    try {
                        imageUrl = repository.uploadImage(uri, newId)
                    } catch (_: Exception) {
                        // Upload failed, will use local image
                    }
                }

                val updatedStudent = Student(
                    id = newId,
                    name = editTextName.text.toString().trim(),
                    phone = editTextPhone.text.toString().trim(),
                    address = editTextAddress.text.toString().trim(),
                    isChecked = checkBoxChecked.isChecked,
                    birthDate = editTextBirthDate.text.toString().trim(),
                    birthTime = editTextBirthTime.text.toString().trim(),
                    imageUrl = imageUrl,
                    localImagePath = localImagePath
                )

                originalStudentId?.let { oldId ->
                    repository.update(oldId, updatedStudent)
                }

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
        buttonDelete.isEnabled = !loading
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

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(R.string.delete_confirmation_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteStudent()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteStudent() {
        setLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                originalStudentId?.let { id ->
                    repository.delete(id)
                }
                findNavController().popBackStack(R.id.studentsListFragment, false)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                setLoading(false)
            }
        }
    }
}
