package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.SalaryPaymentEntity
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    cycleId: String,
    tuitionId: String,
    suggestedAmount: Double = 0.0,
    cycleTargetSalary: Double = 0.0,
    cycleTotalPaid: Double = 0.0,
    existingPayment: SalaryPaymentEntity? = null,
    onDismiss: () -> Unit,
    onSavePayment: (
        cycleId: String,
        tuitionId: String,
        amount: Double,
        paymentDate: String,
        paymentMethod: String,
        notes: String
    ) -> Unit,
    onUpdatePayment: ((
        paymentId: String,
        amount: Double,
        paymentDate: String,
        paymentMethod: String,
        notes: String
    ) -> Unit)? = null,
    onDeletePayment: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var amountText by remember {
        mutableStateOf(
            if (existingPayment != null) existingPayment.amount.toString()
            else if (suggestedAmount > 0) suggestedAmount.toString()
            else ""
        )
    }

    var paymentDate by remember {
        mutableStateOf(existingPayment?.paymentDate ?: DateTimeUtils.getCurrentDate())
    }

    val paymentMethods = listOf("Cash", "bKash", "Nagad", "Bank Transfer", "Other")
    var paymentMethod by remember {
        mutableStateOf("Cash")
    }
    var notes by remember {
        mutableStateOf(existingPayment?.note ?: "")
    }

    var methodMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val remainingBeforeThis = (cycleTargetSalary - cycleTotalPaid).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existingPayment != null) "Edit Payment" else "Record Payment",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (existingPayment != null && onDeletePayment != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Payment",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                if (cycleTargetSalary > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cycle Target", style = MaterialTheme.typography.bodySmall)
                                Text("৳ ${cycleTargetSalary.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Paid so far", style = MaterialTheme.typography.bodySmall)
                                Text("৳ ${cycleTotalPaid.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remaining Due", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "৳ ${remainingBeforeThis.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingBeforeThis > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_payment_amount"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Clickable native Date picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            DateTimeUtils.showNativeDatePicker(context, paymentDate) { formatted, _ ->
                                paymentDate = formatted
                            }
                        }
                ) {
                    OutlinedTextField(
                        value = paymentDate,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Payment Date *") },
                        trailingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
                        },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Payment Method
                ExposedDropdownMenuBox(
                    expanded = methodMenuExpanded,
                    onExpandedChange = { methodMenuExpanded = !methodMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = methodMenuExpanded,
                        onDismissRequest = { methodMenuExpanded = false }
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    paymentMethod = method
                                    methodMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Receipt Ref (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (existingPayment != null && onUpdatePayment != null) {
                                onUpdatePayment(
                                    existingPayment.paymentId,
                                    amount,
                                    paymentDate,
                                    paymentMethod,
                                    notes.trim()
                                )
                            } else {
                                onSavePayment(
                                    cycleId,
                                    tuitionId,
                                    amount,
                                    paymentDate,
                                    paymentMethod,
                                    notes.trim()
                                )
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_payment"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && existingPayment != null && onDeletePayment != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Payment") },
            text = { Text("Are you sure you want to delete this payment record?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeletePayment(existingPayment.paymentId)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
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
