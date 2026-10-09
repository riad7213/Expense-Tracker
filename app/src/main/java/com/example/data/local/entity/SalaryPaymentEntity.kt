package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "salary_payments",
    foreignKeys = [
        ForeignKey(
            entity = TuitionEntity::class,
            parentColumns = ["tuitionId"],
            childColumns = ["tuitionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tuitionId"]),
        Index(value = ["cycleId"]),
        Index(value = ["paymentDate"]),
        Index(value = ["status"])
    ]
)
data class SalaryPaymentEntity(
    @PrimaryKey
    val paymentId: String = UUID.randomUUID().toString(),
    val tuitionId: String,
    val cycleId: String = "",
    val paymentDate: String, // "dd-MMM-yy" e.g. "20-Oct-26"
    val amount: Double = 0.0, // actual amount received in BDT
    val coverageStartDate: String = "",
    val coverageEndDate: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE", // "ACTIVE", "ARCHIVED", "DELETED"
    val isSynced: Boolean = false
)
