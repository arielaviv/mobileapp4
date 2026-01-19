package com.arielaviv.studentsapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arielaviv.studentsapp.R
import com.arielaviv.studentsapp.model.Student

class StudentAdapter(
    private var students: List<Student>,
    private val onItemClick: (Student) -> Unit,
    private val onCheckboxClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewAvatar: ImageView = itemView.findViewById(R.id.imageViewAvatar)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewId: TextView = itemView.findViewById(R.id.textViewId)
        val checkBoxStudent: CheckBox = itemView.findViewById(R.id.checkBoxStudent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]

        holder.imageViewAvatar.setImageResource(student.avatarResId)
        holder.textViewName.text = student.name
        holder.textViewId.text = "ID: ${student.id}"

        // Remove listener before setting checked state to avoid triggering callback
        holder.checkBoxStudent.setOnCheckedChangeListener(null)
        holder.checkBoxStudent.isChecked = student.isChecked

        // Set up click listeners
        holder.itemView.setOnClickListener {
            onItemClick(student)
        }

        holder.checkBoxStudent.setOnCheckedChangeListener { _, _ ->
            onCheckboxClick(student)
        }
    }

    override fun getItemCount(): Int = students.size

    fun updateStudents(newStudents: List<Student>) {
        students = newStudents
        notifyDataSetChanged()
    }
}
