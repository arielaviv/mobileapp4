package com.company.assignment4.firebase

import android.net.Uri
import com.company.assignment4.model.Student
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val studentsCollection = firestore.collection("students")

    // Auth
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user
    }

    suspend fun register(email: String, password: String): FirebaseUser? {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user
    }

    fun signOut() {
        auth.signOut()
    }

    // Firestore
    suspend fun getAllStudents(): List<Student> {
        val snapshot = studentsCollection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(StudentFirestoreModel::class.java)?.toStudent(doc.id)
        }
    }

    suspend fun getStudentById(id: String): Student? {
        val doc = studentsCollection.document(id).get().await()
        return doc.toObject(StudentFirestoreModel::class.java)?.toStudent(doc.id)
    }

    suspend fun addStudent(student: Student) {
        val data = student.toFirestoreMap()
        studentsCollection.document(student.id).set(data).await()
    }

    suspend fun updateStudent(oldId: String, student: Student) {
        if (oldId != student.id) {
            studentsCollection.document(oldId).delete().await()
        }
        val data = student.toFirestoreMap()
        studentsCollection.document(student.id).set(data).await()
    }

    suspend fun deleteStudent(id: String) {
        studentsCollection.document(id).delete().await()
    }

    // Storage
    suspend fun uploadImage(imageUri: Uri, studentId: String): String {
        val ref = storage.reference.child("student_images/$studentId.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    // Helper classes
    data class StudentFirestoreModel(
        val name: String = "",
        val phone: String = "",
        val address: String = "",
        val isChecked: Boolean = false,
        val birthDate: String = "",
        val birthTime: String = "",
        val imageUrl: String = ""
    ) {
        fun toStudent(id: String): Student {
            return Student(
                id = id,
                name = name,
                phone = phone,
                address = address,
                isChecked = isChecked,
                birthDate = birthDate,
                birthTime = birthTime,
                imageUrl = imageUrl
            )
        }
    }

    private fun Student.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "phone" to phone,
            "address" to address,
            "isChecked" to isChecked,
            "birthDate" to birthDate,
            "birthTime" to birthTime,
            "imageUrl" to imageUrl
        )
    }
}
