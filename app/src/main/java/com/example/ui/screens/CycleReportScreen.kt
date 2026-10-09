package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.LectureEntity
import com.example.data.local.entity.SalaryPaymentEntity
import com.example.data.local.entity.TuitionEntity
import com.example.data.model.CycleSummary
import com.example.ui.components.EmbeddedCalendar
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleReportScreen(
    cycleSummary: CycleSummary?,
    tuition: TuitionEntity? = null,
    allLecturesInCycle: List<LectureEntity> = emptyList(),
    onLectureClick: (String) -> Unit,
    onAddLectureClick: (tuitionId: String) -> Unit,
    onRecordPayment: (cycleId: String, tuitionId: String, amount: Double, date: String, method: String, notes: String) -> Unit,
    onUpdatePayment: (paymentId: String, amount: Double, date: String, method: String, notes: String) -> Unit,
    onDeletePayment: (paymentId: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (cycleSummary == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cycle Details") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Cycle not found")
            }
        }
        return
    }

    val cycle = cycleSummary.cycle
    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentToEdit by remember { mutableStateOf<SalaryPaymentEntity?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Calendar state
    val (currYear, currMonth) = remember { DateTimeUtils.getCurrentYearMonth() }
    var calYear by remember { mutableIntStateOf(currYear) }
    var calMonth by remember { mutableIntStateOf(currMonth) }
    var selectedDate by remember { mutableStateOf(DateTimeUtils.getCurrentDate()) }

    // Lectures for selected date
    val selectedDateLectures = remember(allLecturesInCycle, selectedDate) {
        allLecturesInCycle.filter { it.date.equals(selectedDate, ignoreCase = true) }
            .sortedBy { it.startingTime }
    }

    val lectureDates = remember(allLecturesInCycle) {
        allLecturesInCycle.map { it.date }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Cycle ${cycle.cycleNumber}", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("cycle_report_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Cycle Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Record Payment") },
                            onClick = {
                                menuExpanded = false
                                paymentToEdit = null
                                showPaymentDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (cycleSummary.outstandingAmount > 0) {
                FloatingActionButton(
                    onClick = {
                        paymentToEdit = null
                        showPaymentDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_record_cycle_payment")
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = "Record Payment")
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Location (Class) & Cycle · Lectures
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (tuition != null) {
                            Text(
                                text = "${tuition.location.ifBlank { "Location" }} (${tuition.classGrade.ifBlank { "Class" }})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cycle ${cycle.cycleNumber} · ${cycleSummary.completedLectures}/${cycle.contractedLectures} Lectures",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (cycleSummary.isClosed) MaterialTheme.colorScheme.surfaceVariant
                                else Color(0xFFD1FAE5)
                            ) {
                                Text(
                                    text = if (cycleSummary.isClosed) "CLOSED" else "OPEN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cycleSummary.isClosed) MaterialTheme.colorScheme.onSurfaceVariant
                                    else Color(0xFF065F46),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Span
                        Text(
                            text = "Span:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (cycle.endDate != null && cycle.endDate.isNotBlank()) {
                                "${cycle.startDate} – ${cycle.endDate}"
                            } else {
                                "${cycle.startDate} – Ongoing"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Salary & Lectures snapshot
                        Text(
                            text = "Salary & Lectures:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${DateTimeUtils.formatCurrencyBDT(cycle.totalSalary)} / ${cycle.contractedLectures} Lectures",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Earned: ${DateTimeUtils.formatCurrencyBDT(cycleSummary.earnedSalary)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Paid: ${DateTimeUtils.formatCurrencyBDT(cycleSummary.paidAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Due: ${DateTimeUtils.formatCurrencyBDT(cycleSummary.outstandingAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (cycleSummary.outstandingAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            if (cycleSummary.advanceAmount > 0) {
                                Text(
                                    text = "Advance: ${DateTimeUtils.formatCurrencyBDT(cycleSummary.advanceAmount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }
                }
            }

            // Calendar View with cycle's actual span highlighted & actual lecture dates marked
            item {
                EmbeddedCalendar(
                    selectedYear = calYear,
                    selectedMonth = calMonth,
                    selectedDateStr = selectedDate,
                    lectureDates = lectureDates,
                    onPreviousMonth = {
                        val prev = DateTimeUtils.getPreviousYearMonth(calYear, calMonth)
                        calYear = prev.first
                        calMonth = prev.second
                    },
                    onNextMonth = {
                        val next = DateTimeUtils.getNextYearMonth(calYear, calMonth)
                        calYear = next.first
                        calMonth = next.second
                    },
                    onDateSelected = { selectedDate = it },
                    cycles = listOf(cycleSummary)
                )
            }

            // Activity · Selected Date
            item {
                Text(
                    text = "Activity · $selectedDate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (selectedDateLectures.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No lectures on $selectedDate in this cycle",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(selectedDateLectures, key = { it.lectureId }) { lecture ->
                    val durationMins = DateTimeUtils.calculateMinutesBetween(lecture.startingTime, lecture.endingTime)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLectureClick(lecture.lectureId) }
                            .testTag("cycle_lecture_item_${lecture.lectureId}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${lecture.lectureNumber}  Lecture ${lecture.lectureNumber}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "${lecture.startingTime} – ${lecture.endingTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Duration: ${DateTimeUtils.formatMinutesToHoursAndMinutes(durationMins)} ($durationMins min)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (lecture.topic.isNotBlank()) {
                                Text(
                                    text = "Topic: ${lecture.topic}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        RecordPaymentDialog(
            cycleId = cycle.cycleId,
            tuitionId = cycle.tuitionId,
            suggestedAmount = cycleSummary.outstandingAmount,
            cycleTargetSalary = cycle.totalSalary,
            cycleTotalPaid = cycleSummary.paidAmount,
            existingPayment = paymentToEdit,
            onDismiss = {
                showPaymentDialog = false
                paymentToEdit = null
            },
            onSavePayment = { cId, tId, amt, dt, mtd, nts ->
                onRecordPayment(cId, tId, amt, dt, mtd, nts)
            },
            onUpdatePayment = { pId, amt, dt, mtd, nts ->
                onUpdatePayment(pId, amt, dt, mtd, nts)
            },
            onDeletePayment = { pId ->
                onDeletePayment(pId)
            }
        )
    }
}
