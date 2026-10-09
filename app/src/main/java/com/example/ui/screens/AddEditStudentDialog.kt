package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.StudentEntity

@Composable
fun AddEditStudentDialog(
    initialStudent: StudentEntity? = null,
    tuitionId: String,
    onDismiss: () -> Unit,
    onSave: (
        id: String?,
        tuitionId: String,
        name: String,
        nickname: String,
        contact: String,
        fatherContact: String,
        motherContact: String,
        whatsApp: String,
        telegram: String,
        discord: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialStudent?.name ?: "") }
    var nickname by remember { mutableStateOf(initialStudent?.nickname ?: "") }
    var contact by remember { mutableStateOf(initialStudent?.contact ?: "") }
    var fatherContact by remember { mutableStateOf(initialStudent?.fatherContact ?: "") }
    var motherContact by remember { mutableStateOf(initialStudent?.motherContact ?: "") }
    var whatsApp by remember { mutableStateOf(initialStudent?.whatsApp ?: "") }
    var telegram by remember { mutableStateOf(initialStudent?.telegram ?: "") }
    var discord by remember { mutableStateOf(initialStudent?.discord ?: "") }
    var nameError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("add_edit_student_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialStudent == null) "Add Student" else "Edit Student",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Full Name *") },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Full name is required") } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_form_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname (Preferred Name)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_form_nickname_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Number (Phone)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("student_form_contact_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = fatherContact,
                    onValueChange = { fatherContact = it },
                    label = { Text("Father's Contact (Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = motherContact,
                    onValueChange = { motherContact = it },
                    label = { Text("Mother's Contact (Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = whatsApp,
                    onValueChange = { whatsApp = it },
                    label = { Text("WhatsApp (Optional phone / link)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = telegram,
                    onValueChange = { telegram = it },
                    label = { Text("Telegram (Optional username / link)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = discord,
                    onValueChange = { discord = it },
                    label = { Text("Discord (Optional tag / link)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            onSave(
                                initialStudent?.studentId,
                                tuitionId,
                                name.trim(),
                                nickname.trim(),
                                contact.trim(),
                                fatherContact.trim(),
                                motherContact.trim(),
                                whatsApp.trim(),
                                telegram.trim(),
                                discord.trim()
                            )
                        },
                        modifier = Modifier.testTag("save_student_dialog_btn")
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
