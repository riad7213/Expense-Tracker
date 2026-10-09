package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE tuitionId = :tuitionId AND status != 'DELETED' ORDER BY createdAt ASC")
    fun getStudentsForTuition(tuitionId: String): Flow<List<StudentEntity>>

    @Query("SELECT COUNT(*) FROM students WHERE tuitionId = :tuitionId AND status = 'ACTIVE'")
    fun getActiveStudentCount(tuitionId: String): Flow<Int>

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    fun getStudentById(studentId: String): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentByIdSync(studentId: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(student: StudentEntity)

    @Update
    suspend fun update(student: StudentEntity)

    @Transaction
    suspend fun insertOrUpdate(student: StudentEntity) {
        val existing = getStudentByIdSync(student.studentId)
        if (existing != null) {
            update(student)
        } else {
            insert(student)
        }
    }

    @Query("UPDATE students SET status = 'ARCHIVED', updatedAt = :updatedAt WHERE studentId = :studentId")
    suspend fun archiveStudent(studentId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE students SET status = 'DELETED', updatedAt = :updatedAt WHERE studentId = :studentId")
    suspend fun softDeleteStudent(studentId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM students WHERE studentId = :studentId")
    suspend fun deletePermanently(studentId: String)
}
