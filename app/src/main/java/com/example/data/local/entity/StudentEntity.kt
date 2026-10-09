package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "students",
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
        Index(value = ["status"])
    ]
)
data class StudentEntity(
    @PrimaryKey
    val studentId: String = UUID.randomUUID().toString(),
    val tuitionId: String,
    val name: String, // Full Name
    val nickname: String = "",
    val contact: String = "",
    val fatherContact: String = "",
    val motherContact: String = "",
    val whatsApp: String = "",
    val telegram: String = "",
    val discord: String = "",
    val isOnline: Boolean = false,
    val type: String = "",
    val approach: String = "",
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    val displayName: String
        get() = nickname.ifBlank { name }
}
