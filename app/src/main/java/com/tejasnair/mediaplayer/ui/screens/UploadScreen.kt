package com.tejasnair.mediaplayer.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.local.files.MediaScanner

@Composable
fun UploadScreen(
    navController: NavController,
    mediaScanner: MediaScanner
) {

    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        scope.launch {
            uris.forEach { uri ->
                mediaScanner.scanAudioFile(uri)
            }
            navController.navigateUp()
        }
    }

    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
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
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.nav_upload),
                    contentDescription = "Upload Icon",
                    modifier = Modifier.size(120.dp), // Size of your icon
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Upload Songs",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        launcher.launch("audio/*")
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Select Files",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}