package com.company.assignment4.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudent(id: String)

    @Query("DELETE FROM students")
    suspend fun deleteAll()
}
