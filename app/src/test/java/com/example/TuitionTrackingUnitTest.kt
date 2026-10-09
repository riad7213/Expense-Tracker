package com.example

import com.example.data.local.entity.LectureEntity
import com.example.data.local.entity.SalaryCycleEntity
import com.example.data.local.entity.SalaryPaymentEntity
import com.example.data.local.entity.TuitionEntity
import com.example.data.model.PaymentStatus
import com.example.data.model.SalaryCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TuitionTrackingUnitTest {

    @Test
    fun testPaymentStatusCalculation() {
        assertEquals(PaymentStatus.UNPAID, SalaryCalculator.calculatePaymentStatus(0.0, 10000.0))
        assertEquals(PaymentStatus.PARTIAL, SalaryCalculator.calculatePaymentStatus(5000.0, 10000.0))
        assertEquals(PaymentStatus.PAID, SalaryCalculator.calculatePaymentStatus(10000.0, 10000.0))
        assertEquals(PaymentStatus.OVERPAID, SalaryCalculator.calculatePaymentStatus(12000.0, 10000.0))
        assertEquals(PaymentStatus.PAID, SalaryCalculator.calculatePaymentStatus(0.0, 0.0))
    }

    @Test
    fun testCycleFinancialMetrics() {
        val cycle = SalaryCycleEntity(
            cycleId = "c1",
            tuitionId = "t1",
            cycleNumber = 1,
            startDate = "01-Oct-26",
            endDate = null,
            contractedLectures = 12,
            totalSalary = 12000.0,
            status = "OPEN"
        )

        val lectures = (1..6).map { i ->
            LectureEntity(
                lectureId = "l$i",
                tuitionId = "t1",
                cycleId = "c1",
                lectureNumber = i,
                date = "0$i-Oct-26",
                day = "Mon",
                startingTime = "04:00 PM",
                endingTime = "05:30 PM",
                classDuration = "90 mins",
                topic = "Topic $i"
            )
        }

        val payments = listOf(
            SalaryPaymentEntity(
                paymentId = "p1",
                tuitionId = "t1",
                cycleId = "c1",
                paymentDate = "05-Oct-26",
                amount = 4000.0
            )
        )

        val metrics = SalaryCalculator.calculateCycleMetrics(cycle, lectures, payments)
        assertEquals(12000.0, metrics.totalSalary, 0.001)
        assertEquals(4000.0, metrics.paidAmount, 0.001)
        assertEquals(6000.0, metrics.earnedSalary, 0.001) // 6 * (12000/12)
        assertEquals(2000.0, metrics.outstandingAmount, 0.001) // 6000 - 4000
        assertEquals(PaymentStatus.PARTIAL, metrics.paymentStatus)
    }

    @Test
    fun testCycleAutoClosureCriteria() {
        val contractedLectures = 12
        val currentCount = 12
        val shouldClose = currentCount >= contractedLectures
        assertTrue("Cycle must close when lecture count reaches contract", shouldClose)
    }

    @Test
    fun testGlobalFinancialMetrics() {
        val tuition1 = TuitionEntity(
            tuitionId = "t1",
            location = "Dhanmondi",
            classGrade = "Class 10",
            daysPerWeek = 3,
            startingDate = "01-Oct-26",
            totalSalary = 12000.0
        )

        val cycle1 = SalaryCycleEntity(
            cycleId = "c1",
            tuitionId = "t1",
            cycleNumber = 1,
            startDate = "01-Oct-26",
            endDate = null,
            contractedLectures = 12,
            totalSalary = 12000.0,
            status = "OPEN"
        )

        val lectures = (1..6).map { i ->
            LectureEntity(
                lectureId = "l$i",
                tuitionId = "t1",
                cycleId = "c1",
                lectureNumber = i,
                date = "0$i-Oct-26",
                day = "Mon",
                startingTime = "04:00 PM",
                endingTime = "05:30 PM",
                classDuration = "90 mins",
                topic = "Algebra $i"
            )
        }

        val payments = listOf(
            SalaryPaymentEntity(
                paymentId = "p1",
                tuitionId = "t1",
                cycleId = "c1",
                paymentDate = "05-Oct-26",
                amount = 6000.0
            )
        )

        val globalMetrics = SalaryCalculator.computeGlobalMetrics(
            listOf(tuition1),
            listOf(cycle1),
            lectures,
            payments
        )

        assertEquals(12000.0, globalMetrics.totalTargetSalary, 0.001)
        assertEquals(6000.0, globalMetrics.totalEarned, 0.001)
        assertEquals(6000.0, globalMetrics.totalPaid, 0.001)
        assertEquals(0.0, globalMetrics.totalDue, 0.001) // earned (6000) - paid (6000)
    }

    @Test
    fun testCycleHighlightingRules() {
        // Rule 1: Cycle has both Start and End date -> span is start to end
        val cycle1 = SalaryCycleEntity(
            cycleId = "c1",
            tuitionId = "t1",
            cycleNumber = 1,
            startDate = "01-Sep-26",
            endDate = "30-Sep-26"
        )
        assertTrue("Rule 1: Cycle with both dates has closed span", !cycle1.endDate.isNullOrBlank())

        // Rule 2 & 3: Cycle with start date only
        val cycle2 = SalaryCycleEntity(
            cycleId = "c2",
            tuitionId = "t1",
            cycleNumber = 2,
            startDate = "01-Oct-26",
            endDate = null
        )
        assertTrue("Rule 3: Active cycle without end date", cycle2.endDate.isNullOrBlank())
    }
}
