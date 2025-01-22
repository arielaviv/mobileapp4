package com.company.assignment4

import android.app.Application
import com.company.assignment4.database.AppDatabase
import com.company.assignment4.firebase.FirebaseManager
import com.company.assignment4.model.StudentRepository

class StudentsApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val firebaseManager by lazy { FirebaseManager() }
    val repository by lazy { StudentRepository(database.studentDao(), firebaseManager) }
}
