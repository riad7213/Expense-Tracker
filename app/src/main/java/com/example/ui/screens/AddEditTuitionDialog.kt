package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.TuitionEntity
import com.example.util.DateTimeUtils

@Composable
fun AddEditTuitionDialog(
    initialTuition: TuitionEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        location: String,
        classGrade: String,
        tuitionType: String,
        daysPerWeek: Int,
        startingDate: String,
        totalSalary: Double,
        mapLink: String,
        studentName: String,
        studentContact: String
    ) -> Unit
) {
    val context = LocalContext.current

    var location by remember { mutableStateOf(initialTuition?.location ?: "") }
    var classGrade by remember { mutableStateOf(initialTuition?.classGrade ?: "") }
    var tuitionType by remember { mutableStateOf(initialTuition?.tuitionType ?: "Physical") }
    var daysPerWeek by remember { mutableIntStateOf(initialTuition?.daysPerWeek ?: 3) }
    var startingDate by remember { mutableStateOf(initialTuition?.startingDate ?: DateTimeUtils.getCurrentDate()) }
    var salaryText by remember { mutableStateOf(if (initialTuition != null && initialTuition.totalSalary > 0) initialTuition.totalSalary.toInt().toString() else "") }
    var mapLink by remember { mutableStateOf(initialTuition?.mapLink ?: "") }
    var studentName by remember { mutableStateOf("") }
    var studentContact by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    var locationError by remember { mutableStateOf(false) }
    var classError by remember { mutableStateOf(false) }

    val classSuggestions = listOf("Class 8", "Class 9", "Class 10", "O-Level", "A-Level", "Admission")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("add_tuition_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTuition == null) "New Tuition Profile" else "Edit Tuition Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location field
                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        location = it
                        if (it.isNotBlank()) locationError = false
                    },
                    label = { Text("Location") },
                    placeholder = { Text("e.g. Dhanmondi, Rd 4 or Zoom") },
                    isError = locationError,
                    supportingText = if (locationError) { { Text("Location is required") } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tuition_location_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Map Link field (Google Maps link)
                OutlinedTextField(
                    value = mapLink,
                    onValueChange = { mapLink = it },
                    label = { Text("Map Link (Optional)") },
                    placeholder = { Text("e.g. https://maps.google.com/?q=...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    supportingText = { Text("Paste Google Maps link for 1-tap navigation") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tuition_map_link_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Class field
                OutlinedTextField(
                    value = classGrade,
                    onValueChange = {
                        classGrade = it
                        if (it.isNotBlank()) classError = false
                    },
                    label = { Text("Class / Standard") },
                    placeholder = { Text("e.g. O-Level, Class 9, Admission") },
                    isError = classError,
                    supportingText = if (classError) { { Text("Class is required") } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tuition_class_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick suggestions chips for Class
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    classSuggestions.forEach { suggestion ->
                        FilterChip(
                            selected = classGrade == suggestion,
                            onClick = {
                                classGrade = suggestion
                                classError = false
                            },
                            label = { Text(suggestion) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tuition Type: Online vs Physical
                Text(
                    text = "Tuition Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val typeOptions = listOf("Physical", "Online")
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    typeOptions.forEachIndexed { index, option ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = typeOptions.size),
                            onClick = { tuitionType = option },
                            selected = tuitionType == option,
                            modifier = Modifier.testTag("type_option_$option")
                        ) {
                            Text(option)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Days per Week: 2 / 3 / 4 days/wk
                Text(
                    text = "Days per Week (Determines Contracted Lectures)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val daysOptions = listOf(2, 3, 4)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    daysOptions.forEachIndexed { index, days ->
                        val contracted = when (days) {
                            2 -> 8
                            3 -> 12
                            else -> 16
                        }
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = daysOptions.size),
                            onClick = { daysPerWeek = days },
                            selected = daysPerWeek == days,
                            modifier = Modifier.testTag("days_option_$days")
                        ) {
                            Text("$days d/wk ($contracted lec)")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Starting Date (Native DatePicker on tap) & Total Salary row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Clickable native DatePicker input
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clickable {
                                DateTimeUtils.showNativeDatePicker(context, startingDate) { formatted, _ ->
                                    startingDate = formatted
                                }
                            }
                    ) {
                        OutlinedTextField(
                            value = startingDate,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Starting Date") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Open Calendar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tuition_start_date_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = salaryText,
                        onValueChange = { salaryText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Monthly Salary (BDT)") },
                        placeholder = { Text("e.g. 12000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("tuition_salary_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (initialTuition != null) {
                    val originalContracted = when (initialTuition.daysPerWeek) {
                        2 -> 8
                        3 -> 12
                        else -> 16
                    }
                    val originalSalary = initialTuition.totalSalary
                    val currentSalary = salaryText.toDoubleOrNull() ?: 0.0
                    val currentContracted = when (daysPerWeek) {
                        2 -> 8
                        3 -> 12
                        else -> 16
                    }
                    if (originalContracted != currentContracted || originalSalary != currentSalary) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ Notice: This changes the contract for future Salary Cycles. Existing cycles will remain unchanged.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                if (initialTuition == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Initial Student (Optional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Student Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tuition_student_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = studentContact,
                        onValueChange = { studentContact = it },
                        label = { Text("Student Contact / Phone") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tuition_student_contact_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (isSubmitting) return@Button
                            if (location.isBlank()) {
                                locationError = true
                                return@Button
                            }
                            if (classGrade.isBlank()) {
                                classError = true
                                return@Button
                            }
                            isSubmitting = true
                            val salaryDouble = salaryText.toDoubleOrNull() ?: 0.0
                            onSave(
                                location,
                                classGrade,
                                tuitionType,
                                daysPerWeek,
                                startingDate.ifBlank { DateTimeUtils.getCurrentDate() },
                                salaryDouble,
                                mapLink,
                                studentName,
                                studentContact
                            )
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_tuition_submit_btn")
                    ) {
                        Text(if (isSubmitting) "Saving..." else "Save Tuition")
                    }
                }
            }
        }
    }
}
