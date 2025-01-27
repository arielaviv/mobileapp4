package com.company.assignment4.model

import android.net.Uri
import com.company.assignment4.database.StudentDao
import com.company.assignment4.firebase.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class StudentRepository(
    private val studentDao: StudentDao,
    private val firebaseManager: FirebaseManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allStudents: Flow<List<Student>> = studentDao.getAllStudents().map { entities ->
        entities.map { it.toStudent() }
    }

    suspend fun refreshFromFirestore() {
        withContext(Dispatchers.IO) {
            val remoteStudents = withTimeoutOrNull(10_000L) {
                firebaseManager.getAllStudents()
            } ?: return@withContext

            // preserve local image paths when merging
            val localEntities = studentDao.getAllStudentsList()
            val localPathMap = localEntities.associate { it.id to it.localImagePath }

            val merged = remoteStudents.map { student ->
                val localPath = localPathMap[student.id] ?: ""
                student.copy(localImagePath = localPath.ifEmpty { student.localImagePath })
            }

            studentDao.deleteAll()
            studentDao.insertAll(merged.map { it.toEntity() })
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
        }
        // sync to firestore in background
        scope.launch {
            try { firebaseManager.addStudent(student) } catch (_: Throwable) {}
        }
    }

    suspend fun update(oldId: String, student: Student) {
        withContext(Dispatchers.IO) {
            if (oldId != student.id) {
                studentDao.deleteStudent(oldId)
            }
            studentDao.insertStudent(student.toEntity())
        }
        scope.launch {
            try { firebaseManager.updateStudent(oldId, student) } catch (_: Throwable) {}
        }
    }

    suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            studentDao.deleteStudent(id)
        }
        scope.launch {
            try { firebaseManager.deleteStudent(id) } catch (_: Throwable) {}
        }
    }

    suspend fun toggleChecked(id: String) {
        withContext(Dispatchers.IO) {
            val entity = studentDao.getStudentById(id) ?: return@withContext
            val updated = entity.copy(isChecked = !entity.isChecked)
            studentDao.updateStudent(updated)
            scope.launch {
                try { firebaseManager.updateStudent(id, updated.toStudent()) } catch (_: Throwable) {}
            }
        }
    }

    suspend fun exists(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            studentDao.getStudentById(id) != null
        }
    }

    suspend fun uploadImage(imageUri: Uri, studentId: String): String? {
        return withContext(Dispatchers.IO) {
            withTimeoutOrNull(15_000L) {
                firebaseManager.uploadImage(imageUri, studentId)
            }
        }
    }
}
