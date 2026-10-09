package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.TuitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TuitionDao {
    @Query("SELECT * FROM tuitions WHERE status = :status ORDER BY updatedAt DESC")
    fun getTuitionsByStatus(status: String = "ACTIVE"): Flow<List<TuitionEntity>>

    @Query("SELECT * FROM tuitions WHERE status != 'DELETED' ORDER BY updatedAt DESC")
    fun getAllNonDeletedTuitions(): Flow<List<TuitionEntity>>

    @Query("SELECT * FROM tuitions WHERE status != 'DELETED' ORDER BY updatedAt DESC")
    suspend fun getAllNonDeletedTuitionsSync(): List<TuitionEntity>

    @Query("SELECT * FROM tuitions WHERE tuitionId = :id LIMIT 1")
    fun getTuitionById(id: String): Flow<TuitionEntity?>

    @Query("SELECT * FROM tuitions WHERE tuitionId = :id LIMIT 1")
    suspend fun getTuitionByIdSync(id: String): TuitionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tuition: TuitionEntity)

    @Update
    suspend fun update(tuition: TuitionEntity)

    /**
     * Safe upsert that performs an UPDATE on existing rows instead of SQLite REPLACE,
     * protecting child entities (cycles, lectures, students, payments) from cascade deletion.
     */
    @Transaction
    suspend fun insertOrUpdate(tuition: TuitionEntity) {
        val existing = getTuitionByIdSync(tuition.tuitionId)
        if (existing != null) {
            update(tuition)
        } else {
            insert(tuition)
        }
    }

    @Query("UPDATE tuitions SET status = 'ARCHIVED', updatedAt = :updatedAt WHERE tuitionId = :id")
    suspend fun archiveTuition(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tuitions SET status = 'ACTIVE', updatedAt = :updatedAt WHERE tuitionId = :id")
    suspend fun unarchiveTuition(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tuitions SET status = 'DELETED', updatedAt = :updatedAt WHERE tuitionId = :id")
    suspend fun softDeleteTuition(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM tuitions WHERE tuitionId = :id")
    suspend fun deletePermanently(id: String)
}
