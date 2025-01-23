package com.company.assignment4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.assignment4.adapter.StudentAdapter
import com.company.assignment4.model.StudentRepository
import kotlinx.coroutines.launch

class StudentsListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: StudentAdapter
    private lateinit var repository: StudentRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_students_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = (requireActivity().application as StudentsApplication).repository

        setupRecyclerView(view)
        setupMenu()
        observeStudents()
        refreshData()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewStudents)
        progressBar = view.findViewById(R.id.progressBar)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = StudentAdapter(
            students = emptyList(),
            onItemClick = { student ->
                val bundle = bundleOf("studentId" to student.id)
                findNavController().navigate(
                    R.id.action_studentsListFragment_to_studentDetailsFragment,
                    bundle
                )
            },
            onCheckboxClick = { student ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.toggleChecked(student.id)
                }
            }
        )

        recyclerView.adapter = adapter
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_students_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add_student -> {
                        findNavController().navigate(
                            R.id.action_studentsListFragment_to_newStudentFragment
                        )
                        true
                    }
                    R.id.action_sign_out -> {
                        signOut()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeStudents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.allStudents.collect { students ->
                    adapter.updateStudents(students)
                }
            }
        }
    }

    private fun refreshData() {
        progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            repository.refreshFromFirestore()
            progressBar.visibility = View.GONE
        }
    }

    private fun signOut() {
        val app = requireActivity().application as StudentsApplication
        app.firebaseManager.signOut()
        findNavController().navigate(R.id.action_studentsListFragment_to_loginFragment)
    }
}
