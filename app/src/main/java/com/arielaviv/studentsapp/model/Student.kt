package com.arielaviv.studentsapp.model

import com.arielaviv.studentsapp.R

data class Student(
    var id: String,
    var name: String,
    var phone: String,
    var address: String,
    var isChecked: Boolean = false,
    var birthDate: String = "",  // Format: dd/MM/yyyy
    var birthTime: String = "",  // Format: HH:mm
    val avatarResId: Int = R.drawable.ic_student_avatar
)
