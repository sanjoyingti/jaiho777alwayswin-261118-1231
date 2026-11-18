package com.aibuilt.jaiho777alwayswin.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.aibuilt.jaiho777alwayswin.data.LogLine
import com.aibuilt.jaiho777alwayswin.data.PatchStatus
import com.aibuilt.jaiho777alwayswin.model.ModConfig
import com.aibuilt.jaiho777alwayswin.viewmodel.MainViewModel

private const val PATCH_ASSET = "alwayswin.js"

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    vm: MainViewModel,
    onOpenSettings: () -> Unit,
    onShowNotice: () -> Unit = {}
) {

    val config by vm.config.collectAsState()
    val status by vm.status.collectAsState()
    val lines by vm.lines.collectAsState()
    val multiplier = config.multiplier
    val winFactor = config.winFactor
    val context = LocalContext.current

    var showLog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var loadedUrl by remember { mutableStateOf<String?>(null) }

    val patchSource = remember { readPatch(context) }
    val bridge = remember { Bridge(vm) }

    BackHandler(enabled = webView?.canGoBack() == true) { webView?.goBack() }

    LaunchedEffect(status) {
        if (status == PatchStatus.ACTIVE || status == PatchStatus.FAILED) showLog = true
    }

    LaunchedEffect(multiplier, winFactor) {
        if (patchSource.isNotBlank()) webView?.evaluateJavascript(patchSource, null)
    }

    LaunchedEffect(config.gameUrl, webView) {
        val view = webView ?: return@LaunchedEffect
        val target = config.gameUrl.trim().ifEmpty { ModConfig.DEFAULT_URL }
        if (loadedUrl != target) {
            loadedUrl = target
            errorText = null
            loading = true
            vm.clearStatus()
            view.loadUrl(target)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jaiho777 AlwaysWin") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { showLog = !showLog }) {
                        Icon(Icons.Filled.Terminal, contentDescription = "Show or hide the live log")
                    }
                    IconButton(onClick = { webView?.reload() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reload the game")
                    }
                    IconButton(onClick = onShowNotice) {
                        Icon(Icons.Filled.Info, contentDescription = "Install notice")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Open settings")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportZoom(false)
                        settings.mediaPlaybackRequiresUserGesture = false
                        addJavascriptInterface(bridge, "AwpBridge")
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: Bitmap?
                            ) {
                                loading = true
                                vm.noteInjection()
                                inject(view, patchSource)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                loading = false
                                inject(view, patchSource)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame != true) return
                                loading = false
                                errorText = error?.description?.toString() ?: "Unknown network error"
                                vm.note("load error: $errorText")
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean = false
                        }
                        val start = config.gameUrl.trim().ifEmpty { ModConfig.DEFAULT_URL }
                        loadedUrl = start
                        loadUrl(start)
                        webView = this
                    }
                },
                update = { view -> webView = view }
            )

            if (loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }

            errorText?.let { message ->
                ErrorCard(
                    message = message,
                    onRetry = {
                        errorText = null
                        loading = true
                        webView?.reload()
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp)
                )
            }

            StatusChip(
                status = status,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )

            if (showLog) {
                LogCard(
                    lines = lines,
                    onClose = { showLog = false },
                    onClear = vm::clearLog,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: PatchStatus, modifier: Modifier = Modifier) {
    val label = when (status) {
        PatchStatus.IDLE -> "Idle"
        PatchStatus.INJECTING -> "Attaching…"
        PatchStatus.ACTIVE -> "Always-win ON"
        PatchStatus.FAILED -> "Patch failed"
    }
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (status == PatchStatus.INJECTING) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
            }
            Text(label, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("The game could not be loaded", style = MaterialTheme.typography.titleMedium)
            Text(message, fontSize = 12.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            Button(onClick = onRetry) { Text("Try again") }
        }
    }
}

@Composable
private fun LogCard(
    lines: List<LogLine>,
    onClose: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.heightIn(max = 240.dp)) {
        Column(Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("LIVE LOG", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "clear",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable { onClear() }
                    )
                    Text(
                        "hide",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable { onClose() }
                    )
                }
            }
            if (lines.isEmpty()) {
                Text(
                    "Nothing yet. The patch reports here once the engine answers.",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(lines) { line ->
                        Text(
                            "${line.time}  ${line.text}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun readPatch(context: Context): String = try {
    context.assets.open(PATCH_ASSET).bufferedReader().use { it.readText() }
} catch (e: Exception) {
    ""
}

private fun inject(view: WebView?, source: String) {
    if (view == null || source.isBlank()) return
    view.evaluateJavascript(source, null)
}
