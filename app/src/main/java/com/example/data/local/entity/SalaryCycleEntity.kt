package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "salary_cycles",
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
        Index(value = ["tuitionId", "cycleNumber"]),
        Index(value = ["status"])
    ]
)
data class SalaryCycleEntity(
    @PrimaryKey
    val cycleId: String = UUID.randomUUID().toString(),
    val tuitionId: String,
    val cycleNumber: Int = 1,
    val startDate: String = "",
    val endDate: String? = null,
    val contractedLectures: Int = 12,
    val totalSalary: Double = 0.0,
    val status: String = "OPEN", // "OPEN", "CLOSED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
