package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.LectureEntity
import com.example.data.local.entity.SalaryCycleEntity
import com.example.data.local.entity.SalaryPaymentEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TuitionEntity
import com.example.data.model.CycleFinancialMetrics
import com.example.data.model.CycleSummary
import com.example.data.model.GlobalFinancialMetrics
import com.example.data.model.LectureWithProgress
import com.example.data.model.PaymentStatus
import com.example.data.model.SalaryCalculator
import com.example.data.model.TuitionDetailData
import com.example.data.model.TuitionSummary
import com.example.data.repository.TuitionRepository
import com.example.util.DateTimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavDestination {
    HOME,
    TUITIONS,
    LECTURES,
    SALARY
}

class TuitionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = TuitionRepository(database)

    init {
        viewModelScope.launch {
            repository.cleanupAllDuplicateOpenCycles()
        }
    }

    // Current navigation tab
    private val _currentDestination = MutableStateFlow(AppNavDestination.HOME)
    val currentDestination: StateFlow<AppNavDestination> = _currentDestination.asStateFlow()

    fun navigateTo(dest: AppNavDestination) {
        _currentDestination.value = dest
    }

    // Selected tuition ID for detail screen
    private val _selectedTuitionId = MutableStateFlow<String?>(null)
    val selectedTuitionId: StateFlow<String?> = _selectedTuitionId.asStateFlow()

    // Selected cycle ID for cycle report screen
    private val _selectedCycleId = MutableStateFlow<String?>(null)
    val selectedCycleId: StateFlow<String?> = _selectedCycleId.asStateFlow()

    // Selected lecture ID for lecture detail / edit screen
    private val _selectedLectureId = MutableStateFlow<String?>(null)
    val selectedLectureId: StateFlow<String?> = _selectedLectureId.asStateFlow()

    // Selected date for Home screen calendar / daily view (formatted "dd-MMM-yy")
    private val _selectedDate = MutableStateFlow(DateTimeUtils.getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Tuitions list
    val allTuitions: StateFlow<List<TuitionEntity>> = repository.getAllTuitions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tuitionSummaries: StateFlow<List<TuitionSummary>> = repository.getTuitionSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentTuitionDetail: StateFlow<TuitionDetailData?> = _selectedTuitionId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getTuitionDetail(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Global lectures list
    val allLecturesWithDetails: StateFlow<List<LectureWithProgress>> = repository.getAllLectures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All cycles
    val allCycles: StateFlow<List<SalaryCycleEntity>> = repository.getAllCycles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All payments
    val allPayments: StateFlow<List<SalaryPaymentEntity>> = repository.getAllPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Global Financial Metrics
    val globalFinancialMetrics: StateFlow<GlobalFinancialMetrics> = combine(
        allTuitions,
        allCycles,
        allLecturesWithDetails,
        allPayments
    ) { tuitions, cycles, lecturesWithProg, payments ->
        val lectures = lecturesWithProg.map { it.lecture }
        SalaryCalculator.computeGlobalMetrics(tuitions, cycles, lectures, payments)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        GlobalFinancialMetrics(0.0, 0.0, 0.0, 0.0, 0, 0, 0)
    )

    // Selected Cycle Report Data
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCycleSummary: StateFlow<CycleSummary?> = _selectedCycleId
        .flatMapLatest { cycleId ->
            if (cycleId == null) flowOf(null)
            else repository.getCycleSummary(cycleId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Selected Lecture Data
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedLecture: StateFlow<LectureWithProgress?> = _selectedLectureId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getLectureWithProgressById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Students for current selected tuition
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentStudents: StateFlow<List<StudentEntity>> = _selectedTuitionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getStudents(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTuition(tuitionId: String?) {
        _selectedTuitionId.value = tuitionId
        if (tuitionId != null) {
            viewModelScope.launch {
                repository.cleanupDuplicateOpenCycles(tuitionId)
            }
        }
    }

    fun selectCycle(cycleId: String?) {
        _selectedCycleId.value = cycleId
    }

    fun selectLecture(lectureId: String?) {
        _selectedLectureId.value = lectureId
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun saveTuition(
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
        viewModelScope.launch {
            repository.saveTuition(
                id = id,
                studentName = studentName,
                className = className,
                institution = institution,
                subjects = subjects,
                contractedLectures = contractedLectures,
                totalSalary = totalSalary,
                location = location,
                colorHex = colorHex,
                paymentType = paymentType,
                notes = notes
            )
        }
    }

    fun saveTuition(
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
        viewModelScope.launch {
            repository.saveTuition(
                id = id,
                location = location,
                classGrade = classGrade,
                tuitionType = tuitionType,
                daysPerWeek = daysPerWeek,
                startingDate = startingDate,
                totalSalary = totalSalary,
                mapLink = mapLink,
                studentName = studentName,
                studentContact = studentContact
            )
        }
    }

    fun deleteTuition(tuitionId: String) {
        viewModelScope.launch {
            repository.deleteTuition(tuitionId)
            if (_selectedTuitionId.value == tuitionId) {
                _selectedTuitionId.value = null
            }
        }
    }

    fun archiveTuition(tuitionId: String) {
        viewModelScope.launch {
            repository.archiveTuition(tuitionId)
        }
    }

    fun unarchiveTuition(tuitionId: String) {
        viewModelScope.launch {
            repository.unarchiveTuition(tuitionId)
        }
    }

    fun createNewCycle(
        tuitionId: String,
        startDate: String,
        forceClosePrevious: Boolean = true,
        onComplete: (Result<SalaryCycleEntity>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createNewCycle(tuitionId, startDate, forceClosePrevious)
            onComplete(result)
        }
    }

    fun recordLecture(
        tuitionId: String,
        cycleId: String? = null,
        lectureDate: String,
        startTime: String,
        endTime: String,
        durationMinutes: Int,
        topic: String = "",
        homework: String = "",
        examMarks: String = "",
        shortNote: String = "",
        leavingTime: String = "",
        returnTime: String = "",
        onComplete: (Result<LectureEntity>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val day = DateTimeUtils.deriveDayFromDate(lectureDate)
            val result = repository.recordLecture(
                tuitionId = tuitionId,
                cycleId = cycleId,
                date = lectureDate,
                day = day,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                topic = topic,
                homework = homework,
                examMarks = examMarks,
                shortNote = shortNote,
                leavingTime = leavingTime,
                returnTime = returnTime
            )
            onComplete(result)
        }
    }

    fun updateLecture(
        lectureId: String,
        date: String,
        startTime: String,
        endTime: String,
        durationMinutes: Int,
        topic: String,
        homework: String,
        examMarks: String,
        shortNote: String,
        leavingTime: String = "",
        returnTime: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val day = DateTimeUtils.deriveDayFromDate(date)
            repository.updateLecture(
                lectureId = lectureId,
                date = date,
                day = day,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                topic = topic,
                homework = homework,
                examMarks = examMarks,
                shortNote = shortNote,
                leavingTime = leavingTime,
                returnTime = returnTime
            )
            onComplete()
        }
    }

    fun deleteLecture(lectureId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteLecture(lectureId)
            if (_selectedLectureId.value == lectureId) {
                _selectedLectureId.value = null
            }
            onComplete()
        }
    }

    fun recordPayment(
        cycleId: String,
        tuitionId: String,
        amount: Double,
        paymentDate: String,
        paymentMethod: String,
        notes: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.recordPayment(
                cycleId = cycleId,
                tuitionId = tuitionId,
                amount = amount,
                paymentDate = paymentDate,
                paymentMethod = paymentMethod,
                notes = notes
            )
            onComplete()
        }
    }

    fun updatePayment(
        paymentId: String,
        amount: Double,
        paymentDate: String,
        paymentMethod: String,
        notes: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.updatePayment(
                paymentId = paymentId,
                amount = amount,
                paymentDate = paymentDate,
                paymentMethod = paymentMethod,
                notes = notes
            )
            onComplete()
        }
    }

    fun deletePayment(paymentId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePayment(paymentId)
            onComplete()
        }
    }

    fun saveStudent(
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
        viewModelScope.launch {
            repository.saveStudent(
                id = id,
                tuitionId = tuitionId,
                name = name,
                nickname = nickname,
                contact = contact,
                fatherContact = fatherContact,
                motherContact = motherContact,
                whatsApp = whatsApp,
                telegram = telegram,
                discord = discord,
                isOnline = isOnline,
                type = type,
                approach = approach
            )
        }
    }

    fun saveStudent(
        id: String?,
        tuitionId: String,
        name: String,
        grade: String,
        school: String,
        parentContact: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.saveStudent(id, tuitionId, name, grade, school, parentContact, notes)
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
        }
    }
}
