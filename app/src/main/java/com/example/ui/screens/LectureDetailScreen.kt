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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LectureWithProgress
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureDetailScreen(
    lectureWithProgress: LectureWithProgress?,
    cycleCompletedCount: Int = 1,
    cycleContractedCount: Int = 12,
    onUpdateLecture: (
        lectureId: String,
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
    onDeleteLecture: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (lectureWithProgress == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Lecture Details") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Lecture not found")
            }
        }
        return
    }

    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val lecture = lectureWithProgress.lecture

    // Form states
    var selectedDate by remember { mutableStateOf(lecture.date) }
    var startTime by remember { mutableStateOf(lecture.startingTime.ifBlank { "04:00 PM" }) }
    var endTime by remember { mutableStateOf(lecture.endingTime.ifBlank { "05:45 PM" }) }
    var durationMinutes by remember {
        mutableIntStateOf(DateTimeUtils.calculateMinutesBetween(startTime, endTime))
    }

    var leavingTime by remember { mutableStateOf(lecture.leavingTime) }
    var returnTime by remember { mutableStateOf(lecture.returnTime) }

    var topic by remember { mutableStateOf(lecture.topic) }
    var shortNote by remember { mutableStateOf(lecture.shortNote) }
    var homework by remember { mutableStateOf(lecture.homework) }
    var examMarks by remember { mutableStateOf(lecture.examMarks) }

    LaunchedEffect(lectureWithProgress) {
        selectedDate = lecture.date
        startTime = lecture.startingTime.ifBlank { "04:00 PM" }
        endTime = lecture.endingTime.ifBlank { "05:45 PM" }
        durationMinutes = DateTimeUtils.calculateMinutesBetween(startTime, endTime)
        leavingTime = lecture.leavingTime
        returnTime = lecture.returnTime
        topic = lecture.topic
        shortNote = lecture.shortNote
        homework = lecture.homework
        examMarks = lecture.examMarks
    }

    val displayCompletedCount = cycleCompletedCount.coerceAtLeast(lecture.lectureNumber)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lecture ${lecture.lectureNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("lecture_detail_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Lecture") },
                            onClick = {
                                menuExpanded = false
                                isEditing = true
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete Lecture", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                showDeleteConfirm = true
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
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
            // Lecture title badge: Lecture 2/9 (where 9 is completed lectures in cycle)
            Text(
                text = "Lecture ${lecture.lectureNumber}/$displayCompletedCount",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Section 1: Overview
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column {
                        Text("Tuition:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${lectureWithProgress.tuitionLocation} (${lectureWithProgress.tuitionClass})",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column {
                        Text("Cycle:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "#${lectureWithProgress.cycleNumber}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column {
                        Text("Lecture Number:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${lecture.lectureNumber}/$displayCompletedCount",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Section 2: Time and Schedule
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Time and Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (isEditing) {
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
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                                    trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
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
                                    trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
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

                        // Travel times editing
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
                                    label = { Text("Left Home") },
                                    trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
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
                                    label = { Text("Returned Home") },
                                    trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
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
                    } else {
                        Column {
                            Text("Date:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(lecture.date, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }

                        Column {
                            Text("Time:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${lecture.startingTime} – ${lecture.endingTime}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        val exactClassMins = DateTimeUtils.calculateMinutesBetween(lecture.startingTime, lecture.endingTime)
                        Column {
                            Text("Class Duration:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${DateTimeUtils.formatMinutesToHoursAndMinutes(exactClassMins)} ($exactClassMins min)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Display travel time separately if source data exists
                        if (lecture.leavingTime.isNotBlank() && lecture.returnTime.isNotBlank()) {
                            val totalMins = DateTimeUtils.calculateMinutesBetween(lecture.leavingTime, lecture.returnTime)
                            val travelMins = (totalMins - exactClassMins).coerceAtLeast(0)
                            Column {
                                Text("Travel Time (Separate):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${DateTimeUtils.formatMinutesToHoursAndMinutes(travelMins)} ($travelMins min) [Left: ${lecture.leavingTime}, Returned: ${lecture.returnTime}]",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Academic Details
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Academic Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (isEditing) {
                        OutlinedTextField(
                            value = topic,
                            onValueChange = { topic = it },
                            label = { Text("Topic") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_lecture_topic"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = shortNote,
                            onValueChange = { shortNote = it },
                            label = { Text("Short Notes") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_lecture_notes"),
                            minLines = 2,
                            shape = RoundedCornerShape(10.dp)
                        )
                    } else {
                        Column {
                            Text("Topic:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                topic.ifBlank { "None recorded" },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column {
                            Text("Short Notes:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                shortNote.ifBlank { "None" },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Edit Action buttons
            if (isEditing) {
                Button(
                    onClick = {
                        onUpdateLecture(
                            lecture.lectureId,
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
                        isEditing = false
                        Toast.makeText(context, "Lecture updated", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_lecture_edits_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Changes")
                }

                OutlinedButton(
                    onClick = {
                        selectedDate = lecture.date
                        startTime = lecture.startingTime.ifBlank { "04:00 PM" }
                        endTime = lecture.endingTime.ifBlank { "05:45 PM" }
                        durationMinutes = DateTimeUtils.calculateMinutesBetween(startTime, endTime)
                        leavingTime = lecture.leavingTime
                        returnTime = lecture.returnTime
                        topic = lecture.topic
                        shortNote = lecture.shortNote
                        isEditing = false
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Lecture") },
            text = { Text("Are you sure you want to delete Lecture #${lecture.lectureNumber}? Cycle progress will update accordingly.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteLecture(lecture.lectureId)
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
