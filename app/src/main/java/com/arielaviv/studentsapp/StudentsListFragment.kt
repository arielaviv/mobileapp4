package com.arielaviv.studentsapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arielaviv.studentsapp.adapter.StudentAdapter
import com.arielaviv.studentsapp.model.StudentRepository

class StudentsListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_students_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        setupMenu()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewStudents)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = StudentAdapter(
            students = StudentRepository.getAll(),
            onItemClick = { student ->
                val bundle = bundleOf("studentId" to student.id)
                findNavController().navigate(
                    R.id.action_studentsListFragment_to_studentDetailsFragment,
                    bundle
                )
            },
            onCheckboxClick = { student ->
                StudentRepository.toggleChecked(student.id)
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
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun refreshList() {
        adapter.updateStudents(StudentRepository.getAll())
    }
}
