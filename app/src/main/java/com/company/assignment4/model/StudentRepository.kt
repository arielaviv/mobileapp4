package com.company.assignment4.model

import android.net.Uri
import com.company.assignment4.database.StudentDao
import com.company.assignment4.firebase.FirebaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StudentRepository(
    private val studentDao: StudentDao,
    private val firebaseManager: FirebaseManager
) {

    val allStudents: Flow<List<Student>> = studentDao.getAllStudents().map { entities ->
        entities.map { it.toStudent() }
    }

    suspend fun refreshFromFirestore() {
        withContext(Dispatchers.IO) {
            try {
                val students = firebaseManager.getAllStudents()
                val entities = students.map { it.toEntity() }
                studentDao.deleteAll()
                studentDao.insertAll(entities)
            } catch (e: Exception) {
                // no network, use local cache
            }
        }
    }

    suspend fun getById(id: String): Student? {
        return withContext(Dispatchers.IO) {
            studentDao.getStudentById(id)?.toStudent()
        }
    }

    suspend fun add(student: Student) {
        withContext(Dispatchers.IO) {
            studentDao.insertStudent(student.toEntity())
            try {
                firebaseManager.addStudent(student)
            } catch (e: Exception) {
                // ignore, saved locally
            }
        }
    }

    suspend fun update(oldId: String, student: Student) {
        withContext(Dispatchers.IO) {
            if (oldId != student.id) {
                studentDao.deleteStudent(oldId)
            }
            studentDao.insertStudent(student.toEntity())
            try {
                firebaseManager.updateStudent(oldId, student)
            } catch (_: Exception) {
            }
        }
    }

    suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            studentDao.deleteStudent(id)
            try {
                firebaseManager.deleteStudent(id)
            } catch (e: Exception) {
                // failed to delete remotely
            }
        }
    }

    suspend fun toggleChecked(id: String) {
        withContext(Dispatchers.IO) {
            val entity = studentDao.getStudentById(id) ?: return@withContext
            val updated = entity.copy(isChecked = !entity.isChecked)
            studentDao.updateStudent(updated)
            try {
                firebaseManager.updateStudent(id, updated.toStudent())
            } catch (_: Exception) {
                // ok, local db is updated
            }
        }
    }

    suspend fun exists(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            studentDao.getStudentById(id) != null
        }
    }

    suspend fun uploadImage(imageUri: Uri, studentId: String): String {
        return withContext(Dispatchers.IO) {
            firebaseManager.uploadImage(imageUri, studentId)
        }
    }
}
