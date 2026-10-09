package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.SalaryPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryPaymentDao {
    @Query("SELECT * FROM salary_payments WHERE tuitionId = :tuitionId AND status != 'DELETED' ORDER BY paymentDate DESC, createdAt DESC")
    fun getPaymentsForTuition(tuitionId: String): Flow<List<SalaryPaymentEntity>>

    @Query("SELECT * FROM salary_payments WHERE cycleId = :cycleId AND status != 'DELETED' ORDER BY paymentDate DESC, createdAt DESC")
    fun getPaymentsForCycle(cycleId: String): Flow<List<SalaryPaymentEntity>>

    @Query("SELECT * FROM salary_payments WHERE cycleId = :cycleId AND status != 'DELETED' ORDER BY paymentDate DESC, createdAt DESC")
    suspend fun getPaymentsForCycleSync(cycleId: String): List<SalaryPaymentEntity>

    @Query("SELECT * FROM salary_payments WHERE tuitionId = :tuitionId AND status != 'DELETED' ORDER BY paymentDate DESC, createdAt DESC")
    suspend fun getPaymentsForTuitionSync(tuitionId: String): List<SalaryPaymentEntity>

    @Query("SELECT * FROM salary_payments WHERE status != 'DELETED' ORDER BY paymentDate DESC, createdAt DESC")
    fun getAllNonDeletedPayments(): Flow<List<SalaryPaymentEntity>>

    @Query("SELECT * FROM salary_payments WHERE paymentId = :paymentId LIMIT 1")
    fun getPaymentById(paymentId: String): Flow<SalaryPaymentEntity?>

    @Query("SELECT * FROM salary_payments WHERE paymentId = :paymentId LIMIT 1")
    suspend fun getPaymentByIdSync(paymentId: String): SalaryPaymentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(payment: SalaryPaymentEntity)

    @Update
    suspend fun update(payment: SalaryPaymentEntity)

    @Transaction
    suspend fun insertOrUpdate(payment: SalaryPaymentEntity) {
        val existing = getPaymentByIdSync(payment.paymentId)
        if (existing != null) {
            update(payment)
        } else {
            insert(payment)
        }
    }

    @Query("UPDATE salary_payments SET status = 'ARCHIVED', updatedAt = :updatedAt WHERE paymentId = :paymentId")
    suspend fun archivePayment(paymentId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE salary_payments SET status = 'DELETED', updatedAt = :updatedAt WHERE paymentId = :paymentId")
    suspend fun softDeletePayment(paymentId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM salary_payments WHERE paymentId = :paymentId")
    suspend fun deletePermanently(paymentId: String)
}
