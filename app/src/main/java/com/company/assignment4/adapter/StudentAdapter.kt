package com.company.assignment4.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.company.assignment4.R
import com.company.assignment4.model.Student

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

        val imageSource = student.localImagePath.ifEmpty { student.imageUrl }
        if (imageSource.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageSource)
                .placeholder(R.drawable.ic_student_avatar)
                .error(R.drawable.ic_student_avatar)
                .circleCrop()
                .into(holder.imageViewAvatar)
        } else {
            holder.imageViewAvatar.setImageResource(R.drawable.ic_student_avatar)
        }

        holder.textViewName.text = student.name
        holder.textViewId.text = "ID: ${student.id}"

        holder.checkBoxStudent.setOnCheckedChangeListener(null)
        holder.checkBoxStudent.isChecked = student.isChecked

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
