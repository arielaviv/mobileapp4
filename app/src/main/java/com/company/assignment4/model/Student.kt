package com.company.assignment4.model

data class Student(
    var id: String,
    var name: String,
    var phone: String,
    var address: String,
    var isChecked: Boolean = false,
    var birthDate: String = "",
    var birthTime: String = "",
    var imageUrl: String = "",
    var localImagePath: String = ""
)
