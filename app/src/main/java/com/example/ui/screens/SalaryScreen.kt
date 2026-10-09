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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.TuitionEntity
import com.example.data.model.GlobalFinancialMetrics
import com.example.data.model.PaymentStatus
import com.example.data.model.TuitionSummary
import com.example.ui.components.PaymentStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryScreen(
    metrics: GlobalFinancialMetrics,
    tuitionSummaries: List<TuitionSummary>,
    tuitions: List<TuitionEntity>,
    onCycleClick: (cycleId: String) -> Unit,
    onRecordPayment: (cycleId: String, tuitionId: String, amount: Double, date: String, method: String, notes: String) -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Cycle filters
    var selectedTuitionId by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<PaymentStatus?>(null) }
    var selectedCycleStateFilter by remember { mutableStateOf<String?>("ALL") }

    val allCycleItems = remember(tuitionSummaries) {
        tuitionSummaries.flatMap { summary ->
            summary.cycleHistory.map { cycleSummary ->
                CycleListItem(
                    tuition = summary.tuition,
                    cycleSummary = cycleSummary
                )
            }
        }
    }

    val filteredCycles = remember(allCycleItems, selectedTuitionId, selectedStatusFilter, selectedCycleStateFilter) {
        allCycleItems.filter { item ->
            val matchTuition = selectedTuitionId == null || item.tuition.tuitionId == selectedTuitionId
            val matchStatus = selectedStatusFilter == null || item.cycleSummary.paymentStatus == selectedStatusFilter
            val matchCycleState = when (selectedCycleStateFilter) {
                "ACTIVE" -> !item.cycleSummary.isClosed
                "CLOSED" -> item.cycleSummary.isClosed
                else -> true
            }
            matchTuition && matchStatus && matchCycleState
        }.sortedWith(
            compareBy<CycleListItem> { it.cycleSummary.isClosed }
                .thenByDescending { it.cycleSummary.cycle.cycleNumber }
        )
    }

    var recordPaymentTarget by remember { mutableStateOf<CycleListItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial & Salary") },
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
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Financial Overview Cards
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Financial Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Total Earned (Completed Lectures)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                "৳ ${metrics.totalEarned.toInt()}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Total Contract Value: ৳ ${metrics.totalTargetSalary.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Total Paid",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "৳ ${metrics.totalPaid.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${metrics.paidCyclesCount} cycles paid",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Total Due",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "৳ ${metrics.totalDue.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (metrics.totalDue > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${metrics.unpaidCyclesCount} cycles due",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Cycle Filters Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    "Cycle Filters",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (selectedTuitionId != null || selectedStatusFilter != null || selectedCycleStateFilter != "ALL") {
                                TextButton(
                                    onClick = {
                                        selectedTuitionId = null
                                        selectedStatusFilter = null
                                        selectedCycleStateFilter = "ALL"
                                    }
                                ) {
                                    Text("Reset Filters", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        // Tuition Dropdown
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

                        // State chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedCycleStateFilter == "ALL",
                                onClick = { selectedCycleStateFilter = "ALL" },
                                label = { Text("All Cycles") }
                            )
                            FilterChip(
                                selected = selectedCycleStateFilter == "ACTIVE",
                                onClick = { selectedCycleStateFilter = "ACTIVE" },
                                label = { Text("Active") }
                            )
                            FilterChip(
                                selected = selectedCycleStateFilter == "CLOSED",
                                onClick = { selectedCycleStateFilter = "CLOSED" },
                                label = { Text("Closed") }
                            )
                        }

                        // Status chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedStatusFilter == PaymentStatus.UNPAID,
                                onClick = {
                                    selectedStatusFilter = if (selectedStatusFilter == PaymentStatus.UNPAID) null else PaymentStatus.UNPAID
                                },
                                label = { Text("Unpaid") }
                            )
                            FilterChip(
                                selected = selectedStatusFilter == PaymentStatus.PARTIAL,
                                onClick = {
                                    selectedStatusFilter = if (selectedStatusFilter == PaymentStatus.PARTIAL) null else PaymentStatus.PARTIAL
                                },
                                label = { Text("Partial") }
                            )
                            FilterChip(
                                selected = selectedStatusFilter == PaymentStatus.PAID,
                                onClick = {
                                    selectedStatusFilter = if (selectedStatusFilter == PaymentStatus.PAID) null else PaymentStatus.PAID
                                },
                                label = { Text("Paid") }
                            )
                        }
                    }
                }
            }

            // Cycles Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Salary Cycles (${filteredCycles.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Cycle items
            if (filteredCycles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No salary cycles match the selected filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredCycles, key = { it.cycleSummary.cycle.cycleId }) { item ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SalaryCycleRowCard(
                            item = item,
                            onClick = { onCycleClick(item.cycleSummary.cycle.cycleId) },
                            onRecordPayment = { recordPaymentTarget = item }
                        )
                    }
                }
            }
        }
    }

    if (recordPaymentTarget != null) {
        val target = recordPaymentTarget!!
        RecordPaymentDialog(
            cycleId = target.cycleSummary.cycle.cycleId,
            tuitionId = target.tuition.tuitionId,
            suggestedAmount = target.cycleSummary.outstandingAmount,
            cycleTargetSalary = target.cycleSummary.cycle.totalSalary,
            cycleTotalPaid = target.cycleSummary.paidAmount,
            onDismiss = { recordPaymentTarget = null },
            onSavePayment = { cId, tId, amt, dt, method, nts ->
                onRecordPayment(cId, tId, amt, dt, method, nts)
                recordPaymentTarget = null
            }
        )
    }
}

private data class CycleListItem(
    val tuition: TuitionEntity,
    val cycleSummary: com.example.data.model.CycleSummary
)

@Composable
private fun SalaryCycleRowCard(
    item: CycleListItem,
    onClick: () -> Unit,
    onRecordPayment: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("salary_cycle_card_${item.cycleSummary.cycle.cycleId}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
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
                Column {
                    Text(
                        text = "${item.tuition.classGrade} (${item.tuition.location})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cycle #${item.cycleSummary.cycle.cycleNumber} • ${if (item.cycleSummary.isClosed) "Closed" else "Active"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PaymentStatusBadge(item.cycleSummary.paymentStatus)
            }

            LinearProgressIndicator(
                progress = {
                    if (item.tuition.contractedLectures > 0)
                        (item.cycleSummary.completedLectures.toFloat() / item.tuition.contractedLectures).coerceIn(0f, 1f)
                    else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Lectures: ${item.cycleSummary.completedLectures}/${item.tuition.contractedLectures}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Paid: ৳ ${item.cycleSummary.paidAmount.toInt()} / ৳ ${item.cycleSummary.cycle.totalSalary.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (item.cycleSummary.outstandingAmount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Due: ৳ ${item.cycleSummary.outstandingAmount.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onRecordPayment) {
                        Text("Record Payment", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
