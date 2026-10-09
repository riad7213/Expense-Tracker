package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LectureEntity
import com.example.data.local.entity.SalaryPaymentEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.model.TuitionDetailData
import com.example.ui.components.EmbeddedCalendar
import com.example.util.DateTimeUtils

enum class TuitionDetailTab(val title: String) {
    LECTURES("Lectures"),
    PAYMENTS("Payments"),
    DETAILS("Details")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuitionDetailScreen(
    tuitionDetail: TuitionDetailData?,
    students: List<StudentEntity>,
    onBackClick: () -> Unit,
    onEditTuitionClick: () -> Unit,
    onDeleteTuitionClick: () -> Unit,
    onArchiveToggleClick: () -> Unit = {},
    onStudentClick: (studentId: String) -> Unit = {},
    onSaveStudent: (
        id: String?,
        tuitionId: String,
        name: String,
        nickname: String,
        contact: String,
        fatherContact: String,
        motherContact: String,
        whatsApp: String,
        telegram: String,
        discord: String,
        isOnline: Boolean,
        type: String,
        approach: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onDeleteStudent: (studentId: String) -> Unit = {},
    onCreateNewCycleClick: (startDate: String) -> Unit = {},
    onRecordNewLectureClick: () -> Unit = {},
    onLectureClick: (lectureId: String) -> Unit,
    onCycleClick: (cycleId: String) -> Unit,
    onRecordPayment: (cycleId: String, tuitionId: String, amount: Double, paymentDate: String, paymentMethod: String, notes: String) -> Unit = { _, _, _, _, _, _ -> },
    onUpdatePayment: (paymentId: String, amount: Double, paymentDate: String, paymentMethod: String, notes: String) -> Unit = { _, _, _, _, _ -> },
    onDeletePayment: (paymentId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (tuitionDetail == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tuition Details") },
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
                Text("Tuition details not found.")
            }
        }
        return
    }

    val tuition = tuitionDetail.tuition
    var selectedTab by remember { mutableStateOf(TuitionDetailTab.LECTURES) }
    var topMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Student detail/edit modal state
    var showStudentDialog by remember { mutableStateOf(false) }
    var studentBeingEdited by remember { mutableStateOf<StudentEntity?>(null) }

    // Payment dialog state
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var paymentBeingEdited by remember { mutableStateOf<SalaryPaymentEntity?>(null) }

    // Create cycle dialog state
    var showCreateCycleDialog by remember { mutableStateOf(false) }

    // Calendar state for Lectures tab
    val (currYear, currMonth) = remember { DateTimeUtils.getCurrentYearMonth() }
    var calYear by remember { mutableIntStateOf(currYear) }
    var calMonth by remember { mutableIntStateOf(currMonth) }
    var selectedDate by remember { mutableStateOf(DateTimeUtils.getCurrentDate()) }

    val activeCycle = tuitionDetail.activeCycle ?: tuitionDetail.cycles.firstOrNull()

    // Salary snapshots
    val cycleSalary = activeCycle?.cycle?.totalSalary ?: tuition.totalSalary
    val cycleLecturesCount = activeCycle?.cycle?.contractedLectures ?: tuition.contractedLectures
    val salaryPerLecture = if (cycleLecturesCount > 0) cycleSalary / cycleLecturesCount else 0.0

    // Lectures for selected date
    val selectedDateLectures = remember(tuitionDetail.allLectures, selectedDate) {
        tuitionDetail.allLectures
            .filter { it.date.equals(selectedDate, ignoreCase = true) }
            .sortedBy { it.startingTime }
    }

    // Dates with lectures for embedded calendar dots
    val lectureDates = remember(tuitionDetail.allLectures) {
        tuitionDetail.allLectures.map { it.date }.toSet()
    }

    val sortedCycles = remember(tuitionDetail.cycles) {
        tuitionDetail.cycles.sortedByDescending { it.cycle.cycleNumber }
    }

    val sortedLectures = remember(tuitionDetail.allLectures) {
        tuitionDetail.allLectures.sortedWith(
            compareByDescending<LectureEntity> { it.date }.thenByDescending { it.startingTime }
        )
    }

    val sortedPayments = remember(tuitionDetail.allPayments) {
        tuitionDetail.allPayments.sortedByDescending { it.paymentDate }
    }

    // Financial totals across all cycles/payments
    val totalTargetSalary = remember(tuitionDetail.cycles, tuition.totalSalary) {
        if (tuitionDetail.cycles.isNotEmpty()) {
            tuitionDetail.cycles.sumOf { it.cycle.totalSalary }
        } else {
            tuition.totalSalary
        }
    }
    val totalEarnedSalary = remember(tuitionDetail.cycles) {
        tuitionDetail.cycles.sumOf { it.earnedSalary }
    }
    val totalPaidAmount = remember(tuitionDetail.allPayments) {
        tuitionDetail.allPayments.sumOf { it.amount }
    }
    val totalDueAmount = remember(totalEarnedSalary, totalPaidAmount) {
        (totalEarnedSalary - totalPaidAmount).coerceAtLeast(0.0)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "${tuition.location.ifBlank { "Location" }} (${tuition.classGrade.ifBlank { "Class" }})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${if (tuition.tuitionType.equals("Online", ignoreCase = true)) "Online" else "Physical"} · ${tuition.daysPerWeek} days/week",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick, modifier = Modifier.testTag("tuition_detail_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { topMenuExpanded = true }, modifier = Modifier.testTag("tuition_detail_menu_btn")) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = topMenuExpanded,
                            onDismissRequest = { topMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Start New Cycle") },
                                onClick = {
                                    topMenuExpanded = false
                                    showCreateCycleDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Tuition") },
                                onClick = {
                                    topMenuExpanded = false
                                    onEditTuitionClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(if (tuition.status == "ARCHIVED") "Restore Tuition" else "Archive Tuition")
                                },
                                onClick = {
                                    topMenuExpanded = false
                                    onArchiveToggleClick()
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete Tuition", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    topMenuExpanded = false
                                    showDeleteConfirmDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Required Tabs: Lectures | Payments | Details
                PrimaryTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("tuition_detail_tab_row")
                ) {
                    TuitionDetailTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("tuition_detail_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                TuitionDetailTab.LECTURES -> {
                    FloatingActionButton(
                        onClick = onRecordNewLectureClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("tuition_lectures_fab_add")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Record Lecture")
                    }
                }
                TuitionDetailTab.PAYMENTS -> {
                    FloatingActionButton(
                        onClick = {
                            paymentBeingEdited = null
                            showRecordPaymentDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("tuition_payments_fab_add")
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = "Record Payment")
                    }
                }
                TuitionDetailTab.DETAILS -> {
                    FloatingActionButton(
                        onClick = {
                            studentBeingEdited = null
                            showStudentDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("tuition_details_fab_add_student")
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Add Student")
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                TuitionDetailTab.LECTURES -> {
                    LecturesTabView(
                        tuition = tuition,
                        activeCycle = activeCycle,
                        calYear = calYear,
                        calMonth = calMonth,
                        selectedDate = selectedDate,
                        lectureDates = lectureDates,
                        selectedDateLectures = selectedDateLectures,
                        allLectures = sortedLectures,
                        cycles = tuitionDetail.cycles,
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
                        onLectureClick = onLectureClick,
                        onRecordNewLectureClick = onRecordNewLectureClick,
                        onCreateNewCycleClick = { showCreateCycleDialog = true }
                    )
                }

                TuitionDetailTab.PAYMENTS -> {
                    PaymentsTabView(
                        tuition = tuition,
                        activeCycle = activeCycle,
                        cycles = sortedCycles,
                        payments = sortedPayments,
                        totalTargetSalary = totalTargetSalary,
                        totalEarnedSalary = totalEarnedSalary,
                        totalPaidAmount = totalPaidAmount,
                        totalDueAmount = totalDueAmount,
                        onCycleClick = onCycleClick,
                        onCreateNewCycleClick = { showCreateCycleDialog = true },
                        onRecordPaymentClick = {
                            paymentBeingEdited = null
                            showRecordPaymentDialog = true
                        },
                        onEditPaymentClick = { payment ->
                            paymentBeingEdited = payment
                            showRecordPaymentDialog = true
                        },
                        onDeletePaymentClick = { paymentId ->
                            onDeletePayment(paymentId)
                        }
                    )
                }

                TuitionDetailTab.DETAILS -> {
                    DetailsTabView(
                        tuition = tuition,
                        activeCycle = activeCycle,
                        students = students,
                        cycles = sortedCycles,
                        cycleSalary = cycleSalary,
                        cycleLecturesCount = cycleLecturesCount,
                        salaryPerLecture = salaryPerLecture,
                        onEditTuitionClick = onEditTuitionClick,
                        onArchiveToggleClick = onArchiveToggleClick,
                        onDeleteTuitionClick = { showDeleteConfirmDialog = true },
                        onAddStudentClick = {
                            studentBeingEdited = null
                            showStudentDialog = true
                        },
                        onStudentClick = { student ->
                            studentBeingEdited = student
                            showStudentDialog = true
                        },
                        onCreateNewCycleClick = { showCreateCycleDialog = true },
                        onCycleClick = onCycleClick
                    )
                }
            }
        }
    }

    // Create New Cycle Dialog
    if (showCreateCycleDialog) {
        CreateNewCycleDialog(
            tuitionId = tuition.tuitionId,
            activeCycle = activeCycle,
            onDismiss = { showCreateCycleDialog = false },
            onCreateCycle = { startDate ->
                onCreateNewCycleClick(startDate)
                showCreateCycleDialog = false
            }
        )
    }

    // Student Detail / Edit Dialog
    if (showStudentDialog) {
        StudentDetailDialog(
            student = studentBeingEdited,
            tuitionId = tuition.tuitionId,
            onDismiss = { showStudentDialog = false },
            onSave = { id, tId, name, nick, contact, father, mother, wa, tg, disc, isOnline, type, approach ->
                onSaveStudent(id, tId, name, nick, contact, father, mother, wa, tg, disc, isOnline, type, approach)
                showStudentDialog = false
            },
            onDelete = { studentId ->
                onDeleteStudent(studentId)
                showStudentDialog = false
            }
        )
    }

    // Record / Edit Payment Dialog
    if (showRecordPaymentDialog) {
        val targetCycleId = paymentBeingEdited?.cycleId
            ?: activeCycle?.cycle?.cycleId
            ?: tuitionDetail.cycles.firstOrNull()?.cycle?.cycleId
            ?: ""

        RecordPaymentDialog(
            cycleId = targetCycleId,
            tuitionId = tuition.tuitionId,
            suggestedAmount = paymentBeingEdited?.amount ?: activeCycle?.outstandingAmount ?: 0.0,
            cycleTargetSalary = activeCycle?.cycle?.totalSalary ?: tuition.totalSalary,
            cycleTotalPaid = activeCycle?.paidAmount ?: 0.0,
            existingPayment = paymentBeingEdited,
            onDismiss = {
                showRecordPaymentDialog = false
                paymentBeingEdited = null
            },
            onSavePayment = { cId, tId, amount, date, method, notes ->
                onRecordPayment(cId, tId, amount, date, method, notes)
                showRecordPaymentDialog = false
                paymentBeingEdited = null
            },
            onUpdatePayment = { paymentId, amount, date, method, notes ->
                onUpdatePayment(paymentId, amount, date, method, notes)
                showRecordPaymentDialog = false
                paymentBeingEdited = null
            },
            onDeletePayment = { paymentId ->
                onDeletePayment(paymentId)
                showRecordPaymentDialog = false
                paymentBeingEdited = null
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Tuition") },
            text = { Text("Are you sure you want to delete this tuition profile? Past cycle and lecture records will be preserved.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteTuitionClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 1: LECTURES VIEW
// -------------------------------------------------------------
@Composable
private fun LecturesTabView(
    tuition: com.example.data.local.entity.TuitionEntity,
    activeCycle: com.example.data.model.CycleSummary?,
    calYear: Int,
    calMonth: Int,
    selectedDate: String,
    lectureDates: Set<String>,
    selectedDateLectures: List<LectureEntity>,
    allLectures: List<LectureEntity>,
    cycles: List<com.example.data.model.CycleSummary>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (String) -> Unit,
    onLectureClick: (String) -> Unit,
    onRecordNewLectureClick: () -> Unit,
    onCreateNewCycleClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Cycle Progress summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (activeCycle != null) "Active Cycle #${activeCycle.cycle.cycleNumber}" else "No Active Cycle",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (activeCycle != null) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (activeCycle.isOpen) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (activeCycle.isOpen) "ONGOING" else "CLOSED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeCycle.isOpen) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = onCreateNewCycleClick,
                            modifier = Modifier.testTag("lectures_add_cycle_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Cycle")
                        }
                    }

                    if (activeCycle != null) {
                        val completed = activeCycle.completedLectures
                        val contracted = activeCycle.cycle.contractedLectures
                        val progress = if (contracted > 0) (completed.toFloat() / contracted.toFloat()).coerceIn(0f, 1f) else 0f

                        Text(
                            text = "Lectures: $completed of $contracted completed",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Recorded: ${allLectures.size} lectures",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onRecordNewLectureClick) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record Lecture")
                        }
                    }
                }
            }
        }

        // Embedded Calendar
        item {
            EmbeddedCalendar(
                selectedYear = calYear,
                selectedMonth = calMonth,
                selectedDateStr = selectedDate,
                lectureDates = lectureDates,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onDateSelected = onDateSelected,
                cycles = cycles
            )
        }

        // Activity of Selected Date
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Activity · $selectedDate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (selectedDateLectures.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No lectures on this date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    selectedDateLectures.forEach { lecture ->
                        val cycle = cycles.find { it.cycle.cycleId == lecture.cycleId }
                        val cycleNum = cycle?.cycle?.cycleNumber ?: 1
                        val durationMins = DateTimeUtils.calculateMinutesBetween(lecture.startingTime, lecture.endingTime)
                        val durationFormatted = DateTimeUtils.formatMinutesToHoursAndMinutes(durationMins)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLectureClick(lecture.lectureId) }
                                .testTag("activity_lecture_${lecture.lectureId}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${lecture.lectureNumber}  Lecture ${lecture.lectureNumber}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Cycle $cycleNum",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "${lecture.startingTime} – ${lecture.endingTime}  ($durationFormatted)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
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

        // All Lectures Section (Newest first)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Lectures (${allLectures.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (allLectures.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No lectures recorded yet. Tap '+ Record Lecture' to start.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(allLectures, key = { it.lectureId }) { lecture ->
                val cycle = cycles.find { it.cycle.cycleId == lecture.cycleId }
                val cycleNum = cycle?.cycle?.cycleNumber ?: 1
                val durationMins = DateTimeUtils.calculateMinutesBetween(lecture.startingTime, lecture.endingTime)
                val durationFormatted = DateTimeUtils.formatMinutesToHoursAndMinutes(durationMins)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLectureClick(lecture.lectureId) }
                        .testTag("all_lecture_${lecture.lectureId}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${lecture.date} (${lecture.day})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "Lecture #${lecture.lectureNumber} · Cycle $cycleNum",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "${lecture.startingTime} – ${lecture.endingTime} · $durationFormatted",
                            style = MaterialTheme.typography.bodyMedium,
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

                        if (lecture.homework.isNotBlank()) {
                            Text(
                                text = "HW: ${lecture.homework}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: PAYMENTS VIEW
// -------------------------------------------------------------
@Composable
private fun PaymentsTabView(
    tuition: com.example.data.local.entity.TuitionEntity,
    activeCycle: com.example.data.model.CycleSummary?,
    cycles: List<com.example.data.model.CycleSummary>,
    payments: List<SalaryPaymentEntity>,
    totalTargetSalary: Double,
    totalEarnedSalary: Double,
    totalPaidAmount: Double,
    totalDueAmount: Double,
    onCycleClick: (String) -> Unit,
    onCreateNewCycleClick: () -> Unit = {},
    onRecordPaymentClick: () -> Unit,
    onEditPaymentClick: (SalaryPaymentEntity) -> Unit,
    onDeletePaymentClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall Financial Metrics Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Financial Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Target Salary", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DateTimeUtils.formatCurrencyBDT(totalTargetSalary), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Earned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DateTimeUtils.formatCurrencyBDT(totalEarnedSalary), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("Paid", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DateTimeUtils.formatCurrencyBDT(totalPaidAmount), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        }
                        Column {
                            Text("Due", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                DateTimeUtils.formatCurrencyBDT(totalDueAmount),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (totalDueAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Active Cycle Payment Card
        if (activeCycle != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Cycle #${activeCycle.cycle.cycleNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (activeCycle.paymentStatus) {
                                    com.example.data.model.PaymentStatus.PAID -> Color(0xFFD1FAE5)
                                    com.example.data.model.PaymentStatus.PARTIAL -> Color(0xFFFEF3C7)
                                    com.example.data.model.PaymentStatus.UNPAID -> MaterialTheme.colorScheme.errorContainer
                                    else -> Color(0xFFE0E7FF)
                                }
                            ) {
                                Text(
                                    text = activeCycle.paymentStatus.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (activeCycle.paymentStatus) {
                                        com.example.data.model.PaymentStatus.PAID -> Color(0xFF065F46)
                                        com.example.data.model.PaymentStatus.PARTIAL -> Color(0xFF92400E)
                                        com.example.data.model.PaymentStatus.UNPAID -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Paid ${DateTimeUtils.formatCurrencyBDT(activeCycle.paidAmount)} of ${DateTimeUtils.formatCurrencyBDT(activeCycle.cycle.totalSalary)} target",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        LinearProgressIndicator(
                            progress = { activeCycle.financialProgressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = Color(0xFF166534),
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Due: ${DateTimeUtils.formatCurrencyBDT(activeCycle.outstandingAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (activeCycle.outstandingAmount > 0) MaterialTheme.colorScheme.error else Color(0xFF166534)
                            )

                            Button(
                                onClick = onRecordPaymentClick,
                                modifier = Modifier.testTag("record_payment_active_cycle_btn")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record Payment")
                            }
                        }
                    }
                }
            }
        }

        // Payments History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment History (${payments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onRecordPaymentClick,
                    modifier = Modifier.testTag("tuition_add_payment_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Payment")
                }
            }
        }

        if (payments.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No payments recorded yet. Tap 'Add Payment' to log tuition income.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(payments, key = { it.paymentId }) { payment ->
                val cycle = cycles.find { it.cycle.cycleId == payment.cycleId }
                val cycleNum = cycle?.cycle?.cycleNumber ?: 1

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditPaymentClick(payment) }
                        .testTag("payment_item_${payment.paymentId}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateTimeUtils.formatCurrencyBDT(payment.amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Received",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Date: ${payment.paymentDate} · Cycle #$cycleNum",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row {
                                IconButton(
                                    onClick = { onEditPaymentClick(payment) },
                                    modifier = Modifier.size(32.dp).testTag("edit_payment_${payment.paymentId}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Payment", modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { onDeletePaymentClick(payment.paymentId) },
                                    modifier = Modifier.size(32.dp).testTag("delete_payment_${payment.paymentId}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Payment", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        if (payment.note.isNotBlank()) {
                            Text(
                                text = "Note: ${payment.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Salary Cycles Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Salary Cycles Breakdown (${cycles.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = onCreateNewCycleClick,
                    modifier = Modifier.testTag("payments_add_cycle_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Cycle")
                }
            }
        }

        items(cycles, key = { it.cycle.cycleId }) { cycleSummary ->
            val cycle = cycleSummary.cycle
            val spanText = if (cycle.endDate != null && cycle.endDate.isNotBlank()) {
                "${cycle.startDate} – ${cycle.endDate}"
            } else {
                "${cycle.startDate} – Ongoing"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCycleClick(cycle.cycleId) }
                    .testTag("cycle_card_${cycle.cycleId}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cycle ${cycle.cycleNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (cycleSummary.isClosed) MaterialTheme.colorScheme.surfaceVariant
                            else Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = if (cycleSummary.isClosed) "CLOSED" else "ONGOING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (cycleSummary.isClosed) MaterialTheme.colorScheme.onSurfaceVariant
                                else Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = spanText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Lectures: ${cycleSummary.completedLectures}/${cycle.contractedLectures} · Target: ${DateTimeUtils.formatCurrencyBDT(cycle.totalSalary)}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Paid: ${DateTimeUtils.formatCurrencyBDT(cycleSummary.paidAmount)} · Due: ${DateTimeUtils.formatCurrencyBDT(cycleSummary.outstandingAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (cycleSummary.outstandingAmount > 0) MaterialTheme.colorScheme.error else Color(0xFF166534)
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "View Cycle Details",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: DETAILS VIEW
// -------------------------------------------------------------
@Composable
private fun DetailsTabView(
    tuition: com.example.data.local.entity.TuitionEntity,
    activeCycle: com.example.data.model.CycleSummary?,
    students: List<StudentEntity>,
    cycles: List<com.example.data.model.CycleSummary>,
    cycleSalary: Double,
    cycleLecturesCount: Int,
    salaryPerLecture: Double,
    onEditTuitionClick: () -> Unit,
    onArchiveToggleClick: () -> Unit,
    onDeleteTuitionClick: () -> Unit,
    onAddStudentClick: () -> Unit,
    onStudentClick: (StudentEntity) -> Unit,
    onCreateNewCycleClick: () -> Unit,
    onCycleClick: (String) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Compact Tuition Summary
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${if (tuition.tuitionType.equals("Online", ignoreCase = true)) "Online" else "Physical"} · ${tuition.daysPerWeek} d/w",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (tuition.status == "ARCHIVED") MaterialTheme.colorScheme.surfaceVariant else Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = tuition.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (tuition.status == "ARCHIVED") MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${DateTimeUtils.formatCurrencyBDT(cycleSalary)} / cycle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$cycleLecturesCount lectures / cycle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${DateTimeUtils.formatCurrencyBDT(salaryPerLecture)} / lecture",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (tuition.startingDate.isNotBlank()) {
                        Text(
                            text = "Starting Date: ${tuition.startingDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Location hyperlink if available
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Location: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (tuition.mapLink.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { openExternalMap(context, tuition.mapLink) }
                                    .testTag("location_map_link")
                            ) {
                                Text(
                                    text = tuition.location.ifBlank { "View Map" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open in Maps",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else {
                            Text(
                                text = tuition.location.ifBlank { "Unspecified" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (activeCycle != null) {
                        Text(
                            text = "Active Cycle: #${activeCycle.cycle.cycleNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Cycle Progress: ${activeCycle.completedLectures}/${activeCycle.cycle.contractedLectures} lectures",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Status: ${if (activeCycle.isOpen) "OPEN" else "CLOSED"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (activeCycle.isOpen) Color(0xFF166534) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section: Students +
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Students (${students.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onAddStudentClick,
                    modifier = Modifier.size(36.dp).testTag("tuition_add_student_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Student")
                }
            }
        }

        if (students.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No students added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onAddStudentClick) {
                            Text("+ Add Student")
                        }
                    }
                }
            }
        } else {
            items(students, key = { it.studentId }) { student ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStudentClick(student) }
                        .testTag("student_card_${student.studentId}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = student.displayName.ifBlank { "Student" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (student.nickname.isNotBlank() && student.nickname != student.name) {
                                    Text(
                                        text = "Full: ${student.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row {
                                if (student.whatsApp.isNotBlank() || student.contact.isNotBlank()) {
                                    val waNum = student.whatsApp.ifBlank { student.contact }
                                    IconButton(
                                        onClick = { openWhatsApp(context, waNum) },
                                        modifier = Modifier.size(36.dp).testTag("wa_student_${student.studentId}")
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                                    }
                                }
                                if (student.contact.isNotBlank()) {
                                    IconButton(
                                        onClick = { dialPhoneNumber(context, student.contact) },
                                        modifier = Modifier.size(36.dp).testTag("dial_student_${student.studentId}")
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Dial", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        if (student.contact.isNotBlank()) {
                            Text(
                                text = "Contact: ${student.contact}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (student.fatherContact.isNotBlank() || student.motherContact.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (student.fatherContact.isNotBlank()) {
                                    Text(
                                        text = "Father: ${student.fatherContact}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (student.motherContact.isNotBlank()) {
                                    Text(
                                        text = "Mother: ${student.motherContact}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (student.approach.isNotBlank()) {
                            Text(
                                text = "Notes: ${student.approach}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Section: Salary Cycles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Salary Cycles (${cycles.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onCreateNewCycleClick,
                    modifier = Modifier.testTag("tuition_details_add_cycle_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Cycle")
                }
            }
        }

        if (cycles.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No cycles recorded yet. Tap 'New Cycle' to create Cycle #1.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(cycles, key = { it.cycle.cycleId }) { cycleSummary ->
                val cycle = cycleSummary.cycle
                val statusText = if (cycleSummary.isClosed) "CLOSED" else "ONGOING"
                val spanText = if (cycle.endDate != null && cycle.endDate.isNotBlank()) {
                    "${cycle.startDate} – ${cycle.endDate}"
                } else {
                    "${cycle.startDate} – Ongoing"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCycleClick(cycle.cycleId) }
                        .testTag("details_cycle_card_${cycle.cycleId}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cycle ${cycle.cycleNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (cycleSummary.isClosed) MaterialTheme.colorScheme.surfaceVariant
                                else Color(0xFFD1FAE5)
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cycleSummary.isClosed) MaterialTheme.colorScheme.onSurfaceVariant
                                    else Color(0xFF065F46),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = spanText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${cycleSummary.completedLectures}/${cycle.contractedLectures} Lectures · Total ${DateTimeUtils.formatCurrencyBDT(cycle.totalSalary)}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Paid ${DateTimeUtils.formatCurrencyBDT(cycleSummary.paidAmount)} · Due ${DateTimeUtils.formatCurrencyBDT(cycleSummary.outstandingAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (cycleSummary.outstandingAmount > 0) MaterialTheme.colorScheme.error else Color(0xFF166534),
                                fontWeight = FontWeight.SemiBold
                            )

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View Cycle Details",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Management Actions Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tuition Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = onEditTuitionClick,
                        modifier = Modifier.fillMaxWidth().testTag("tuition_manage_edit_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Tuition Profile")
                    }

                    OutlinedButton(
                        onClick = onArchiveToggleClick,
                        modifier = Modifier.fillMaxWidth().testTag("tuition_manage_archive_btn")
                    ) {
                        Text(if (tuition.status == "ARCHIVED") "Restore to Active Tuitions" else "Archive Tuition")
                    }

                    Button(
                        onClick = onDeleteTuitionClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                        modifier = Modifier.fillMaxWidth().testTag("tuition_manage_delete_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Tuition")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STUDENT DETAIL & EDIT DIALOG
// -------------------------------------------------------------
@Composable
fun StudentDetailDialog(
    student: StudentEntity?,
    tuitionId: String,
    onDismiss: () -> Unit,
    onSave: (
        id: String?,
        tuitionId: String,
        name: String,
        nickname: String,
        contact: String,
        fatherContact: String,
        motherContact: String,
        whatsApp: String,
        telegram: String,
        discord: String,
        isOnline: Boolean,
        type: String,
        approach: String
    ) -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf(student?.name ?: "") }
    var nickname by remember { mutableStateOf(student?.nickname ?: "") }
    var contact by remember { mutableStateOf(student?.contact ?: "") }
    var fatherContact by remember { mutableStateOf(student?.fatherContact ?: "") }
    var motherContact by remember { mutableStateOf(student?.motherContact ?: "") }
    var whatsApp by remember { mutableStateOf(student?.whatsApp ?: "") }
    var telegram by remember { mutableStateOf(student?.telegram ?: "") }
    var discord by remember { mutableStateOf(student?.discord ?: "") }
    var approach by remember { mutableStateOf(student?.approach ?: "") }

    var showConfirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (student != null) "Student Details" else "Add Student", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname (Primary Display)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_input_nickname")
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_input_name")
                )

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Number") },
                    trailingIcon = {
                        if (contact.isNotBlank()) {
                            IconButton(onClick = { dialPhoneNumber(context, contact) }) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_input_contact")
                )

                OutlinedTextField(
                    value = fatherContact,
                    onValueChange = { fatherContact = it },
                    label = { Text("Father's Contact") },
                    trailingIcon = {
                        if (fatherContact.isNotBlank()) {
                            IconButton(onClick = { dialPhoneNumber(context, fatherContact) }) {
                                Icon(Icons.Default.Call, contentDescription = "Call Father", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_input_father_contact")
                )

                OutlinedTextField(
                    value = motherContact,
                    onValueChange = { motherContact = it },
                    label = { Text("Mother's Contact") },
                    trailingIcon = {
                        if (motherContact.isNotBlank()) {
                            IconButton(onClick = { dialPhoneNumber(context, motherContact) }) {
                                Icon(Icons.Default.Call, contentDescription = "Call Mother", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_input_mother_contact")
                )

                OutlinedTextField(
                    value = whatsApp,
                    onValueChange = { whatsApp = it },
                    label = { Text("WhatsApp Number") },
                    trailingIcon = {
                        if (whatsApp.isNotBlank()) {
                            IconButton(onClick = { openWhatsApp(context, whatsApp) }) {
                                Icon(Icons.Default.Chat, contentDescription = "Open WhatsApp", tint = Color(0xFF25D366))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_input_whatsapp")
                )

                OutlinedTextField(
                    value = telegram,
                    onValueChange = { telegram = it },
                    label = { Text("Telegram Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_input_telegram")
                )

                OutlinedTextField(
                    value = discord,
                    onValueChange = { discord = it },
                    label = { Text("Discord Tag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_input_discord")
                )

                OutlinedTextField(
                    value = approach,
                    onValueChange = { approach = it },
                    label = { Text("Learning Notes / Approach") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("student_input_approach")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() || nickname.isNotBlank()) {
                        val finalName = fullName.ifBlank { nickname }
                        onSave(
                            student?.studentId,
                            tuitionId,
                            finalName,
                            nickname,
                            contact,
                            fatherContact,
                            motherContact,
                            whatsApp,
                            telegram,
                            discord,
                            student?.isOnline ?: false,
                            student?.type ?: "",
                            approach
                        )
                    }
                },
                enabled = fullName.isNotBlank() || nickname.isNotBlank(),
                modifier = Modifier.testTag("save_student_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (student != null) {
                    TextButton(
                        onClick = { showConfirmDelete = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )

    if (showConfirmDelete && student != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Delete Student") },
            text = { Text("Are you sure you want to remove this student from this tuition?") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDelete = false
                        onDelete(student.studentId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun openExternalMap(context: Context, url: String) {
    try {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open map link", Toast.LENGTH_SHORT).show()
    }
}

private fun dialPhoneNumber(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.trim()}"))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
    }
}

private fun openWhatsApp(context: Context, phone: String) {
    try {
        val cleanPhone = phone.replace("+", "").replace("-", "").replace(" ", "").trim()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone"))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
    }
}
