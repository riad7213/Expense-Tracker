package com.example.data.model

import com.example.data.local.entity.LectureEntity
import com.example.data.local.entity.SalaryCycleEntity
import com.example.data.local.entity.SalaryPaymentEntity
import com.example.data.local.entity.TuitionEntity
import com.example.util.DateTimeUtils

data class CycleSummary(
    val cycle: SalaryCycleEntity,
    val completedLectures: Int = 0,
    val remainingLectures: Int = 0,
    val earnedSalary: Double = 0.0,
    val paidAmount: Double = 0.0,
    val outstandingAmount: Double = 0.0,
    val advanceAmount: Double = 0.0,
    val remainingContractSalary: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val payments: List<SalaryPaymentEntity> = emptyList()
) {
    val isClosed: Boolean get() = cycle.status == "CLOSED" || (completedLectures >= cycle.contractedLectures && paidAmount >= cycle.totalSalary)
    val isOpen: Boolean get() = !isClosed

    val lectureProgressFraction: Float
        get() = if (cycle.contractedLectures > 0) {
            (completedLectures.toFloat() / cycle.contractedLectures.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val financialProgressFraction: Float
        get() = if (cycle.totalSalary > 0) {
            (paidAmount.toFloat() / cycle.totalSalary.toFloat()).coerceIn(0f, 1f)
        } else 0f
}

data class MonthlyStats(
    val year: Int,
    val month: Int,
    val monthName: String,
    val contractedLectures: Int = 12,
    val completedLectures: Int = 0,
    val settledLectures: Int = 0,
    val unsettledLectures: Int = 0,
    val totalSalary: Double = 0.0,
    val earnedSalary: Double = 0.0,
    val paidSalary: Double = 0.0,
    val outstandingSalary: Double = 0.0,
    val totalClassMinutes: Int = 0,
    val totalTransportMinutes: Int = 0,
    val totalTimeMinutes: Int = 0
) {
    val lecturesGiven: Int get() = completedLectures
    val claimableSalary: Double get() = earnedSalary
    val remainingLectures: Int get() = (contractedLectures - completedLectures).coerceAtLeast(0)

    val classTimeFormatted: String
        get() = DateTimeUtils.formatMinutesToHoursAndMinutes(totalClassMinutes)
    val transportTimeFormatted: String
        get() = DateTimeUtils.formatMinutesToHoursAndMinutes(totalTransportMinutes)
    val totalTimeFormatted: String
        get() = DateTimeUtils.formatMinutesToHoursAndMinutes(totalTimeMinutes)
}

data class OverallMonthlySummary(
    val year: Int,
    val month: Int,
    val monthName: String,
    val totalTuitions: Int = 0,
    val totalLecturesGiven: Int = 0,
    val totalSettledLectures: Int = 0,
    val totalUnsettledLectures: Int = 0,
    val totalEarnedSalary: Double = 0.0,
    val totalPaidSalary: Double = 0.0,
    val totalOutstandingSalary: Double = 0.0,
    val totalClassMinutes: Int = 0,
    val totalTransportMinutes: Int = 0,
    val totalTimeMinutes: Int = 0
) {
    val totalClaimableSalary: Double get() = totalEarnedSalary

    val classTimeFormatted: String
        get() = DateTimeUtils.formatMinutesToHoursAndMinutes(totalClassMinutes)
    val transportTimeFormatted: String
        get() = DateTimeUtils.formatMinutesToHoursAndMinutes(totalTransportMinutes)
    val totalTimeFormatted: String
        get() = DateTimeUtils.formatMinutesToHoursAndMinutes(totalTimeMinutes)
}

data class TuitionSummary(
    val tuition: TuitionEntity,
    val studentCount: Int = 0,
    val monthlyStats: MonthlyStats = MonthlyStats(
        year = 2026,
        month = 10,
        monthName = "October 2026"
    ),
    val currentCycle: CycleSummary? = null,
    val cycleHistory: List<CycleSummary> = emptyList(),
    val allTimeLecturesCount: Int = 0,
    val payments: List<SalaryPaymentEntity> = emptyList(),
    val latestLecture: LectureEntity? = null
) {
    // Current cycle delegated properties (Cycle-based authority)
    val cycleNumber: Int get() = currentCycle?.cycle?.cycleNumber ?: 1
    val cycleCompletedLectures: Int get() = currentCycle?.completedLectures ?: 0
    val cycleContractedLectures: Int get() = currentCycle?.cycle?.contractedLectures ?: tuition.contractedLectures
    val cycleTotalSalary: Double get() = currentCycle?.cycle?.totalSalary ?: tuition.totalSalary
    val cycleEarnedSalary: Double get() = currentCycle?.earnedSalary ?: 0.0
    val cyclePaidAmount: Double get() = currentCycle?.paidAmount ?: 0.0
    val cycleOutstandingAmount: Double get() = currentCycle?.outstandingAmount ?: 0.0
    val cycleAdvanceAmount: Double get() = currentCycle?.advanceAmount ?: 0.0
    val isCycleOpen: Boolean get() = currentCycle?.isOpen ?: true
    val paymentStatus: PaymentStatus get() = currentCycle?.paymentStatus ?: PaymentStatus.UNPAID

    // Backward-compatible properties used in existing views
    val lecturesGiven: Int get() = monthlyStats.completedLectures
    val contractedLectures: Int get() = currentCycle?.cycle?.contractedLectures ?: monthlyStats.contractedLectures
    val claimableSalary: Double get() = currentCycle?.earnedSalary ?: monthlyStats.earnedSalary
    val earnedSalary: Double get() = currentCycle?.earnedSalary ?: monthlyStats.earnedSalary
    val paidSalary: Double get() = currentCycle?.paidAmount ?: monthlyStats.paidSalary
    val outstandingSalary: Double get() = currentCycle?.outstandingAmount ?: monthlyStats.outstandingSalary
    val settledLectures: Int get() = monthlyStats.settledLectures
    val unsettledLectures: Int get() = monthlyStats.unsettledLectures
    val remainingLectures: Int get() = currentCycle?.remainingLectures ?: monthlyStats.remainingLectures
}

data class TuitionDetailData(
    val tuition: TuitionEntity,
    val cycles: List<CycleSummary>,
    val activeCycle: CycleSummary?,
    val allLectures: List<LectureEntity>,
    val allPayments: List<SalaryPaymentEntity>
)

data class GlobalFinancialMetrics(
    val totalTargetSalary: Double,
    val totalEarned: Double,
    val totalPaid: Double,
    val totalDue: Double,
    val paidCyclesCount: Int,
    val unpaidCyclesCount: Int,
    val partialCyclesCount: Int
)

data class LectureWithProgress(
    val lecture: LectureEntity,
    val cycleNumber: Int = 1,
    val tuitionLocation: String = "",
    val tuitionClass: String = ""
)
