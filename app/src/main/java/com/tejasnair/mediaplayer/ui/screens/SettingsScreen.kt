package com.tejasnair.mediaplayer.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import com.tejasnair.mediaplayer.ui.viewmodel.SettingsViewModel
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.material3.RadioButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.TextButton
import android.content.Intent
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.ui.components.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navController: NavController,
    currentSetting: ThemeMode,
    onSettingChanged: (ThemeMode) -> Unit
) {

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {

                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.nav_back_arrow),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

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
                            onClick = null // Selected via the Row's clickable
                        )
                        Text(
                            text = setting.name.lowercase().replaceFirstChar{ if (it.isLowerCase()) it.titlecase() else it.toString() },
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // About Subsection
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 6.dp, top = 14.dp),
                    text = "About",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                val context = LocalContext.current
                val packageInfo = context.packageManager
                    .getPackageInfo(context.packageName, 0)
                Text(
                    text = "Version " + packageInfo.versionName,
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/tnair3/".toUri()
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "GitHub",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}