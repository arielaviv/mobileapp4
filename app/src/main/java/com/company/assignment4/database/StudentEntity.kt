package com.company.assignment4.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val address: String,
    val isChecked: Boolean = false,
    val birthDate: String = "",
    val birthTime: String = "",
    val imageUrl: String = "",
    val localImagePath: String = ""
)
