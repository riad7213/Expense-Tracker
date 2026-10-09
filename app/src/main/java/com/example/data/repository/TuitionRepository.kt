package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.LectureEntity
import com.example.data.local.entity.SalaryCycleEntity
import com.example.data.local.entity.SalaryPaymentEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TuitionEntity
import com.example.data.model.CycleSummary
import com.example.data.model.LectureWithProgress
import com.example.data.model.MonthlyStats
import com.example.data.model.SalaryCalculator
import com.example.data.model.TuitionDetailData
import com.example.data.model.TuitionSummary
import com.example.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class TuitionRepository(
    private val database: AppDatabase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val tuitionDao = database.tuitionDao()
    private val studentDao = database.studentDao()
    private val lectureDao = database.lectureDao()
    private val salaryPaymentDao = database.salaryPaymentDao()
    private val salaryCycleDao = database.salaryCycleDao()
    private val tuitionMutex = Mutex()

    fun getAllTuitions(): Flow<List<TuitionEntity>> = tuitionDao.getAllNonDeletedTuitions()

    fun getAllCycles(): Flow<List<SalaryCycleEntity>> = salaryCycleDao.getAllCycles()

    fun getAllPayments(): Flow<List<SalaryPaymentEntity>> = salaryPaymentDao.getAllNonDeletedPayments()

    suspend fun getOrCreateOpenCycle(tuitionId: String, lectureDate: String = ""): SalaryCycleEntity {
        val existingOpen = salaryCycleDao.getOpenCycleForTuitionSync(tuitionId)
        if (existingOpen != null) {
            val lecturesInCycle = lectureDao.getLectureCountForCycle(existingOpen.cycleId)
            if (lecturesInCycle >= existingOpen.contractedLectures) {
                checkAndCloseCycleIfEligible(existingOpen.cycleId, lectureDate)
                return createNextCycle(tuitionId, lectureDate)
            }

            if (existingOpen.startDate.isBlank() && lectureDate.isNotBlank()) {
                val updated = existingOpen.copy(startDate = lectureDate, updatedAt = System.currentTimeMillis())
                salaryCycleDao.insertOrUpdate(updated)
                return updated
            }
            return existingOpen
        }

        return createNextCycle(tuitionId, lectureDate)
    }

    private suspend fun createNextCycle(tuitionId: String, startDate: String = ""): SalaryCycleEntity {
        // Prevent duplicate open cycles: if an open cycle already exists, return it
        val existingOpen = salaryCycleDao.getOpenCycleForTuitionSync(tuitionId)
        if (existingOpen != null) {
            return existingOpen
        }

        val tuition = tuitionDao.getTuitionByIdSync(tuitionId)
        val maxCycleNum = salaryCycleDao.getMaxCycleNumber(tuitionId)
        val newCycle = SalaryCycleEntity(
            cycleId = UUID.randomUUID().toString(),
            tuitionId = tuitionId,
            cycleNumber = maxCycleNum + 1,
            startDate = startDate.ifBlank { DateTimeUtils.getCurrentDate() },
            endDate = null,
            contractedLectures = tuition?.contractedLectures ?: 12,
            totalSalary = tuition?.totalSalary ?: 0.0,
            status = "OPEN",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        salaryCycleDao.insertOrUpdate(newCycle)
        return newCycle
    }

    suspend fun checkAndCloseCycleIfEligible(cycleId: String, eventDate: String) {
        val cycle = salaryCycleDao.getCycleByIdSync(cycleId) ?: return
        val lectureCount = lectureDao.getLectureCountForCycle(cycleId)
        val payments = salaryPaymentDao.getPaymentsForCycleSync(cycleId)
        val totalPaid = payments.filter { it.status != "DELETED" }.sumOf { it.amount }

        val shouldClose = lectureCount >= cycle.contractedLectures && totalPaid >= cycle.totalSalary

        if (shouldClose && cycle.status != "CLOSED") {
            val closeDate = eventDate.ifBlank { DateTimeUtils.getCurrentDate() }
            salaryCycleDao.updateCycleStatus(
                cycleId = cycleId,
                status = "CLOSED",
                endDate = closeDate,
                updatedAt = System.currentTimeMillis()
            )
        } else if (!shouldClose && cycle.status == "CLOSED") {
            salaryCycleDao.updateCycleStatus(
                cycleId = cycleId,
                status = "OPEN",
                endDate = null,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    suspend fun cleanupDuplicateOpenCycles(tuitionId: String) {
        val cycles = salaryCycleDao.getCyclesForTuitionSync(tuitionId).sortedBy { it.cycleNumber }
        val openCycles = cycles.filter { it.status == "OPEN" }
        if (openCycles.size > 1) {
            // Find which cycles have recorded lectures or payments
            val openWithActivity = openCycles.filter { cycle ->
                val lectureCount = lectureDao.getLectureCountForCycle(cycle.cycleId)
                val payments = salaryPaymentDao.getPaymentsForCycleSync(cycle.cycleId)
                lectureCount > 0 || payments.isNotEmpty()
            }

            // Keep the active cycle with data, or if none have activity, keep the first one (Cycle #1)
            val cycleToKeepOpen = openWithActivity.maxByOrNull { it.cycleNumber }
                ?: openCycles.firstOrNull()

            for (extra in openCycles) {
                if (extra.cycleId != cycleToKeepOpen?.cycleId) {
                    val lectureCount = lectureDao.getLectureCountForCycle(extra.cycleId)
                    val payments = salaryPaymentDao.getPaymentsForCycleSync(extra.cycleId)
                    if (lectureCount == 0 && payments.isEmpty()) {
                        // Empty duplicate open cycle: delete permanently
                        salaryCycleDao.deletePermanently(extra.cycleId)
                    } else {
                        // Has data: close it to ensure only one cycle is OPEN at a time
                        salaryCycleDao.updateCycleStatus(
                            cycleId = extra.cycleId,
                            status = "CLOSED",
                            endDate = extra.startDate.ifBlank { DateTimeUtils.getCurrentDate() },
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                }
            }
        }
    }

    suspend fun cleanupAllDuplicateOpenCycles() {
        val tuitions = tuitionDao.getAllNonDeletedTuitionsSync()
        for (tuition in tuitions) {
            cleanupDuplicateOpenCycles(tuition.tuitionId)
        }
    }

    private suspend fun buildCycleSummaries(
        tuition: TuitionEntity,
        allLectures: List<LectureEntity>,
        allPayments: List<SalaryPaymentEntity>
    ): List<CycleSummary> {
        val cycles = salaryCycleDao.getCyclesForTuitionSync(tuition.tuitionId)
        if (cycles.isEmpty()) {
            return emptyList()
        }

        return cycles.map { cycle ->
            val cycleLectures = allLectures.filter { it.cycleId == cycle.cycleId }
            val cyclePayments = allPayments.filter { it.cycleId == cycle.cycleId }

            val metrics = SalaryCalculator.calculateCycleMetrics(cycle, cycleLectures, cyclePayments)

            CycleSummary(
                cycle = cycle,
                completedLectures = metrics.completedLectures,
                remainingLectures = metrics.remainingLectures,
                earnedSalary = metrics.earnedSalary,
                paidAmount = metrics.paidAmount,
                outstandingAmount = metrics.outstandingAmount,
                advanceAmount = metrics.advanceAmount,
                remainingContractSalary = metrics.remainingContractSalary,
                paymentStatus = metrics.paymentStatus,
                payments = cyclePayments
            )
        }
    }

    fun getTuitionSummaries(status: String = "ACTIVE", year: Int = 2026, month: Int = 10): Flow<List<TuitionSummary>> {
        return tuitionDao.getTuitionsByStatus(status).map { tuitions ->
            tuitions.map { tuition ->
                buildSummaryForTuition(tuition, year, month)
            }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun buildSummaryForTuition(tuition: TuitionEntity, year: Int, month: Int): TuitionSummary {
        val studentCount = studentDao.getActiveStudentCount(tuition.tuitionId).first()
        val allLectures = lectureDao.getLecturesForTuition(tuition.tuitionId).first()
        val allPayments = salaryPaymentDao.getPaymentsForTuition(tuition.tuitionId).first()

        val cycleSummaries = buildCycleSummaries(tuition, allLectures, allPayments)
        val currentOpenCycle = cycleSummaries.firstOrNull { it.cycle.status == "OPEN" } ?: cycleSummaries.firstOrNull()

        val monthLectures = allLectures.filter { DateTimeUtils.isDateInYearMonth(it.date, year, month) }
        val completedInMonth = monthLectures.size

        var totalClassMinutes = 0
        var totalTimeMinutes = 0
        for (lec in monthLectures) {
            val classMins = DateTimeUtils.calculateMinutesBetween(lec.startingTime, lec.endingTime)
            val totalMins = DateTimeUtils.calculateMinutesBetween(lec.leavingTime, lec.returnTime)
            totalClassMinutes += classMins
            totalTimeMinutes += totalMins
        }
        val totalTransportMinutes = (totalTimeMinutes - totalClassMinutes).coerceAtLeast(0)

        val monthlyStats = MonthlyStats(
            year = year,
            month = month,
            monthName = DateTimeUtils.formatYearMonth(year, month),
            contractedLectures = currentOpenCycle?.cycle?.contractedLectures ?: tuition.contractedLectures,
            completedLectures = completedInMonth,
            settledLectures = currentOpenCycle?.completedLectures ?: 0,
            unsettledLectures = currentOpenCycle?.remainingLectures ?: 0,
            totalSalary = currentOpenCycle?.cycle?.totalSalary ?: tuition.totalSalary,
            earnedSalary = currentOpenCycle?.earnedSalary ?: 0.0,
            paidSalary = currentOpenCycle?.paidAmount ?: 0.0,
            outstandingSalary = currentOpenCycle?.outstandingAmount ?: 0.0,
            totalClassMinutes = totalClassMinutes,
            totalTransportMinutes = totalTransportMinutes,
            totalTimeMinutes = totalTimeMinutes
        )

        return TuitionSummary(
            tuition = tuition,
            studentCount = studentCount,
            monthlyStats = monthlyStats,
            currentCycle = currentOpenCycle,
            cycleHistory = cycleSummaries,
            allTimeLecturesCount = allLectures.size,
            payments = currentOpenCycle?.payments ?: allPayments,
            latestLecture = monthLectures.firstOrNull() ?: allLectures.firstOrNull()
        )
    }

    fun getTuitionDetail(tuitionId: String): Flow<TuitionDetailData?> {
        return combine(
            tuitionDao.getTuitionById(tuitionId),
            salaryCycleDao.getCyclesForTuition(tuitionId),
            lectureDao.getLecturesForTuition(tuitionId),
            salaryPaymentDao.getPaymentsForTuition(tuitionId)
        ) { tuition, cycles, lectures, payments ->
            if (tuition == null) null
            else {
                val cycleSummaries = cycles.map { cycle ->
                    val cycleLectures = lectures.filter { it.cycleId == cycle.cycleId }
                    val cyclePayments = payments.filter { it.cycleId == cycle.cycleId }
                    val metrics = SalaryCalculator.calculateCycleMetrics(cycle, cycleLectures, cyclePayments)
                    CycleSummary(
                        cycle = cycle,
                        completedLectures = metrics.completedLectures,
                        remainingLectures = metrics.remainingLectures,
                        earnedSalary = metrics.earnedSalary,
                        paidAmount = metrics.paidAmount,
                        outstandingAmount = metrics.outstandingAmount,
                        advanceAmount = metrics.advanceAmount,
                        remainingContractSalary = metrics.remainingContractSalary,
                        paymentStatus = metrics.paymentStatus,
                        payments = cyclePayments
                    )
                }
                val activeCycle = cycleSummaries.firstOrNull { it.cycle.status == "OPEN" } ?: cycleSummaries.firstOrNull()
                TuitionDetailData(
                    tuition = tuition,
                    cycles = cycleSummaries,
                    activeCycle = activeCycle,
                    allLectures = lectures,
                    allPayments = payments
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getCycleSummary(cycleId: String): Flow<CycleSummary?> {
        return combine(
            salaryCycleDao.getCycleById(cycleId),
            lectureDao.getLecturesForCycle(cycleId),
            salaryPaymentDao.getPaymentsForCycle(cycleId)
        ) { cycle, lectures, payments ->
            if (cycle == null) null
            else {
                val metrics = SalaryCalculator.calculateCycleMetrics(cycle, lectures, payments)
                CycleSummary(
                    cycle = cycle,
                    completedLectures = metrics.completedLectures,
                    remainingLectures = metrics.remainingLectures,
                    earnedSalary = metrics.earnedSalary,
                    paidAmount = metrics.paidAmount,
                    outstandingAmount = metrics.outstandingAmount,
                    advanceAmount = metrics.advanceAmount,
                    remainingContractSalary = metrics.remainingContractSalary,
                    paymentStatus = metrics.paymentStatus,
                    payments = payments
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getStudents(tuitionId: String): Flow<List<StudentEntity>> {
        return studentDao.getStudentsForTuition(tuitionId)
    }

    fun getAllLectures(): Flow<List<LectureWithProgress>> {
        return combine(
            lectureDao.getAllLectures(),
            salaryCycleDao.getAllCycles(),
            tuitionDao.getAllNonDeletedTuitions()
        ) { lectures: List<LectureEntity>, cycles: List<SalaryCycleEntity>, tuitions: List<TuitionEntity> ->
            val cycleMap = cycles.associateBy { it.cycleId }
            val tuitionMap = tuitions.associateBy { it.tuitionId }
            lectures.map { lecture ->
                val cycleNum = cycleMap[lecture.cycleId]?.cycleNumber ?: 1
                val tuition = tuitionMap[lecture.tuitionId]
                LectureWithProgress(
                    lecture = lecture,
                    cycleNumber = cycleNum,
                    tuitionLocation = tuition?.location ?: "",
                    tuitionClass = tuition?.classGrade ?: ""
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getLectureWithProgressById(lectureId: String): Flow<LectureWithProgress?> {
        return combine(
            lectureDao.getLectureById(lectureId),
            salaryCycleDao.getAllCycles(),
            tuitionDao.getAllNonDeletedTuitions()
        ) { lecture, cycles, tuitions ->
            if (lecture == null) null
            else {
                val cycle = cycles.find { it.cycleId == lecture.cycleId }
                val tuition = tuitions.find { it.tuitionId == lecture.tuitionId }
                LectureWithProgress(
                    lecture = lecture,
                    cycleNumber = cycle?.cycleNumber ?: 1,
                    tuitionLocation = tuition?.location ?: "",
                    tuitionClass = tuition?.classGrade ?: ""
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getAllCycleSummaries(): Flow<List<CycleSummary>> {
        return combine(
            tuitionDao.getAllNonDeletedTuitions(),
            salaryCycleDao.getAllCycles(),
            lectureDao.getAllLectures(),
            salaryPaymentDao.getAllNonDeletedPayments()
        ) { tuitions: List<TuitionEntity>, cycles: List<SalaryCycleEntity>, lectures: List<LectureEntity>, payments: List<SalaryPaymentEntity> ->
            cycles.map { cycle ->
                val cycleLectures = lectures.filter { it.cycleId == cycle.cycleId }
                val cyclePayments = payments.filter { it.cycleId == cycle.cycleId }
                val metrics = SalaryCalculator.calculateCycleMetrics(cycle, cycleLectures, cyclePayments)

                CycleSummary(
                    cycle = cycle,
                    completedLectures = metrics.completedLectures,
                    remainingLectures = metrics.remainingLectures,
                    earnedSalary = metrics.earnedSalary,
                    paidAmount = metrics.paidAmount,
                    outstandingAmount = metrics.outstandingAmount,
                    advanceAmount = metrics.advanceAmount,
                    remainingContractSalary = metrics.remainingContractSalary,
                    paymentStatus = metrics.paymentStatus,
                    payments = cyclePayments
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveTuition(
        tuition: TuitionEntity,
        initialStudentName: String = "",
        initialStudentContact: String = ""
    ) {
        tuitionMutex.withLock {
            val now = System.currentTimeMillis()
            val toSave = tuition.copy(updatedAt = now)
            tuitionDao.insertOrUpdate(toSave)

            if (initialStudentName.isNotBlank()) {
                val student = StudentEntity(
                    studentId = UUID.randomUUID().toString(),
                    tuitionId = toSave.tuitionId,
                    name = initialStudentName.trim(),
                    contact = initialStudentContact.trim(),
                    isOnline = toSave.tuitionType.equals("Online", ignoreCase = true),
                    createdAt = now,
                    updatedAt = now
                )
                studentDao.insertOrUpdate(student)
            }

            val existingCycles = salaryCycleDao.getCyclesForTuitionSync(toSave.tuitionId)
            if (existingCycles.isEmpty()) {
                val initialCycle = SalaryCycleEntity(
                    cycleId = UUID.randomUUID().toString(),
                    tuitionId = toSave.tuitionId,
                    cycleNumber = 1,
                    startDate = toSave.startingDate.ifBlank { DateTimeUtils.getCurrentDate() },
                    contractedLectures = toSave.contractedLectures,
                    totalSalary = toSave.totalSalary,
                    status = "OPEN"
                )
                salaryCycleDao.insertOrUpdate(initialCycle)
            }
            // Always ensure that no duplicate open cycles exist
            cleanupDuplicateOpenCycles(toSave.tuitionId)
        }
    }

    suspend fun saveTuition(
        id: String?,
        studentName: String,
        className: String,
        institution: String,
        subjects: String,
        contractedLectures: Int,
        totalSalary: Double,
        location: String,
        colorHex: String,
        paymentType: String = "Per Cycle",
        notes: String = ""
    ) {
        val days = when (contractedLectures) {
            8 -> 2
            16 -> 4
            else -> 3
        }
        val existing = if (id != null) tuitionDao.getTuitionByIdSync(id) else null
        val tuition = (existing ?: TuitionEntity(
            tuitionId = id ?: UUID.randomUUID().toString(),
            location = location,
            classGrade = className,
            daysPerWeek = days,
            startingDate = DateTimeUtils.getCurrentDate(),
            totalSalary = totalSalary
        )).copy(
            location = location,
            classGrade = className,
            daysPerWeek = days,
            totalSalary = totalSalary,
            updatedAt = System.currentTimeMillis()
        )
        saveTuition(tuition, studentName, "")
    }

    suspend fun saveTuition(
        id: String?,
        location: String,
        classGrade: String,
        tuitionType: String,
        daysPerWeek: Int,
        startingDate: String,
        totalSalary: Double,
        mapLink: String,
        studentName: String,
        studentContact: String
    ) {
        val contractedLec = when (daysPerWeek) {
            1 -> 4
            2 -> 8
            4 -> 16
            5 -> 20
            6 -> 24
            7 -> 28
            else -> 12
        }
        val existing = if (id != null) tuitionDao.getTuitionByIdSync(id) else null
        val tuition = (existing ?: TuitionEntity(
            tuitionId = id ?: UUID.randomUUID().toString(),
            location = location,
            classGrade = classGrade,
            tuitionType = tuitionType,
            daysPerWeek = daysPerWeek,
            startingDate = startingDate,
            totalSalary = totalSalary,
            mapLink = mapLink
        )).copy(
            location = location,
            classGrade = classGrade,
            tuitionType = tuitionType,
            daysPerWeek = daysPerWeek,
            startingDate = startingDate,
            totalSalary = totalSalary,
            mapLink = mapLink,
            updatedAt = System.currentTimeMillis()
        )
        saveTuition(tuition, studentName, studentContact)
    }

    suspend fun archiveTuition(tuitionId: String) {
        tuitionDao.archiveTuition(tuitionId)
    }

    suspend fun unarchiveTuition(tuitionId: String) {
        tuitionDao.unarchiveTuition(tuitionId)
    }

    suspend fun deleteTuition(tuitionId: String) {
        tuitionDao.softDeleteTuition(tuitionId)
    }

    suspend fun createNewCycle(
        tuitionId: String,
        startDate: String,
        forceClosePrevious: Boolean = true
    ): Result<SalaryCycleEntity> {
        val existingCycles = salaryCycleDao.getCyclesForTuitionSync(tuitionId)
        val openCycles = existingCycles.filter { it.status == "OPEN" }
        if (openCycles.isNotEmpty()) {
            if (forceClosePrevious) {
                val closeDate = startDate.ifBlank { DateTimeUtils.getCurrentDate() }
                for (openCycle in openCycles) {
                    salaryCycleDao.updateCycleStatus(
                        cycleId = openCycle.cycleId,
                        status = "CLOSED",
                        endDate = closeDate,
                        updatedAt = System.currentTimeMillis()
                    )
                }
            } else {
                return Result.failure(IllegalStateException("An active cycle already exists for this tuition."))
            }
        }
        val tuition = tuitionDao.getTuitionByIdSync(tuitionId)
            ?: return Result.failure(IllegalArgumentException("Tuition not found"))
        val maxCycleNum = salaryCycleDao.getMaxCycleNumber(tuitionId)
        val newCycle = SalaryCycleEntity(
            cycleId = UUID.randomUUID().toString(),
            tuitionId = tuitionId,
            cycleNumber = maxCycleNum + 1,
            startDate = startDate.ifBlank { DateTimeUtils.getCurrentDate() },
            endDate = null,
            contractedLectures = tuition.contractedLectures,
            totalSalary = tuition.totalSalary,
            status = "OPEN",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        salaryCycleDao.insertOrUpdate(newCycle)
        return Result.success(newCycle)
    }

    suspend fun saveStudent(student: StudentEntity) {
        val now = System.currentTimeMillis()
        studentDao.insertOrUpdate(student.copy(updatedAt = now))
    }

    suspend fun saveStudent(
        id: String?,
        tuitionId: String,
        name: String,
        nickname: String = "",
        contact: String = "",
        fatherContact: String = "",
        motherContact: String = "",
        whatsApp: String = "",
        telegram: String = "",
        discord: String = "",
        isOnline: Boolean = false,
        type: String = "",
        approach: String = ""
    ) {
        val existing = if (id != null) studentDao.getStudentByIdSync(id) else null
        val now = System.currentTimeMillis()
        val student = (existing ?: StudentEntity(
            studentId = id ?: UUID.randomUUID().toString(),
            tuitionId = tuitionId,
            name = name,
            createdAt = now
        )).copy(
            tuitionId = tuitionId,
            name = name.ifBlank { existing?.name ?: "" },
            nickname = nickname,
            contact = contact,
            fatherContact = fatherContact,
            motherContact = motherContact,
            whatsApp = whatsApp,
            telegram = telegram,
            discord = discord,
            isOnline = isOnline,
            type = type,
            approach = approach,
            updatedAt = now
        )
        studentDao.insertOrUpdate(student)
    }

    suspend fun saveStudent(
        id: String?,
        tuitionId: String,
        name: String,
        grade: String,
        school: String,
        parentContact: String,
        notes: String
    ) {
        saveStudent(
            id = id,
            tuitionId = tuitionId,
            name = name,
            contact = parentContact
        )
    }

    suspend fun deleteStudent(studentId: String) {
        studentDao.softDeleteStudent(studentId)
    }

    suspend fun recordLecture(
        tuitionId: String,
        cycleId: String? = null,
        date: String,
        day: String,
        startTime: String,
        endTime: String,
        durationMinutes: Int,
        topic: String = "",
        homework: String = "",
        examMarks: String = "",
        shortNote: String = "",
        leavingTime: String = "",
        returnTime: String = ""
    ): Result<LectureEntity> {
        val targetCycle = if (!cycleId.isNullOrBlank()) {
            salaryCycleDao.getCycleByIdSync(cycleId) ?: getOrCreateOpenCycle(tuitionId, date)
        } else {
            val openCycle = getOrCreateOpenCycle(tuitionId, date)
            val cycleLectures = lectureDao.getLecturesForCycleSync(openCycle.cycleId)
            if (cycleLectures.size >= openCycle.contractedLectures) {
                checkAndCloseCycleIfEligible(openCycle.cycleId, date)
                createNextCycle(tuitionId, date)
            } else {
                openCycle
            }
        }

        val lectureNumber = (lectureDao.getLectureCountForCycle(targetCycle.cycleId) + 1)
            .coerceAtMost(targetCycle.contractedLectures)

        val transportMinutes = if (leavingTime.isNotBlank() && returnTime.isNotBlank()) {
            val total = DateTimeUtils.calculateMinutesBetween(leavingTime, returnTime)
            (total - durationMinutes).coerceAtLeast(0)
        } else 0

        val lecture = LectureEntity(
            lectureId = UUID.randomUUID().toString(),
            tuitionId = tuitionId,
            cycleId = targetCycle.cycleId,
            lectureNumber = lectureNumber,
            date = date,
            day = day,
            leavingTime = leavingTime,
            startingTime = startTime,
            endingTime = endTime,
            returnTime = returnTime,
            classDuration = "$durationMinutes mins",
            transportationDuration = if (transportMinutes > 0) "$transportMinutes mins" else "",
            topic = topic,
            homework = homework,
            examMarks = examMarks,
            shortNote = shortNote,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        saveLecture(lecture)
        return Result.success(lecture)
    }

    suspend fun updateLecture(
        lectureId: String,
        date: String,
        day: String,
        startTime: String,
        endTime: String,
        durationMinutes: Int,
        topic: String,
        homework: String,
        examMarks: String,
        shortNote: String,
        leavingTime: String = "",
        returnTime: String = ""
    ) {
        val existing = lectureDao.getLectureByIdSync(lectureId) ?: return
        val transportMinutes = if (leavingTime.isNotBlank() && returnTime.isNotBlank()) {
            val total = DateTimeUtils.calculateMinutesBetween(leavingTime, returnTime)
            (total - durationMinutes).coerceAtLeast(0)
        } else 0

        val updated = existing.copy(
            date = date,
            day = day,
            leavingTime = leavingTime,
            startingTime = startTime,
            endingTime = endTime,
            returnTime = returnTime,
            classDuration = "$durationMinutes mins",
            transportationDuration = if (transportMinutes > 0) "$transportMinutes mins" else existing.transportationDuration,
            topic = topic,
            homework = homework,
            examMarks = examMarks,
            shortNote = shortNote,
            updatedAt = System.currentTimeMillis()
        )
        lectureDao.insertOrUpdate(updated)
    }

    suspend fun saveLecture(lecture: LectureEntity) {
        val now = System.currentTimeMillis()

        var cycle = if (lecture.cycleId.isNotBlank()) {
            salaryCycleDao.getCycleByIdSync(lecture.cycleId) ?: getOrCreateOpenCycle(lecture.tuitionId, lecture.date)
        } else {
            getOrCreateOpenCycle(lecture.tuitionId, lecture.date)
        }

        val cycleLectures = lectureDao.getLecturesForCycleSync(cycle.cycleId)
        val isExisting = cycleLectures.any { it.lectureId == lecture.lectureId }

        if (!isExisting && cycleLectures.size >= cycle.contractedLectures) {
            checkAndCloseCycleIfEligible(cycle.cycleId, lecture.date)
            cycle = createNextCycle(lecture.tuitionId, lecture.date)
        }

        val assignedLectureNumber = if (isExisting && lecture.lectureNumber > 0) {
            lecture.lectureNumber
        } else {
            (cycleLectures.size + 1).coerceAtMost(cycle.contractedLectures)
        }

        val lectureToSave = lecture.copy(
            cycleId = cycle.cycleId,
            lectureNumber = assignedLectureNumber,
            updatedAt = now
        )

        lectureDao.insertOrUpdate(lectureToSave)

        if (cycle.startDate.isBlank() && lectureToSave.date.isNotBlank()) {
            salaryCycleDao.insertOrUpdate(cycle.copy(startDate = lectureToSave.date, updatedAt = now))
        }

        checkAndCloseCycleIfEligible(cycle.cycleId, lectureToSave.date)
    }

    suspend fun deleteLecture(lectureId: String) {
        val lecture = lectureDao.getLectureByIdSync(lectureId)
        lectureDao.softDeleteLecture(lectureId)
        if (lecture != null && lecture.cycleId.isNotBlank()) {
            checkAndCloseCycleIfEligible(lecture.cycleId, "")
        }
    }

    suspend fun recordPayment(
        cycleId: String,
        tuitionId: String,
        amount: Double,
        paymentDate: String,
        paymentMethod: String,
        notes: String
    ) {
        val payment = SalaryPaymentEntity(
            paymentId = UUID.randomUUID().toString(),
            tuitionId = tuitionId,
            cycleId = cycleId,
            paymentDate = paymentDate,
            amount = amount,
            note = if (paymentMethod.isNotBlank()) "[$paymentMethod] $notes".trim() else notes,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        salaryPaymentDao.insertOrUpdate(payment)
        checkAndCloseCycleIfEligible(cycleId, paymentDate)
    }

    suspend fun updatePayment(
        paymentId: String,
        amount: Double,
        paymentDate: String,
        paymentMethod: String,
        notes: String
    ) {
        val existing = salaryPaymentDao.getPaymentByIdSync(paymentId) ?: return
        val updated = existing.copy(
            amount = amount,
            paymentDate = paymentDate,
            note = if (paymentMethod.isNotBlank()) "[$paymentMethod] $notes".trim() else notes,
            updatedAt = System.currentTimeMillis()
        )
        salaryPaymentDao.insertOrUpdate(updated)
        checkAndCloseCycleIfEligible(existing.cycleId, paymentDate)
    }

    suspend fun deletePayment(paymentId: String) {
        val existing = salaryPaymentDao.getPaymentByIdSync(paymentId)
        salaryPaymentDao.softDeletePayment(paymentId)
        if (existing != null) {
            checkAndCloseCycleIfEligible(existing.cycleId, "")
        }
    }
}
