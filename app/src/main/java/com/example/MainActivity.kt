package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TuitionEntity
import com.example.ui.AppNavDestination
import com.example.ui.TuitionViewModel
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.screens.AddEditTuitionDialog
import com.example.ui.screens.AddLectureScreen
import com.example.ui.screens.CycleReportScreen
import com.example.ui.screens.GlobalLecturesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LectureDetailScreen
import com.example.ui.screens.SalaryScreen
import com.example.ui.screens.TuitionDetailScreen
import com.example.ui.screens.TuitionsScreen
import com.example.ui.theme.TuitionTrackerTheme
import com.example.util.DateTimeUtils

class MainActivity : ComponentActivity() {

    private val viewModel: TuitionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TuitionTrackerTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

private enum class ScreenView {
    MAIN_TAB,
    TUITION_DETAIL,
    CYCLE_DETAIL,
    LECTURE_DETAIL,
    ADD_LECTURE
}

@Composable
fun MainApp(viewModel: TuitionViewModel) {
    val currentTab by viewModel.currentDestination.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val allTuitions by viewModel.allTuitions.collectAsStateWithLifecycle()
    val tuitionSummaries by viewModel.tuitionSummaries.collectAsStateWithLifecycle()
    val allLectures by viewModel.allLecturesWithDetails.collectAsStateWithLifecycle()
    val currentTuitionDetail by viewModel.currentTuitionDetail.collectAsStateWithLifecycle()
    val currentStudents by viewModel.currentStudents.collectAsStateWithLifecycle()
    val selectedCycleSummary by viewModel.selectedCycleSummary.collectAsStateWithLifecycle()
    val selectedLecture by viewModel.selectedLecture.collectAsStateWithLifecycle()
    val financialMetrics by viewModel.globalFinancialMetrics.collectAsStateWithLifecycle()
    val allCycles by viewModel.allCycles.collectAsStateWithLifecycle()

    var activeScreen by remember { mutableStateOf(ScreenView.MAIN_TAB) }
    var showAddTuitionDialog by remember { mutableStateOf(false) }
    var tuitionToEdit by remember { mutableStateOf<TuitionEntity?>(null) }
    var addLectureInitialTuitionId by remember { mutableStateOf<String?>(null) }
    var addLectureInitialDate by remember { mutableStateOf(DateTimeUtils.getCurrentDate()) }
    var tuitionSearchQuery by remember { mutableStateOf("") }
    var showArchivedTuitions by remember { mutableStateOf(false) }

    // Screen navigation stack handling
    BackHandler(enabled = activeScreen != ScreenView.MAIN_TAB) {
        when (activeScreen) {
            ScreenView.CYCLE_DETAIL -> activeScreen = ScreenView.TUITION_DETAIL
            ScreenView.LECTURE_DETAIL -> activeScreen = ScreenView.MAIN_TAB
            ScreenView.ADD_LECTURE -> activeScreen = ScreenView.MAIN_TAB
            ScreenView.TUITION_DETAIL -> {
                viewModel.selectTuition(null)
                activeScreen = ScreenView.MAIN_TAB
            }
            ScreenView.MAIN_TAB -> {}
        }
    }

    Scaffold(
        bottomBar = {
            if (activeScreen == ScreenView.MAIN_TAB) {
                AppBottomNavigationBar(
                    currentDestination = currentTab,
                    onNavigate = { dest ->
                        viewModel.navigateTo(dest)
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        when (activeScreen) {
            ScreenView.MAIN_TAB -> {
                when (currentTab) {
                    AppNavDestination.HOME -> HomeScreen(
                        selectedDate = selectedDate,
                        onDateSelected = { viewModel.selectDate(it) },
                        allLectures = allLectures,
                        tuitionSummaries = tuitionSummaries,
                        allTuitions = allTuitions,
                        financialMetrics = financialMetrics,
                        onAddTuitionClick = {
                            tuitionToEdit = null
                            showAddTuitionDialog = true
                        },
                        onTuitionClick = { tuitionId ->
                            viewModel.selectTuition(tuitionId)
                            activeScreen = ScreenView.TUITION_DETAIL
                        },
                        onAddLectureClick = { tId, date ->
                            addLectureInitialTuitionId = tId
                            addLectureInitialDate = date
                            activeScreen = ScreenView.ADD_LECTURE
                        },
                        onLectureClick = { lectureId ->
                            viewModel.selectLecture(lectureId)
                            activeScreen = ScreenView.LECTURE_DETAIL
                        },
                        modifier = Modifier.padding(innerPadding)
                    )

                    AppNavDestination.TUITIONS -> TuitionsScreen(
                        tuitions = tuitionSummaries,
                        searchQuery = tuitionSearchQuery,
                        onSearchQueryChange = { tuitionSearchQuery = it },
                        showArchived = showArchivedTuitions,
                        onToggleArchived = { showArchivedTuitions = it },
                        onSelectTuition = { tuitionId ->
                            viewModel.selectTuition(tuitionId)
                            activeScreen = ScreenView.TUITION_DETAIL
                        },
                        onAddTuitionClick = {
                            tuitionToEdit = null
                            showAddTuitionDialog = true
                        }
                    )

                    AppNavDestination.LECTURES -> GlobalLecturesScreen(
                        lectures = allLectures,
                        tuitions = allTuitions,
                        onLectureClick = { lectureId ->
                            viewModel.selectLecture(lectureId)
                            activeScreen = ScreenView.LECTURE_DETAIL
                        },
                        onAddLectureClick = {
                            addLectureInitialTuitionId = allTuitions.firstOrNull()?.tuitionId
                            addLectureInitialDate = DateTimeUtils.getCurrentDate()
                            activeScreen = ScreenView.ADD_LECTURE
                        },
                        modifier = Modifier.padding(innerPadding)
                    )

                    AppNavDestination.SALARY -> SalaryScreen(
                        metrics = financialMetrics,
                        tuitionSummaries = tuitionSummaries,
                        tuitions = allTuitions,
                        onCycleClick = { cycleId ->
                            viewModel.selectCycle(cycleId)
                            activeScreen = ScreenView.CYCLE_DETAIL
                        },
                        onRecordPayment = { cycleId, tuitionId, amount, date, method, notes ->
                            viewModel.recordPayment(cycleId, tuitionId, amount, date, method, notes)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            ScreenView.TUITION_DETAIL -> TuitionDetailScreen(
                tuitionDetail = currentTuitionDetail,
                students = currentStudents,
                onBackClick = {
                    viewModel.selectTuition(null)
                    activeScreen = ScreenView.MAIN_TAB
                },
                onEditTuitionClick = {
                    tuitionToEdit = currentTuitionDetail?.tuition
                    showAddTuitionDialog = true
                },
                onDeleteTuitionClick = {
                    currentTuitionDetail?.tuition?.let { viewModel.deleteTuition(it.tuitionId) }
                    activeScreen = ScreenView.MAIN_TAB
                },
                onArchiveToggleClick = {
                    currentTuitionDetail?.tuition?.let {
                        if (it.status == "ARCHIVED") viewModel.unarchiveTuition(it.tuitionId)
                        else viewModel.archiveTuition(it.tuitionId)
                    }
                },
                onSaveStudent = { id, tId, name, nick, contact, fatherContact, motherContact, wa, tg, disc, isOnline, type, approach ->
                    viewModel.saveStudent(id, tId, name, nick, contact, fatherContact, motherContact, wa, tg, disc, isOnline, type, approach)
                },
                onDeleteStudent = { studentId ->
                    viewModel.deleteStudent(studentId)
                },
                onCreateNewCycleClick = { startDate ->
                    currentTuitionDetail?.tuition?.let {
                        viewModel.createNewCycle(it.tuitionId, startDate, forceClosePrevious = true)
                    }
                },
                onRecordNewLectureClick = {
                    currentTuitionDetail?.tuition?.let {
                        addLectureInitialTuitionId = it.tuitionId
                        addLectureInitialDate = DateTimeUtils.getCurrentDate()
                        activeScreen = ScreenView.ADD_LECTURE
                    }
                },
                onLectureClick = { lectureId ->
                    viewModel.selectLecture(lectureId)
                    activeScreen = ScreenView.LECTURE_DETAIL
                },
                onCycleClick = { cycleId ->
                    viewModel.selectCycle(cycleId)
                    activeScreen = ScreenView.CYCLE_DETAIL
                },
                onRecordPayment = { cycleId, tuitionId, amount, paymentDate, paymentMethod, notes ->
                    viewModel.recordPayment(cycleId, tuitionId, amount, paymentDate, paymentMethod, notes)
                },
                onUpdatePayment = { paymentId, amount, paymentDate, paymentMethod, notes ->
                    viewModel.updatePayment(paymentId, amount, paymentDate, paymentMethod, notes)
                },
                onDeletePayment = { paymentId ->
                    viewModel.deletePayment(paymentId)
                }
            )

            ScreenView.CYCLE_DETAIL -> CycleReportScreen(
                cycleSummary = selectedCycleSummary,
                tuition = allTuitions.find { it.tuitionId == selectedCycleSummary?.cycle?.tuitionId },
                allLecturesInCycle = allLectures.map { it.lecture }.filter { it.cycleId == selectedCycleSummary?.cycle?.cycleId },
                onLectureClick = { lectureId ->
                    viewModel.selectLecture(lectureId)
                    activeScreen = ScreenView.LECTURE_DETAIL
                },
                onAddLectureClick = { tuitionId ->
                    addLectureInitialTuitionId = tuitionId
                    addLectureInitialDate = DateTimeUtils.getCurrentDate()
                    activeScreen = ScreenView.ADD_LECTURE
                },
                onRecordPayment = { cycleId, tuitionId, amount, date, method, notes ->
                    viewModel.recordPayment(cycleId, tuitionId, amount, date, method, notes)
                },
                onUpdatePayment = { paymentId, amount, date, method, notes ->
                    viewModel.updatePayment(paymentId, amount, date, method, notes)
                },
                onDeletePayment = { paymentId ->
                    viewModel.deletePayment(paymentId)
                },
                onBackClick = {
                    activeScreen = if (currentTuitionDetail != null) ScreenView.TUITION_DETAIL else ScreenView.MAIN_TAB
                },
                modifier = Modifier.padding(innerPadding)
            )

            ScreenView.LECTURE_DETAIL -> LectureDetailScreen(
                lectureWithProgress = selectedLecture,
                cycleCompletedCount = selectedCycleSummary?.completedLectures ?: 1,
                cycleContractedCount = selectedCycleSummary?.cycle?.contractedLectures ?: 12,
                onUpdateLecture = { lectureId, date, startTime, endTime, duration, topic, hw, marks, note, leavingTime, returnTime ->
                    viewModel.updateLecture(lectureId, date, startTime, endTime, duration, topic, hw, marks, note, leavingTime, returnTime)
                },
                onDeleteLecture = { lectureId ->
                    viewModel.deleteLecture(lectureId)
                },
                onBackClick = {
                    activeScreen = ScreenView.MAIN_TAB
                },
                modifier = Modifier.padding(innerPadding)
            )

            ScreenView.ADD_LECTURE -> AddLectureScreen(
                tuitions = allTuitions,
                cycles = allCycles,
                initialTuitionId = addLectureInitialTuitionId,
                initialDate = addLectureInitialDate,
                onSaveLecture = { tuitionId, cycleId, date, startTime, endTime, duration, topic, hw, marks, note, leavingTime, returnTime ->
                    viewModel.recordLecture(
                        tuitionId = tuitionId,
                        cycleId = cycleId,
                        lectureDate = date,
                        startTime = startTime,
                        endTime = endTime,
                        durationMinutes = duration,
                        topic = topic,
                        homework = hw,
                        examMarks = marks,
                        shortNote = note,
                        leavingTime = leavingTime,
                        returnTime = returnTime,
                        onComplete = { _ ->
                            activeScreen = ScreenView.MAIN_TAB
                        }
                    )
                },
                onBackClick = {
                    activeScreen = ScreenView.MAIN_TAB
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showAddTuitionDialog) {
        AddEditTuitionDialog(
            initialTuition = tuitionToEdit,
            onDismiss = {
                showAddTuitionDialog = false
                tuitionToEdit = null
            },
            onSave = { loc, grade, tType, days, sDate, salary, map, sName, sContact ->
                viewModel.saveTuition(
                    id = tuitionToEdit?.tuitionId,
                    location = loc,
                    classGrade = grade,
                    tuitionType = tType,
                    daysPerWeek = days,
                    startingDate = sDate,
                    totalSalary = salary,
                    mapLink = map,
                    studentName = sName,
                    studentContact = sContact
                )
                showAddTuitionDialog = false
                tuitionToEdit = null
            }
        )
    }
}
