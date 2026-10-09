package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "lectures",
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
        Index(value = ["tuitionId", "lectureNumber"]),
        Index(value = ["cycleId"]),
        Index(value = ["status"])
    ]
)
data class LectureEntity(
    @PrimaryKey
    val lectureId: String = UUID.randomUUID().toString(),
    val tuitionId: String,
    val cycleId: String = "",
    val lectureNumber: Int,
    val date: String, // "dd-MMM-yy" e.g. "07-Oct-26"
    val day: String,
    val leavingTime: String = "",
    val startingTime: String = "",
    val endingTime: String = "",
    val returnTime: String = "",
    val totalTimeSpent: String = "",
    val classDuration: String = "",
    val transportationDuration: String = "",
    // Academic details merged directly into Lecture
    val topic: String = "", // Topic / Chapter
    val homework: String = "", // Homework description or status
    val examMarks: String = "", // Test / Exam Marks
    val shortNote: String = "", // Short Note
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
