package com.tejasnair.mediaplayer.ui.screens

// 1. Android & Core
import android.annotation.SuppressLint
import android.content.Intent
import androidx.core.net.toUri
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity

// 2. Compose UI, Layout & Graphics
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

// 3. Compose Runtime
import androidx.compose.runtime.*

// 4. Material3
import androidx.compose.material3.*

// 5. Navigation
import androidx.navigation.NavController

// 6. Local Project Imports
import com.tejasnair.mediaplayer.BuildConfig
import com.tejasnair.mediaplayer.ui.components.StandardUIBar
import com.tejasnair.mediaplayer.ui.components.ThemeMode
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    libraryViewModel: LibraryViewModel,
    navController: NavController,
    currentSetting: ThemeMode,
    onSettingChanged: (ThemeMode) -> Unit,
) {

    val context = LocalContext.current
    val packageInfo = context.packageManager
        .getPackageInfo(context.packageName, 0)

    // State to manage the visibility of the two warning dialogs
    var showFirstWarning by remember { mutableStateOf(false) }
    var showFinalWarning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        libraryViewModel.loadLibrarySize()
    }
    val librarySize = libraryViewModel.librarySize

    // Warning dialogues
    if (showFirstWarning) {
        AlertDialog(
            onDismissRequest = { showFirstWarning = false },
            title = { Text("Clear Library?") },
            text = { Text("This will remove all indexed media from your local database. Do you want to proceed?") },
            confirmButton = {
                TextButton(onClick = {
                    showFirstWarning = false
                    showFinalWarning = true
                }) {
                    Text("Continue", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFirstWarning = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFinalWarning) {
        AlertDialog(
            onDismissRequest = { showFinalWarning = false },
            title = { Text("Final Warning") },
            text = { Text("This action is permanent and cannot be undone. Are you absolutely sure?") },
            confirmButton = {
                TextButton(onClick = {
                    showFinalWarning = false
                    libraryViewModel.clearLibrary()
                }) {
                    Text("Clear Everything", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalWarning = false }) {
                    Text("Go Back")
                }
            }
        )
    }

    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 56.dp, top = 16.dp)
            ) {
                StandardUIBar(
                    navController = navController,
                    title = "Settings"
                )

                HorizontalDivider(
                    modifier = Modifier
                        .padding(bottom = 12.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Appearance Subsection
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 6.dp),
                    text = "Appearance",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                ThemeMode.entries.forEach { setting ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .selectable(
                                selected = (setting == currentSetting),
                                onClick = { onSettingChanged(setting) }
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (setting == currentSetting),
                            onClick = null
                        )
                        Text(
                            text = setting.name.lowercase().replaceFirstChar{
                                if (it.isLowerCase()) it.titlecase() else it.toString() },
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Library Subsection
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 6.dp, top = 14.dp),
                    text = "Library",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = "Storage used: ${formatBytes(librarySize)}",
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Clear Library",
                    style = MaterialTheme.typography.labelMedium.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .clickable {
                            showFirstWarning = true
                        }
                )

                // About Subsection
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 6.dp, top = 14.dp),
                    text = "About",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = "Version " + packageInfo.versionName,
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Build: ${BuildConfig.BUILD_TYPE} ${BuildConfig.BUILD_DATE}",
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Third-Party Licenses",
                    style = MaterialTheme.typography.labelMedium.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .clickable {
                            context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                        }
                )

                Text(
                    text = "GitHub",
                    style = MaterialTheme.typography.labelMedium.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 8.dp, start = 16.dp)
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://github.com/tnair3/".toUri()
                            )
                            context.startActivity(intent)
                        }
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatBytes(bytes: Long): String {
    val kb = 1024
    val mb = kb * 1024
    val gb = mb * 1024

    return when {
        bytes >= gb -> String.format("%.2f GB", bytes.toFloat() / gb)
        bytes >= mb -> String.format("%.2f MB", bytes.toFloat() / mb)
        bytes >= kb -> String.format("%.2f KB", bytes.toFloat() / kb)
        else -> "$bytes B"
    }
}