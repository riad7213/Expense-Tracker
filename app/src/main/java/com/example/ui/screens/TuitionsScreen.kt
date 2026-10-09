package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.sp
import com.example.data.model.TuitionSummary
import com.example.ui.components.TuitionListCard

enum class TuitionFilterOption {
    ACTIVE,
    ARCHIVED,
    ALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuitionsScreen(
    tuitions: List<TuitionSummary>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showArchived: Boolean,
    onToggleArchived: (Boolean) -> Unit,
    onSelectTuition: (String) -> Unit,
    onAddTuitionClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(if (showArchived) TuitionFilterOption.ARCHIVED else TuitionFilterOption.ACTIVE) }

    // Filter tuitions by selected filter and search query
    val filteredTuitions = remember(tuitions, selectedFilter, searchQuery) {
        tuitions.filter { summary ->
            val status = summary.tuition.status.uppercase()
            val matchesFilter = when (selectedFilter) {
                TuitionFilterOption.ACTIVE -> status != "ARCHIVED" && status != "DELETED"
                TuitionFilterOption.ARCHIVED -> status == "ARCHIVED"
                TuitionFilterOption.ALL -> status != "DELETED"
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                summary.tuition.location.lowercase().contains(q) ||
                    summary.tuition.classGrade.lowercase().contains(q)
            }
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tuitions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = onAddTuitionClick,
                        modifier = Modifier.testTag("add_tuition_top_action")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Tuition")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Compact Filter Row: [Active] [Archived] [All]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == TuitionFilterOption.ACTIVE,
                    onClick = {
                        selectedFilter = TuitionFilterOption.ACTIVE
                        onToggleArchived(false)
                    },
                    label = { Text("Active", fontSize = 13.sp) },
                    modifier = Modifier.testTag("filter_active_chip")
                )

                FilterChip(
                    selected = selectedFilter == TuitionFilterOption.ARCHIVED,
                    onClick = {
                        selectedFilter = TuitionFilterOption.ARCHIVED
                        onToggleArchived(true)
                    },
                    label = { Text("Archived", fontSize = 13.sp) },
                    modifier = Modifier.testTag("filter_archived_chip")
                )

                FilterChip(
                    selected = selectedFilter == TuitionFilterOption.ALL,
                    onClick = {
                        selectedFilter = TuitionFilterOption.ALL
                        onToggleArchived(false)
                    },
                    label = { Text("All", fontSize = 13.sp) },
                    modifier = Modifier.testTag("filter_all_chip")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Search Bar: [ Search tuitions... ]
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search tuitions...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tuition_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tuitions List
            if (filteredTuitions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No tuitions match '$searchQuery'"
                            else when (selectedFilter) {
                                TuitionFilterOption.ARCHIVED -> "No archived tuitions"
                                TuitionFilterOption.ALL -> "No tuitions recorded"
                                TuitionFilterOption.ACTIVE -> "No active tuitions"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedFilter == TuitionFilterOption.ARCHIVED) "Archived tuitions will be displayed here."
                            else "Tap + in the top bar to create your first tuition.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (selectedFilter != TuitionFilterOption.ARCHIVED && searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onAddTuitionClick,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("empty_add_tuition_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Tuition")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTuitions, key = { it.tuition.tuitionId }) { summary ->
                        TuitionListCard(
                            summary = summary,
                            onClick = { onSelectTuition(summary.tuition.tuitionId) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
