package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.SalaryCycleEntity
import com.example.data.local.entity.TuitionEntity
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLectureScreen(
    tuitions: List<TuitionEntity>,
    cycles: List<SalaryCycleEntity> = emptyList(),
    initialTuitionId: String? = null,
    initialCycleId: String? = null,
    initialDate: String = DateTimeUtils.getCurrentDate(),
    onSaveLecture: (
        tuitionId: String,
        cycleId: String?,
        date: String,
        startTime: String,
        endTime: String,
        durationMinutes: Int,
        topic: String,
        homework: String,
        examMarks: String,
        shortNote: String,
        leavingTime: String,
        returnTime: String
    ) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedTuitionId by remember {
        mutableStateOf(initialTuitionId ?: tuitions.firstOrNull()?.tuitionId ?: "")
    }

    // Filter available cycles for selected tuition
    val tuitionCycles = remember(cycles, selectedTuitionId) {
        cycles.filter { it.tuitionId == selectedTuitionId }
    }

    val eligibleOpenCycle = remember(tuitionCycles) {
        tuitionCycles.firstOrNull { it.status == "OPEN" } ?: tuitionCycles.firstOrNull()
    }

    var selectedCycleId by remember {
        mutableStateOf(initialCycleId ?: eligibleOpenCycle?.cycleId)
    }

    var selectedDate by remember { mutableStateOf(initialDate) }
    var startTime by remember { mutableStateOf("04:00 PM") }
    var endTime by remember { mutableStateOf("05:45 PM") }
    var durationMinutes by remember { mutableIntStateOf(DateTimeUtils.calculateMinutesBetween("04:00 PM", "05:45 PM")) }

    var topic by remember { mutableStateOf("") }
    var shortNote by remember { mutableStateOf("") }
    var homework by remember { mutableStateOf("") }
    var examMarks by remember { mutableStateOf("") }

    // Optional travel times
    var leavingTime by remember { mutableStateOf("") }
    var returnTime by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val selectedTuition = tuitions.find { it.tuitionId == selectedTuitionId }
    val currentCycleObj = tuitionCycles.find { it.cycleId == selectedCycleId } ?: eligibleOpenCycle

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record New Lecture", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("add_lecture_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cycle Selector
            var cycleMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = cycleMenuExpanded,
                onExpandedChange = { cycleMenuExpanded = !cycleMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = currentCycleObj?.let { "Cycle #${it.cycleNumber} (${if (it.status == "OPEN") "Eligible · OPEN" else "CLOSED"})" }
                        ?: "No cycle selected",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cycle") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cycleMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("cycle_selector_field"),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = cycleMenuExpanded,
                    onDismissRequest = { cycleMenuExpanded = false }
                ) {
                    tuitionCycles.forEach { cycle ->
                        DropdownMenuItem(
                            text = {
                                Text("Cycle #${cycle.cycleNumber} — ${if (cycle.status == "OPEN") "Eligible (OPEN)" else "Closed"}")
                            },
                            onClick = {
                                selectedCycleId = cycle.cycleId
                                cycleMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Date picker (native)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        DateTimeUtils.showNativeDatePicker(context, selectedDate) { formatted, _ ->
                            selectedDate = formatted
                        }
                    }
            ) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Date") },
                    trailingIcon = {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_lecture_date_field"),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Start Time & End Time (native time pickers)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            DateTimeUtils.showNativeTimePicker(context, startTime) { formatted ->
                                startTime = formatted
                                durationMinutes = DateTimeUtils.calculateMinutesBetween(startTime, endTime)
                            }
                        }
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Start Time") },
                        trailingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_lecture_start_time_field"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            DateTimeUtils.showNativeTimePicker(context, endTime) { formatted ->
                                endTime = formatted
                                durationMinutes = DateTimeUtils.calculateMinutesBetween(startTime, endTime)
                            }
                        }
                ) {
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("End Time") },
                        trailingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_lecture_end_time_field"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Text(
                text = "Class Duration: ${DateTimeUtils.formatMinutesToHoursAndMinutes(durationMinutes)} ($durationMinutes min)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Topic field
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic") },
                placeholder = { Text("Enter topic") },
                modifier = Modifier.fillMaxWidth().testTag("input_topic"),
                shape = RoundedCornerShape(10.dp)
            )

            // Short Notes field
            OutlinedTextField(
                value = shortNote,
                onValueChange = { shortNote = it },
                label = { Text("Short Notes") },
                placeholder = { Text("Enter notes") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth().testTag("input_short_notes"),
                shape = RoundedCornerShape(10.dp)
            )

            // Optional Travel Times expandable/direct fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            DateTimeUtils.showNativeTimePicker(context, if (leavingTime.isNotBlank()) leavingTime else "03:30 PM") { formatted ->
                                leavingTime = formatted
                            }
                        }
                ) {
                    OutlinedTextField(
                        value = leavingTime,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Left Home (Opt)") },
                        trailingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            DateTimeUtils.showNativeTimePicker(context, if (returnTime.isNotBlank()) returnTime else "06:15 PM") { formatted ->
                                returnTime = formatted
                            }
                        }
                ) {
                    OutlinedTextField(
                        value = returnTime,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Returned (Opt)") },
                        trailingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons: [ Cancel ]        [ Save Lecture ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (isSubmitting) return@Button
                        if (selectedTuitionId.isBlank()) {
                            Toast.makeText(context, "Tuition must be selected", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (durationMinutes <= 0) {
                            Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        onSaveLecture(
                            selectedTuitionId,
                            selectedCycleId,
                            selectedDate,
                            startTime,
                            endTime,
                            durationMinutes,
                            topic.trim(),
                            homework.trim(),
                            examMarks.trim(),
                            shortNote.trim(),
                            leavingTime.trim(),
                            returnTime.trim()
                        )
                    },
                    modifier = Modifier.weight(1f).testTag("save_lecture_submit_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Lecture")
                }
            }
        }
    }
}
