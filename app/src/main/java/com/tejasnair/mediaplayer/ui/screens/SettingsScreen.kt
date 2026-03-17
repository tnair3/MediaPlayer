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
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextDecoration
import android.content.Intent
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.tejasnair.mediaplayer.ui.components.StandardUIBar
import com.tejasnair.mediaplayer.ui.components.ThemeMode
import com.tejasnair.mediaplayer.BuildConfig
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navController: NavController,
    currentSetting: ThemeMode,
    onSettingChanged: (ThemeMode) -> Unit
) {

    val context = LocalContext.current
    val packageInfo = context.packageManager
        .getPackageInfo(context.packageName, 0)

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