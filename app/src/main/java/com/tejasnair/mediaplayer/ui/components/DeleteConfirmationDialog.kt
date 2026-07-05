package com.tejasnair.mediaplayer.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.tejasnair.mediaplayer.R


@Composable
fun DeleteConfirmationDialog(
    primaryText: String,
    secondaryText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        icon = { Icon(painter = painterResource(id = R.drawable.options_delete), contentDescription = "Delete Icon") },
        title = { Text(text = primaryText, textAlign = TextAlign.Center) },
        text = { Text(text = secondaryText, textAlign = TextAlign.Center) },
        iconContentColor = MaterialTheme.colorScheme.error
    )
}