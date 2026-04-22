package com.uc.caffeine.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.uc.caffeine.R
import com.uc.caffeine.data.HealthConnectManager
import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.ui.components.SettingsPageScaffold
import com.uc.caffeine.ui.components.rememberAppHaptics
import com.uc.caffeine.ui.theme.CaffeineSurfaceDefaults
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HealthConnectSettingsScreen(
    userSettings: UserSettings,
    healthConnectManager: HealthConnectManager,
    onHealthConnectToggle: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(healthConnectManager.permissions)) {
            onHealthConnectToggle(true)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permission denied — Health Connect sync disabled")
            }
        }
    }

    fun onToggle(enabled: Boolean) {
        haptics.toggle()
        if (!enabled) {
            onHealthConnectToggle(false)
            return
        }
        if (!healthConnectManager.isAvailable()) {
            scope.launch {
                snackbarHostState.showSnackbar("Health Connect is not installed on this device")
            }
            return
        }
        scope.launch {
            if (healthConnectManager.hasPermission()) {
                onHealthConnectToggle(true)
            } else {
                permissionLauncher.launch(healthConnectManager.permissions)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsPageScaffold(
            title = "Health Connect",
            showBackButton = true,
            onBack = onBack,
        ) { bottomPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = bottomPadding + 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "Share your caffeine intake with Health Connect to seamlessly transfer data to fitness apps like Fitbit, enable AI-powered health insights, and consolidate your wellness data in one place.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                SegmentedListItem(
                    onClick = { onToggle(!userSettings.healthConnectEnabled) },
                    leadingContent = {
                        Image(
                            painter = painterResource(R.drawable.health_connect_logo),
                            contentDescription = "Health Connect",
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    content = {
                        Text(text = "Sync with Health Connect")
                    },
                    supportingContent = {
                        Text(
                            text = if (healthConnectManager.isAvailable())
                                "Write caffeine intake as nutrition records"
                            else
                                "Health Connect is not installed on this device"
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = userSettings.healthConnectEnabled,
                            onCheckedChange = { enabled -> onToggle(enabled) },
                            enabled = healthConnectManager.isAvailable(),
                        )
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
                    colors = ListItemDefaults.colors(
                        containerColor = CaffeineSurfaceDefaults.groupedListContainerColor,
                    ),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
