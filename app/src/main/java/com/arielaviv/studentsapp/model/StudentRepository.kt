package com.arielaviv.studentsapp.model

object StudentRepository {
    private val students = mutableListOf<Student>()

    fun getAll(): List<Student> = students.toList()

    fun getById(id: String): Student? = students.find { it.id == id }

    fun add(student: Student) {
        students.add(student)
    }

    fun update(oldId: String, student: Student) {
        val index = students.indexOfFirst { it.id == oldId }
        if (index != -1) {
            students[index] = student
        }
    }

    fun delete(id: String) {
        students.removeAll { it.id == id }
    }

    fun toggleChecked(id: String) {
        val student = students.find { it.id == id }
        student?.let {
            it.isChecked = !it.isChecked
        }
    }

    fun exists(id: String): Boolean = students.any { it.id == id }
}
