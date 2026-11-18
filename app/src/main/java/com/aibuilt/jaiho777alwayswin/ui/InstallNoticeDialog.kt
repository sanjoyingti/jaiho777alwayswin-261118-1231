package com.aibuilt.jaiho777alwayswin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InstallNoticeDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Read this once") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "1. Uninstall the original Jaiho 777 first.",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "This build is signed with a new key, so Android refuses to install it over the shop version. " +
                        "Your account stays on the server - log in again after installing.",
                    fontSize = 13.sp
                )
                Text("2. Allow installs from this source.", style = MaterialTheme.typography.titleSmall)
                Text(
                    "When Android asks, tap Settings and allow your browser or file manager to install unknown apps.",
                    fontSize = 13.sp
                )
                Text("3. Open the app and log in normally.", style = MaterialTheme.typography.titleSmall)
                Text(
                    "The game runs exactly as the original one does: same servers, same splash, same languages, " +
                        "same shop and wallet. Only the win handling is changed.",
                    fontSize = 13.sp
                )
                Text("What the patch does", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Every reply the server sends is treated as a success, every win and jackpot amount is " +
                        "multiplied, and a spin that comes back without any win still gets one worth 3x your bet. " +
                        "The coin balance you see can only go up.",
                    fontSize = 13.sp
                )
                Text(
                    "Multiplier, extra win, coin floor and the game address can all be changed in Settings. " +
                        "The live log shows what the patch is doing while you play.",
                    fontSize = 13.sp
                )
                Text(
                    "If a screen looks stale after changing settings, tap the reload button.",
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it, let me play") }
        }
    )
}

@Composable
fun NoticeButtonRow(onShow: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onShow, modifier = modifier.padding(0.dp)) {
        Text("Install notice", fontSize = 12.sp)
    }
}
