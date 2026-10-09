package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.LectureDao
import com.example.data.local.dao.SalaryCycleDao
import com.example.data.local.dao.SalaryPaymentDao
import com.example.data.local.dao.StudentDao
import com.example.data.local.dao.TuitionDao
import com.example.data.local.entity.LectureEntity
import com.example.data.local.entity.SalaryCycleEntity
import com.example.data.local.entity.SalaryPaymentEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TuitionEntity

@Database(
    entities = [
        TuitionEntity::class,
        StudentEntity::class,
        LectureEntity::class,
        SalaryPaymentEntity::class,
        SalaryCycleEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tuitionDao(): TuitionDao
    abstract fun studentDao(): StudentDao
    abstract fun lectureDao(): LectureDao
    abstract fun salaryPaymentDao(): SalaryPaymentDao
    abstract fun salaryCycleDao(): SalaryCycleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add academic columns to lectures
                db.execSQL("ALTER TABLE lectures ADD COLUMN topic TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE lectures ADD COLUMN homework TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE lectures ADD COLUMN examMarks TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE lectures ADD COLUMN shortNote TEXT NOT NULL DEFAULT ''")

                // 2. Migrate existing academic progress data into lectures table
                try {
                    db.execSQL("""
                        UPDATE lectures SET 
                            topic = COALESCE((SELECT topic FROM academic_progress WHERE academic_progress.lectureId = lectures.lectureId), ''),
                            homework = COALESCE((SELECT upcomingHomework FROM academic_progress WHERE academic_progress.lectureId = lectures.lectureId), ''),
                            examMarks = COALESCE((SELECT testExamMarks FROM academic_progress WHERE academic_progress.lectureId = lectures.lectureId), ''),
                            shortNote = COALESCE((SELECT shortNote FROM academic_progress WHERE academic_progress.lectureId = lectures.lectureId), '')
                        WHERE EXISTS (SELECT 1 FROM academic_progress WHERE academic_progress.lectureId = lectures.lectureId)
                    """.trimIndent())
                    db.execSQL("DROP TABLE IF EXISTS academic_progress")
                } catch (_: Exception) {}

                // 3. Drop sync queue table
                try {
                    db.execSQL("DROP TABLE IF EXISTS sync_queue")
                } catch (_: Exception) {}

                // 4. Recreate salary_payments table to remove lecturesCovered
                try {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS salary_payments_new (
                            paymentId TEXT NOT NULL PRIMARY KEY,
                            tuitionId TEXT NOT NULL,
                            cycleId TEXT NOT NULL,
                            paymentDate TEXT NOT NULL,
                            amount REAL NOT NULL,
                            coverageStartDate TEXT NOT NULL DEFAULT '',
                            coverageEndDate TEXT NOT NULL DEFAULT '',
                            note TEXT NOT NULL DEFAULT '',
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL,
                            status TEXT NOT NULL DEFAULT 'ACTIVE',
                            isSynced INTEGER NOT NULL DEFAULT 0,
                            FOREIGN KEY(tuitionId) REFERENCES tuitions(tuitionId) ON DELETE CASCADE
                        )
                    """.trimIndent())
                    db.execSQL("""
                        INSERT INTO salary_payments_new (paymentId, tuitionId, cycleId, paymentDate, amount, coverageStartDate, coverageEndDate, note, createdAt, updatedAt, status, isSynced)
                        SELECT paymentId, tuitionId, cycleId, paymentDate, amount, coverageStartDate, coverageEndDate, note, createdAt, updatedAt, status, isSynced
                        FROM salary_payments
                    """.trimIndent())
                    db.execSQL("DROP TABLE salary_payments")
                    db.execSQL("ALTER TABLE salary_payments_new RENAME TO salary_payments")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_salary_payments_tuitionId ON salary_payments(tuitionId)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_salary_payments_cycleId ON salary_payments(cycleId)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_salary_payments_paymentDate ON salary_payments(paymentDate)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_salary_payments_status ON salary_payments(status)")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN nickname TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN fatherContact TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN motherContact TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN whatsApp TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN telegram TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN discord TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tuition_tracker_db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getDatabase(context: Context): AppDatabase = getInstance(context)
    }
}
