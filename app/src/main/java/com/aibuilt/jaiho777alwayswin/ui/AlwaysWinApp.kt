package com.aibuilt.jaiho777alwayswin.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aibuilt.jaiho777alwayswin.viewmodel.MainViewModel

private const val ROUTE_GAME = "game"
private const val ROUTE_SETTINGS = "settings"

@Composable
fun AlwaysWinApp(vm: MainViewModel) {
    val nav = rememberNavController()
    val showIntro by vm.showIntro.collectAsState()
    var noticeOpen by remember { mutableStateOf(false) }

    LaunchedEffect(showIntro) {
        if (showIntro) noticeOpen = true
    }

    NavHost(navController = nav, startDestination = ROUTE_GAME) {
        composable(ROUTE_GAME) {
            GameScreen(
                vm = vm,
                onOpenSettings = { nav.navigate(ROUTE_SETTINGS) },
                onShowNotice = { noticeOpen = true }
            )
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(vm = vm, onBack = { nav.popBackStack() })
        }
    }

    if (noticeOpen) {
        InstallNoticeDialog(
            onDismiss = {
                noticeOpen = false
                if (showIntro) vm.dismissIntro()
            }
        )
    }
}
