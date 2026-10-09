package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.SalaryCycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryCycleDao {
    @Query("SELECT * FROM salary_cycles ORDER BY createdAt DESC")
    fun getAllCycles(): Flow<List<SalaryCycleEntity>>

    @Query("SELECT * FROM salary_cycles WHERE tuitionId = :tuitionId ORDER BY cycleNumber DESC")
    fun getCyclesForTuition(tuitionId: String): Flow<List<SalaryCycleEntity>>

    @Query("SELECT * FROM salary_cycles WHERE tuitionId = :tuitionId ORDER BY cycleNumber DESC")
    suspend fun getCyclesForTuitionSync(tuitionId: String): List<SalaryCycleEntity>

    @Query("SELECT * FROM salary_cycles WHERE tuitionId = :tuitionId AND status = 'OPEN' ORDER BY cycleNumber ASC LIMIT 1")
    fun getOpenCycleForTuition(tuitionId: String): Flow<SalaryCycleEntity?>

    @Query("SELECT * FROM salary_cycles WHERE tuitionId = :tuitionId AND status = 'OPEN' ORDER BY cycleNumber ASC LIMIT 1")
    suspend fun getOpenCycleForTuitionSync(tuitionId: String): SalaryCycleEntity?

    @Query("SELECT * FROM salary_cycles WHERE cycleId = :cycleId LIMIT 1")
    fun getCycleById(cycleId: String): Flow<SalaryCycleEntity?>

    @Query("SELECT * FROM salary_cycles WHERE cycleId = :cycleId LIMIT 1")
    suspend fun getCycleByIdSync(cycleId: String): SalaryCycleEntity?

    @Query("SELECT COALESCE(MAX(cycleNumber), 0) FROM salary_cycles WHERE tuitionId = :tuitionId")
    suspend fun getMaxCycleNumber(tuitionId: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(cycle: SalaryCycleEntity)

    @Update
    suspend fun update(cycle: SalaryCycleEntity)

    @Transaction
    suspend fun insertOrUpdate(cycle: SalaryCycleEntity) {
        val existing = getCycleByIdSync(cycle.cycleId)
        if (existing != null) {
            update(cycle)
        } else {
            insert(cycle)
        }
    }

    @Query("UPDATE salary_cycles SET status = :status, endDate = :endDate, updatedAt = :updatedAt WHERE cycleId = :cycleId")
    suspend fun updateCycleStatus(cycleId: String, status: String, endDate: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM salary_cycles WHERE cycleId = :cycleId")
    suspend fun deletePermanently(cycleId: String)
}
