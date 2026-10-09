package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.StudentEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    student: StudentEntity?,
    onBackClick: () -> Unit,
    onSaveStudent: (
        id: String,
        tuitionId: String,
        name: String,
        nickname: String,
        contact: String,
        fatherContact: String,
        motherContact: String,
        whatsApp: String,
        telegram: String,
        discord: String
    ) -> Unit,
    onDeleteStudent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (student == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Student Details") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Student not found")
            }
        }
        return
    }

    var isEditing by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Form fields
    var name by remember { mutableStateOf(student.name) }
    var nickname by remember { mutableStateOf(student.nickname) }
    var contact by remember { mutableStateOf(student.contact) }
    var fatherContact by remember { mutableStateOf(student.fatherContact) }
    var motherContact by remember { mutableStateOf(student.motherContact) }
    var whatsApp by remember { mutableStateOf(student.whatsApp) }
    var telegram by remember { mutableStateOf(student.telegram) }
    var discord by remember { mutableStateOf(student.discord) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = student.displayName.ifBlank { "Student Details" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("student_detail_back_btn")) {
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
                            text = { Text("Edit Student") },
                            onClick = {
                                menuExpanded = false
                                isEditing = true
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete Student", color = MaterialTheme.colorScheme.error) },
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
            Text(
                text = "Student Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (isEditing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name *") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_student_name"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text("Nickname") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_student_nickname"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = contact,
                            onValueChange = { contact = it },
                            label = { Text("Contact (Phone)") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_student_contact"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = fatherContact,
                            onValueChange = { fatherContact = it },
                            label = { Text("Father's Contact (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = motherContact,
                            onValueChange = { motherContact = it },
                            label = { Text("Mother's Contact (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = whatsApp,
                            onValueChange = { whatsApp = it },
                            label = { Text("WhatsApp (Phone number or wa.me link)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = telegram,
                            onValueChange = { telegram = it },
                            label = { Text("Telegram (Username or t.me link)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = discord,
                            onValueChange = { discord = it },
                            label = { Text("Discord (Tag or discord.gg link)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isEditing = false }) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    if (name.isBlank()) {
                                        Toast.makeText(context, "Full name is required", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    onSaveStudent(
                                        student.studentId,
                                        student.tuitionId,
                                        name.trim(),
                                        nickname.trim(),
                                        contact.trim(),
                                        fatherContact.trim(),
                                        motherContact.trim(),
                                        whatsApp.trim(),
                                        telegram.trim(),
                                        discord.trim()
                                    )
                                    isEditing = false
                                    Toast.makeText(context, "Student updated", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("save_student_changes_btn")
                            ) {
                                Text("Save Changes")
                            }
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Full Name
                        FieldDisplay(
                            label = "Full Name:",
                            value = student.name.ifBlank { "Not provided" }
                        )

                        // Nickname
                        if (student.nickname.isNotBlank()) {
                            FieldDisplay(
                                label = "Nickname:",
                                value = student.nickname
                            )
                        }

                        // Primary Contact
                        if (student.contact.isNotBlank()) {
                            ContactFieldDisplay(
                                label = "Contact:",
                                number = student.contact,
                                onDial = { dialPhone(context, student.contact) }
                            )
                        }

                        // Father's Contact
                        if (student.fatherContact.isNotBlank()) {
                            ContactFieldDisplay(
                                label = "Father's Contact:",
                                number = student.fatherContact,
                                onDial = { dialPhone(context, student.fatherContact) }
                            )
                        }

                        // Mother's Contact
                        if (student.motherContact.isNotBlank()) {
                            ContactFieldDisplay(
                                label = "Mother's Contact:",
                                number = student.motherContact,
                                onDial = { dialPhone(context, student.motherContact) }
                            )
                        }

                        // WhatsApp
                        val effectiveWhatsApp = student.whatsApp.ifBlank { student.contact }
                        if (effectiveWhatsApp.isNotBlank()) {
                            LinkFieldDisplay(
                                label = "WhatsApp:",
                                actionText = "Open WhatsApp message",
                                onOpen = { openWhatsAppChat(context, effectiveWhatsApp) }
                            )
                        }

                        // Telegram
                        if (student.telegram.isNotBlank()) {
                            LinkFieldDisplay(
                                label = "Telegram:",
                                actionText = "Open Telegram conversation",
                                onOpen = { openTelegram(context, student.telegram) }
                            )
                        }

                        // Discord
                        if (student.discord.isNotBlank()) {
                            LinkFieldDisplay(
                                label = "Discord:",
                                actionText = "Open Discord profile",
                                onOpen = { openDiscord(context, student.discord) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Student") },
            text = { Text("Are you sure you want to remove this student? Student records will be archived.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteStudent(student.studentId)
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

@Composable
private fun FieldDisplay(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ContactFieldDisplay(label: String, number: String, onDial: () -> Unit) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onDial, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Dial",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LinkFieldDisplay(label: String, actionText: String, onOpen: () -> Unit) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun dialPhone(context: Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${number.trim()}"))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open phone dialer", Toast.LENGTH_SHORT).show()
    }
}

private fun openWhatsAppChat(context: Context, contactOrUrl: String) {
    try {
        val clean = contactOrUrl.trim()
        val url = if (clean.startsWith("http://") || clean.startsWith("https://")) {
            clean
        } else {
            val phoneDigits = clean.filter { it.isDigit() || it == '+' }
            "https://wa.me/$phoneDigits"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
    }
}

private fun openTelegram(context: Context, usernameOrUrl: String) {
    try {
        val clean = usernameOrUrl.trim()
        val url = when {
            clean.startsWith("http://") || clean.startsWith("https://") -> clean
            clean.startsWith("@") -> "https://t.me/${clean.removePrefix("@")}"
            else -> "https://t.me/$clean"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open Telegram", Toast.LENGTH_SHORT).show()
    }
}

private fun openDiscord(context: Context, discordValue: String) {
    try {
        val clean = discordValue.trim()
        val url = if (clean.startsWith("http://") || clean.startsWith("https://")) {
            clean
        } else {
            "https://discord.com/users/$clean"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open Discord", Toast.LENGTH_SHORT).show()
    }
}
