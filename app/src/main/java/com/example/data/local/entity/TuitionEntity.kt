package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "tuitions",
    indices = [Index(value = ["status"])]
)
data class TuitionEntity(
    @PrimaryKey
    val tuitionId: String = UUID.randomUUID().toString(),
    val location: String,
    val classGrade: String,
    val tuitionType: String = "Physical", // "Online" or "Physical"
    val daysPerWeek: Int = 3, // 2, 3, or 4
    val startingDate: String,
    val totalSalary: Double = 0.0,
    val mapLink: String = "", // Google Maps URL link
    val status: String = "ACTIVE", // "ACTIVE", "ARCHIVED", "DELETED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    val contractedLectures: Int
        get() = when (daysPerWeek) {
            2 -> 8
            3 -> 12
            4 -> 16
            else -> 12
        }
}
