package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CycleSummary
import com.example.util.DateTimeUtils
import java.util.Calendar
import java.util.Locale

@Composable
fun EmbeddedCalendar(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDateStr: String,
    lectureDates: Set<String>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (dateStr: String) -> Unit,
    cycles: List<CycleSummary> = emptyList(),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("embedded_home_calendar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Month Header with previous/next navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateTimeUtils.formatYearMonth(selectedYear, selectedMonth),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("calendar_prev_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("calendar_next_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day of Week Headers (Mo, Tu, We, Th, Fr, Sa, Su)
            val daysOfWeek = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Compute Month Grid
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth - 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val offset = (firstDayOfWeek + 5) % 7

            val totalCells = ((offset + daysInMonth + 6) / 7) * 7
            val rows = totalCells / 7

            // -------------------------------------------------------------
            // Compute highlighted intervals for cycles based on 3 rules:
            // 1. If a cycle has both Start and End date, highlight from start date to end date.
            // 2. If a cycle has a start date but another cycle is recorded which has a closer start date from "today()",
            //    then just highlight the start date of this cycle.
            // 3. If a cycle has a start date and no cycle is recorded which has a closer start date from "today()",
            //    then just highlight from the start date of the cycle to Today().
            // -------------------------------------------------------------
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayStartM = todayCal.timeInMillis
            val todayEndM = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            data class CycleHighlightSpan(
                val startMillis: Long,
                val endMillis: Long,
                val isClosed: Boolean
            )

            val validCycles = cycles.filter { it.cycle.startDate.isNotBlank() }

            val cycleHighlightSpans = validCycles.map { c ->
                val sCal = DateTimeUtils.parseDateToCalendar(c.cycle.startDate).apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startM = sCal.timeInMillis
                val sEndOfDayCal = DateTimeUtils.parseDateToCalendar(c.cycle.startDate).apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val startEndOfDayM = sEndOfDayCal.timeInMillis

                val hasEndDate = !c.cycle.endDate.isNullOrBlank()

                if (hasEndDate) {
                    // Rule 1: Cycle has both Start and End date -> highlight from Start to End date
                    val eCal = DateTimeUtils.parseDateToCalendar(c.cycle.endDate).apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    val endM = eCal.timeInMillis
                    CycleHighlightSpan(
                        startMillis = minOf(startM, endM),
                        endMillis = maxOf(startM, endM),
                        isClosed = true
                    )
                } else {
                    // Cycle has start date but no end date
                    val thisDist = Math.abs(startM - todayStartM)
                    val anotherCycleWithCloserStartDate = validCycles.any { other ->
                        if (other.cycle.cycleId == c.cycle.cycleId) false
                        else {
                            val otherSCal = DateTimeUtils.parseDateToCalendar(other.cycle.startDate).apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            val otherStartM = otherSCal.timeInMillis
                            val otherDist = Math.abs(otherStartM - todayStartM)
                            otherDist < thisDist || (otherDist == thisDist && other.cycle.cycleNumber > c.cycle.cycleNumber)
                        }
                    }

                    if (anotherCycleWithCloserStartDate) {
                        // Rule 2: Another cycle is recorded which has a closer start date from "today()"
                        // -> Just highlight the start date of this cycle
                        CycleHighlightSpan(
                            startMillis = startM,
                            endMillis = startEndOfDayM,
                            isClosed = false
                        )
                    } else {
                        // Rule 3: No cycle is recorded which has a closer start date from "today()"
                        // -> Highlight from start date of the cycle to Today()
                        CycleHighlightSpan(
                            startMillis = minOf(startM, todayStartM),
                            endMillis = maxOf(startEndOfDayM, todayEndM),
                            isClosed = false
                        )
                    }
                }
            }

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - offset + 1

                        if (dayNumber in 1..daysInMonth) {
                            val cellCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, selectedYear)
                                set(Calendar.MONTH, selectedMonth - 1)
                                set(Calendar.DAY_OF_MONTH, dayNumber)
                                set(Calendar.HOUR_OF_DAY, 12)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            val cellMillis = cellCal.timeInMillis

                            val cellDateStr = String.format(
                                Locale.US,
                                "%02d-%s-%02d",
                                dayNumber,
                                cellCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US),
                                selectedYear % 100
                            )

                            val isSelected = cellDateStr.equals(selectedDateStr, ignoreCase = true)
                            val hasLectures = lectureDates.contains(cellDateStr)

                            // Check cycle span membership according to the 3 rules
                            val isInClosedCycleSpan = cycleHighlightSpans.any { it.isClosed && cellMillis in it.startMillis..it.endMillis }
                            val isInOngoingCycleSpan = cycleHighlightSpans.any { !it.isClosed && cellMillis in it.startMillis..it.endMillis }

                            // Visual backgrounds
                            val spanBgColor = when {
                                isInClosedCycleSpan -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                isInOngoingCycleSpan -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(1.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(spanBgColor)
                                    .clickable { onDateSelected(cellDateStr) }
                                    .testTag("calendar_day_$dayNumber"),
                                contentAlignment = Alignment.Center
                            ) {
                                val circleBg = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(circleBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$dayNumber",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected || hasLectures) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp
                                        )

                                        if (hasLectures) {
                                            Spacer(modifier = Modifier.height(1.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                        else MaterialTheme.colorScheme.secondary
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Blank cell
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
