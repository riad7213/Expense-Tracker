package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.LectureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LectureDao {
    @Query("SELECT * FROM lectures WHERE status != 'DELETED' ORDER BY date DESC, createdAt DESC")
    fun getAllLectures(): Flow<List<LectureEntity>>

    @Query("SELECT * FROM lectures WHERE tuitionId = :tuitionId AND status != 'DELETED' ORDER BY date DESC, lectureNumber DESC")
    fun getLecturesForTuition(tuitionId: String): Flow<List<LectureEntity>>

    @Query("SELECT * FROM lectures WHERE tuitionId = :tuitionId AND status != 'DELETED' ORDER BY date DESC, createdAt DESC")
    suspend fun getLecturesForTuitionSync(tuitionId: String): List<LectureEntity>

    @Query("SELECT * FROM lectures WHERE cycleId = :cycleId AND status != 'DELETED' ORDER BY lectureNumber ASC")
    fun getLecturesForCycle(cycleId: String): Flow<List<LectureEntity>>

    @Query("SELECT * FROM lectures WHERE cycleId = :cycleId AND status != 'DELETED' ORDER BY lectureNumber ASC")
    suspend fun getLecturesForCycleSync(cycleId: String): List<LectureEntity>

    @Query("SELECT COUNT(*) FROM lectures WHERE cycleId = :cycleId AND status != 'DELETED'")
    suspend fun getLectureCountForCycle(cycleId: String): Int

    @Query("SELECT * FROM lectures WHERE tuitionId = :tuitionId AND status != 'DELETED' ORDER BY date DESC, lectureNumber DESC LIMIT 1")
    fun getLatestLectureForTuition(tuitionId: String): Flow<LectureEntity?>

    @Query("SELECT COUNT(*) FROM lectures WHERE tuitionId = :tuitionId AND status != 'DELETED'")
    fun getLectureCountForTuition(tuitionId: String): Flow<Int>

    @Query("SELECT COALESCE(MAX(lectureNumber), 0) + 1 FROM lectures WHERE tuitionId = :tuitionId AND status != 'DELETED'")
    suspend fun getNextLectureNumber(tuitionId: String): Int

    @Query("SELECT * FROM lectures WHERE lectureId = :lectureId LIMIT 1")
    fun getLectureById(lectureId: String): Flow<LectureEntity?>

    @Query("SELECT * FROM lectures WHERE lectureId = :lectureId LIMIT 1")
    suspend fun getLectureByIdSync(lectureId: String): LectureEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(lecture: LectureEntity)

    @Update
    suspend fun update(lecture: LectureEntity)

    @Transaction
    suspend fun insertOrUpdate(lecture: LectureEntity) {
        val existing = getLectureByIdSync(lecture.lectureId)
        if (existing != null) {
            update(lecture)
        } else {
            insert(lecture)
        }
    }

    @Query("UPDATE lectures SET status = 'DELETED', updatedAt = :updatedAt WHERE lectureId = :lectureId")
    suspend fun softDeleteLecture(lectureId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM lectures WHERE lectureId = :lectureId")
    suspend fun deletePermanently(lectureId: String)
}
