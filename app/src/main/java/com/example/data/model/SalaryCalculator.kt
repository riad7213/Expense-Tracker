package com.example.data.model

import com.example.data.local.entity.LectureEntity
import com.example.data.local.entity.SalaryCycleEntity
import com.example.data.local.entity.SalaryPaymentEntity
import com.example.data.local.entity.TuitionEntity
import kotlin.math.min

enum class PaymentStatus(val displayName: String) {
    UNPAID("Unpaid"),
    PARTIAL("Partial"),
    PAID("Paid"),
    OVERPAID("Overpaid")
}

data class CycleFinancialMetrics(
    val completedLectures: Int,
    val contractedLectures: Int,
    val remainingLectures: Int,
    val totalSalary: Double,
    val earnedSalary: Double,
    val paidAmount: Double,
    val outstandingAmount: Double,
    val advanceAmount: Double,
    val remainingContractSalary: Double,
    val paymentStatus: PaymentStatus,
    val isCycleClosed: Boolean
) {
    val targetSalary: Double get() = totalSalary
    val totalPaid: Double get() = paidAmount
    val remainingBalance: Double get() = outstandingAmount
    val earnedAmount: Double get() = earnedSalary
}

object SalaryCalculator {

    fun computePaymentStatus(paidAmount: Double, totalSalary: Double): PaymentStatus {
        return calculatePaymentStatus(paidAmount, totalSalary)
    }

    fun calculatePaymentStatus(paidAmount: Double, totalSalary: Double): PaymentStatus {
        return when {
            totalSalary <= 0.0 && paidAmount <= 0.0 -> PaymentStatus.PAID
            paidAmount <= 0.0 -> PaymentStatus.UNPAID
            paidAmount < totalSalary -> PaymentStatus.PARTIAL
            paidAmount == totalSalary -> PaymentStatus.PAID
            else -> PaymentStatus.OVERPAID
        }
    }

    fun computeCycleMetrics(
        cycle: SalaryCycleEntity,
        payments: List<SalaryPaymentEntity>
    ): CycleFinancialMetrics {
        return calculateCycleMetrics(cycle, emptyList(), payments)
    }

    fun calculateCycleMetrics(
        cycle: SalaryCycleEntity,
        cycleLectures: List<LectureEntity>,
        cyclePayments: List<SalaryPaymentEntity>
    ): CycleFinancialMetrics {
        val activeLectures = cycleLectures.filter { it.status != "DELETED" }
        val activePayments = cyclePayments.filter { it.status != "DELETED" }

        val completed = if (activeLectures.isNotEmpty()) activeLectures.size else 0
        val contracted = cycle.contractedLectures.coerceAtLeast(1)
        val totalSalary = cycle.totalSalary.coerceAtLeast(0.0)

        val ratePerLecture = totalSalary / contracted.toDouble()
        val earned = min(completed * ratePerLecture, totalSalary)

        val paid = activePayments.sumOf { it.amount }

        val isClosed = cycle.status == "CLOSED" || completed >= contracted

        val outstanding = if (isClosed) {
            (totalSalary - paid).coerceAtLeast(0.0)
        } else {
            (earned - paid).coerceAtLeast(0.0)
        }

        val advance = (paid - (if (isClosed) totalSalary else earned)).coerceAtLeast(0.0)
        val remainingContract = (totalSalary - paid).coerceAtLeast(0.0)
        val remainingLec = (contracted - completed).coerceAtLeast(0)

        val paymentStatus = calculatePaymentStatus(paid, totalSalary)

        return CycleFinancialMetrics(
            completedLectures = completed,
            contractedLectures = contracted,
            remainingLectures = remainingLec,
            totalSalary = totalSalary,
            earnedSalary = earned,
            paidAmount = paid,
            outstandingAmount = outstanding,
            advanceAmount = advance,
            remainingContractSalary = remainingContract,
            paymentStatus = paymentStatus,
            isCycleClosed = isClosed
        )
    }

    fun computeGlobalMetrics(
        tuitions: List<TuitionEntity>,
        cycles: List<SalaryCycleEntity>,
        lectures: List<LectureEntity>,
        payments: List<SalaryPaymentEntity>
    ): GlobalFinancialMetrics {
        val activeTuitions = tuitions.filter { it.status != "DELETED" }
        val activeCycles = cycles.filter { it.tuitionId in activeTuitions.map { t -> t.tuitionId } }
        val activeLectures = lectures.filter { it.status != "DELETED" }
        val activePayments = payments.filter { it.status != "DELETED" }

        var totalTargetSalary = 0.0
        var totalEarned = 0.0
        var totalPaid = activePayments.sumOf { it.amount }
        var totalDue = 0.0

        var paidCycles = 0
        var unpaidCycles = 0
        var partialCycles = 0

        for (cycle in activeCycles) {
            val cLectures = activeLectures.filter { it.cycleId == cycle.cycleId }
            val cPayments = activePayments.filter { it.cycleId == cycle.cycleId }
            val metrics = calculateCycleMetrics(cycle, cLectures, cPayments)

            totalTargetSalary += metrics.totalSalary
            totalEarned += metrics.earnedSalary
            totalDue += metrics.outstandingAmount

            when (metrics.paymentStatus) {
                PaymentStatus.PAID -> paidCycles++
                PaymentStatus.PARTIAL -> partialCycles++
                PaymentStatus.UNPAID -> unpaidCycles++
                PaymentStatus.OVERPAID -> paidCycles++
            }
        }

        return GlobalFinancialMetrics(
            totalTargetSalary = totalTargetSalary,
            totalEarned = totalEarned,
            totalPaid = totalPaid,
            totalDue = totalDue,
            paidCyclesCount = paidCycles,
            unpaidCyclesCount = unpaidCycles,
            partialCyclesCount = partialCycles
        )
    }
}
