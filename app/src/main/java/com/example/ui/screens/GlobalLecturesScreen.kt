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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.TuitionEntity
import com.example.data.model.LectureWithProgress
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalLecturesScreen(
    lectures: List<LectureWithProgress>,
    tuitions: List<TuitionEntity>,
    onLectureClick: (String) -> Unit,
    onAddLectureClick: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Filter states
    var selectedTuitionId by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedMonthPair by remember { mutableStateOf<Pair<Int, Int>?>(null) } // Pair(year, month1Based)
    var selectedCycleNumber by remember { mutableStateOf<Int?>(null) }

    // Distinct cycles present in current lectures
    val availableCycles = remember(lectures, selectedTuitionId) {
        lectures
            .filter { selectedTuitionId == null || it.lecture.tuitionId == selectedTuitionId }
            .map { it.cycleNumber }
            .distinct()
            .sorted()
    }

    // Filtered lectures
    val filteredLectures = remember(
        lectures,
        selectedTuitionId,
        selectedDate,
        selectedMonthPair,
        selectedCycleNumber
    ) {
        lectures.filter { item ->
            val matchTuition = selectedTuitionId == null || item.lecture.tuitionId == selectedTuitionId
            val matchDate = selectedDate == null || item.lecture.date.equals(selectedDate, ignoreCase = true)
            val matchMonth = selectedMonthPair == null || DateTimeUtils.isDateInYearMonth(
                item.lecture.date,
                selectedMonthPair!!.first,
                selectedMonthPair!!.second
            )
            val matchCycle = selectedCycleNumber == null || item.cycleNumber == selectedCycleNumber
            matchTuition && matchDate && matchMonth && matchCycle
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Global Lecture History", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${filteredLectures.size} of ${lectures.size} lectures",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddLectureClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_lecture")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Record Lecture")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filters section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Filters",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val hasActiveFilter = selectedTuitionId != null || selectedDate != null ||
                                selectedMonthPair != null || selectedCycleNumber != null
                        if (hasActiveFilter) {
                            TextButton(
                                onClick = {
                                    selectedTuitionId = null
                                    selectedDate = null
                                    selectedMonthPair = null
                                    selectedCycleNumber = null
                                }
                            ) {
                                Text("Clear All", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    // Tuition filter dropdown
                    var tuitionMenuExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = tuitionMenuExpanded,
                        onExpandedChange = { tuitionMenuExpanded = !tuitionMenuExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val sel = tuitions.find { it.tuitionId == selectedTuitionId }
                        OutlinedTextField(
                            value = sel?.let { "${it.classGrade} (${it.location})" } ?: "All Tuitions",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tuition") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tuitionMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = tuitionMenuExpanded,
                            onDismissRequest = { tuitionMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Tuitions") },
                                onClick = {
                                    selectedTuitionId = null
                                    tuitionMenuExpanded = false
                                }
                            )
                            tuitions.forEach { tuition ->
                                DropdownMenuItem(
                                    text = { Text("${tuition.classGrade} — ${tuition.location}") },
                                    onClick = {
                                        selectedTuitionId = tuition.tuitionId
                                        tuitionMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Date & Month & Cycle filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Date filter chip
                        item {
                            FilterChip(
                                selected = selectedDate != null,
                                onClick = {
                                    DateTimeUtils.showNativeDatePicker(context, selectedDate ?: DateTimeUtils.getCurrentDate()) { formatted, _ ->
                                        selectedDate = formatted
                                    }
                                },
                                label = {
                                    Text(selectedDate ?: "Select Date")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                trailingIcon = if (selectedDate != null) {
                                    {
                                        IconButton(
                                            onClick = { selectedDate = null },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear date", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                } else null
                            )
                        }

                        // Month filter chip
                        item {
                            val (currYear, currMonth) = DateTimeUtils.getCurrentYearMonth()
                            val isCurrentMonthSelected = selectedMonthPair?.first == currYear && selectedMonthPair?.second == currMonth
                            FilterChip(
                                selected = isCurrentMonthSelected,
                                onClick = {
                                    selectedMonthPair = if (isCurrentMonthSelected) null else Pair(currYear, currMonth)
                                },
                                label = { Text("This Month") }
                            )
                        }

                        // Cycle filter chips
                        items(availableCycles) { cycleNum ->
                            FilterChip(
                                selected = selectedCycleNumber == cycleNum,
                                onClick = {
                                    selectedCycleNumber = if (selectedCycleNumber == cycleNum) null else cycleNum
                                },
                                label = { Text("Cycle #$cycleNum") }
                            )
                        }
                    }
                }
            }

            // Lecture List
            if (filteredLectures.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "No lectures found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Try clearing active filters or record a new lecture.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 80.dp
                    )
                ) {
                    items(filteredLectures, key = { it.lecture.lectureId }) { item ->
                        GlobalLectureCard(
                            item = item,
                            onClick = { onLectureClick(item.lecture.lectureId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlobalLectureCard(
    item: LectureWithProgress,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("lecture_card_${item.lecture.lectureId}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#${item.lecture.lectureNumber}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.tuitionClass} (${item.tuitionLocation})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Cycle #${item.cycleNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.lecture.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${item.lecture.startingTime} - ${item.lecture.endingTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.lecture.topic.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Topic: ${item.lecture.topic}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (item.lecture.homework.isNotBlank()) {
                    Text(
                        text = "HW: ${item.lecture.homework}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
