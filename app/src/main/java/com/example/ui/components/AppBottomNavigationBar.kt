package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.AppNavDestination

@Composable
fun AppBottomNavigationBar(
    currentDestination: AppNavDestination,
    onNavigate: (AppNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        // Home is first as requested
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentDestination == AppNavDestination.HOME,
            onClick = { onNavigate(AppNavDestination.HOME) },
            modifier = Modifier.testTag("nav_item_home"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Tuitions") },
            label = { Text("Tuitions") },
            selected = currentDestination == AppNavDestination.TUITIONS,
            onClick = { onNavigate(AppNavDestination.TUITIONS) },
            modifier = Modifier.testTag("nav_item_tuitions"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Lectures") },
            label = { Text("Lectures") },
            selected = currentDestination == AppNavDestination.LECTURES,
            onClick = { onNavigate(AppNavDestination.LECTURES) },
            modifier = Modifier.testTag("nav_item_lectures"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Salary") },
            label = { Text("Salary") },
            selected = currentDestination == AppNavDestination.SALARY,
            onClick = { onNavigate(AppNavDestination.SALARY) },
            modifier = Modifier.testTag("nav_item_salary"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
