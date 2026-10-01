package com.tejasnair.mediaplayer.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.model.Vinyl
import com.tejasnair.mediaplayer.data.model.VinylSide
import com.tejasnair.mediaplayer.ui.components.StandardUIBar
import com.tejasnair.mediaplayer.ui.theme.ThemedScreen
import com.tejasnair.mediaplayer.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import kotlin.collections.emptyList

@Composable
fun VinylsScreen(
    navController: NavController,
    libraryViewModel: LibraryViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val vinylsWithSides by libraryViewModel.allVinyls.collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(value = false) }

    ThemedScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            Column(modifier = Modifier.padding(bottom = 56.dp, top = 16.dp)) {
                StandardUIBar(navController = navController, title = "Vinyls")
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 12.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(count = 2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 16.dp)
                ) {
                    items(items = vinylsWithSides) { record ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape = RoundedCornerShape(size = 12.dp))
                                .clickable(onClick = { navController.navigate(route = "vinyl/${record.vinyl.vinylId}") })
                                .padding(all = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(ratio = 1f)
                                    .clip(shape = RoundedCornerShape(size = 8.dp))
                                    .background(color = MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                val artUri = record.vinyl.coverArtUri
                                if (artUri != null && File(artUri).exists()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = File(artUri)),
                                        contentDescription = record.vinyl.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.vinyl_placeholder),
                                        contentDescription = "Placeholder art",
                                        modifier = Modifier.size(size = 48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(height = 8.dp))
                            Text(
                                text = record.vinyl.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = record.vinyl.artist ?: "Unknown Artist",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape = RoundedCornerShape(size = 12.dp))
                                .clickable(onClick = { showCreateDialog = true })
                                .padding(all = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(ratio = 1f)
                                    .clip(shape = RoundedCornerShape(size = 8.dp))
                                    .background(color = MaterialTheme.colorScheme.background)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(size = 12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.add),
                                    contentDescription = "New Vinyl",
                                    modifier = Modifier.size(size = 32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(height = 8.dp))
                            Text(
                                text = "Create New Vinyl",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (showCreateDialog) {
                CreateVinylDialog(
                    onDismiss = { showCreateDialog = false },
                    onCreateRequested = { title, artist, imageUri ->
                        coroutineScope.launch {
                            val newVinylId = UUID.randomUUID().toString()
                            var savedArtPath: String? = null

                            imageUri?.let { uri ->
                                savedArtPath = libraryViewModel.copyVinylImageToInternalStorage(
                                    context = context,
                                    uri = uri,
                                    vinylId = newVinylId
                                )
                            }

                            val newVinyl = Vinyl(
                                vinylId = newVinylId,
                                title = title,
                                artist = artist.ifBlank { null },
                                coverArtUri = savedArtPath
                            )

                            libraryViewModel.createVinyl(vinyl = newVinyl)
                            libraryViewModel.createVinylSide(side = VinylSide(vinylId = newVinylId, position = 1, sideName = "Side A"))

                            showCreateDialog = false
                            navController.navigate(route = "vinyl/$newVinylId")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CreateVinylDialog(
    onDismiss: () -> Unit,
    onCreateRequested: (String, String, Uri?) -> Unit
) {
    var title by remember { mutableStateOf(value = "") }
    var artist by remember { mutableStateOf(value = "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(value = null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Vinyl Record",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(space = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(size = 100.dp)
                        .clip(shape = RoundedCornerShape(size = 8.dp))
                        .background(color = MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = selectedImageUri),
                            contentDescription = "Selected artwork",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.options_edit),
                                contentDescription = "Upload Art",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(height = 4.dp))
                            Text(
                                text = "Add Cover",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = "Title (Required)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text(text = "Artist (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { onCreateRequested(title, artist, selectedImageUri) }
            ) {
                Text(text = "Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
        shape = RoundedCornerShape(size = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}