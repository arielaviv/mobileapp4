package com.company.assignment4.model

import com.company.assignment4.database.StudentEntity

fun Student.toEntity(): StudentEntity {
    return StudentEntity(
        id = id,
        name = name,
        phone = phone,
        address = address,
        isChecked = isChecked,
        birthDate = birthDate,
        birthTime = birthTime,
        imageUrl = imageUrl,
        localImagePath = localImagePath
    )
}

fun StudentEntity.toStudent(): Student {
    return Student(
        id = id,
        name = name,
        phone = phone,
        address = address,
        isChecked = isChecked,
        birthDate = birthDate,
        birthTime = birthTime,
        imageUrl = imageUrl,
        localImagePath = localImagePath
    )
}
