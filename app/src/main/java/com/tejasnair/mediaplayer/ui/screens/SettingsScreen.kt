package com.tejasnair.mediaplayer.ui.screens

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.navigation.NavController
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import com.tejasnair.mediaplayer.BuildConfig
import com.tejasnair.mediaplayer.ui.components.StandardUIBar
import com.tejasnair.mediaplayer.ui.components.formatBytes
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import com.tejasnair.mediaplayer.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    libraryViewModel: LibraryViewModel,
    navController: NavController,
) {

    val context = LocalContext.current
    val packageInfo = context.packageManager
        .getPackageInfo(context.packageName, 0)

    // State to manage the visibility of the two warning dialogs
    var showFirstWarning by remember { mutableStateOf(value = false) }
    var showFinalWarning by remember { mutableStateOf(value = false) }

    LaunchedEffect(key1 = Unit) {
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
                    Text(
                        text = "Continue",
                        color = MaterialTheme.colorScheme.error)
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
                    Text(
                        text = "Clear Everything",
                        color = MaterialTheme.colorScheme.error
                    )
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
                    text = "Songs stored: ${libraryViewModel.allSongs.collectAsState().value.size}",
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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