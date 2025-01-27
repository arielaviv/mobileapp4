package com.company.assignment4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.company.assignment4.model.Student
import com.company.assignment4.model.StudentRepository
import kotlinx.coroutines.launch

class StudentDetailsFragment : Fragment() {

    private lateinit var imageViewAvatar: ImageView
    private lateinit var textViewName: TextView
    private lateinit var textViewId: TextView
    private lateinit var textViewPhone: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewBirthDate: TextView
    private lateinit var textViewBirthTime: TextView
    private lateinit var textViewStatus: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var repository: StudentRepository
    private var studentId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_student_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = (requireActivity().application as StudentsApplication).repository
        studentId = arguments?.getString("studentId")

        initViews(view)
        setupMenu()
        loadStudentData()
    }

    override fun onResume() {
        super.onResume()
        loadStudentData()
    }

    private fun initViews(view: View) {
        imageViewAvatar = view.findViewById(R.id.imageViewAvatar)
        textViewName = view.findViewById(R.id.textViewName)
        textViewId = view.findViewById(R.id.textViewId)
        textViewPhone = view.findViewById(R.id.textViewPhone)
        textViewAddress = view.findViewById(R.id.textViewAddress)
        textViewBirthDate = view.findViewById(R.id.textViewBirthDate)
        textViewBirthTime = view.findViewById(R.id.textViewBirthTime)
        textViewStatus = view.findViewById(R.id.textViewStatus)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_student_details, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit_student -> {
                        val bundle = bundleOf("studentId" to studentId)
                        findNavController().navigate(
                            R.id.action_studentDetailsFragment_to_editStudentFragment,
                            bundle
                        )
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun loadStudentData() {
        progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            val student = studentId?.let { repository.getById(it) }

            if (student == null) {
                findNavController().popBackStack()
                return@launch
            }

            displayStudent(student)
            progressBar.visibility = View.GONE
        }
    }

    private fun displayStudent(student: Student) {
        val imageSource = student.localImagePath.ifEmpty { student.imageUrl }
        if (imageSource.isNotEmpty()) {
            Glide.with(this)
                .load(imageSource)
                .placeholder(R.drawable.ic_student_avatar)
                .error(R.drawable.ic_student_avatar)
                .circleCrop()
                .into(imageViewAvatar)
        } else {
            imageViewAvatar.setImageResource(R.drawable.ic_student_avatar)
        }

        textViewName.text = student.name
        textViewId.text = student.id
        textViewPhone.text = student.phone.ifEmpty { "-" }
        textViewAddress.text = student.address.ifEmpty { "-" }
        textViewBirthDate.text = student.birthDate.ifEmpty { "-" }
        textViewBirthTime.text = student.birthTime.ifEmpty { "-" }
        textViewStatus.text = if (student.isChecked) {
            getString(R.string.checked_status)
        } else {
            getString(R.string.unchecked_status)
        }
    }
}
