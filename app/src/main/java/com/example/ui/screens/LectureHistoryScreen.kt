package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LectureWithProgress
import com.example.ui.components.LectureItemCard
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureHistoryScreen(
    tuitionLocation: String,
    lectures: List<LectureWithProgress>,
    selectedYear: Int,
    selectedMonth: Int,
    onBack: () -> Unit,
    onAddLectureClick: () -> Unit,
    onDeleteLecture: (lectureId: String) -> Unit
) {
    var lectureToDelete by remember { mutableStateOf<LectureWithProgress?>(null) }
    var filterByCurrentMonth by remember { mutableStateOf(false) }

    val monthName = DateTimeUtils.formatYearMonth(selectedYear, selectedMonth)
    val filteredLectures = remember(lectures, filterByCurrentMonth, selectedYear, selectedMonth) {
        if (filterByCurrentMonth) {
            lectures.filter { DateTimeUtils.isDateInYearMonth(it.lecture.date, selectedYear, selectedMonth) }
        } else {
            lectures
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Lecture History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$tuitionLocation • ${filteredLectures.size} lectures",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddLectureClick,
                modifier = Modifier.testTag("add_lecture_from_history_fab"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Lecture")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chip row: All Lectures vs Current Month
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !filterByCurrentMonth,
                    onClick = { filterByCurrentMonth = false },
                    label = { Text("All Lectures (${lectures.size})") },
                    shape = RoundedCornerShape(8.dp)
                )
                FilterChip(
                    selected = filterByCurrentMonth,
                    onClick = { filterByCurrentMonth = true },
                    label = { Text("$monthName only") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            if (filteredLectures.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (filterByCurrentMonth) "No lectures in $monthName" else "No lectures recorded yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (filterByCurrentMonth) "Switch to 'All Lectures' or tap + to record a lecture in $monthName." else "Tap + to record your first lecture time and progress.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredLectures, key = { it.lecture.lectureId }) { item ->
                        LectureItemCard(
                            item = item,
                            onDeleteClick = { lectureToDelete = item }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (lectureToDelete != null) {
        AlertDialog(
            onDismissRequest = { lectureToDelete = null },
            title = { Text("Delete Lecture?") },
            text = { Text("Are you sure you want to delete Lecture #${lectureToDelete?.lecture?.lectureNumber}? This will automatically recalculate salary and lecture counts.") },
            confirmButton = {
                Button(
                    onClick = {
                        lectureToDelete?.let { onDeleteLecture(it.lecture.lectureId) }
                        lectureToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { lectureToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
