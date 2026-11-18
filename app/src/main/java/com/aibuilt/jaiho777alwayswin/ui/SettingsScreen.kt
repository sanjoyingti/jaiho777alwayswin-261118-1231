package com.aibuilt.jaiho777alwayswin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aibuilt.jaiho777alwayswin.model.ModConfig
import com.aibuilt.jaiho777alwayswin.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: MainViewModel, onBack: () -> Unit) {

    val config by vm.config.collectAsState()
    val scroll = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Always win", style = MaterialTheme.typography.titleMedium)
                    ToggleRow("Enable the patch", config.patchEnabled) { vm.setPatchEnabled(it) }
                    ToggleRow("Force server replies to succeed", config.forceCode) { vm.setForceCode(it) }
                    ToggleRow("Multiply every win", config.scaleWins) { vm.setScaleWins(it) }
                    ToggleRow("Add a win to winning-less spins", config.injectWin) { vm.setInjectWin(it) }
                    ToggleRow("Coins can only go up", config.coinFloor) { vm.setCoinFloor(it) }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Win multiplier: x${config.multiplier}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = config.multiplier.toFloat(),
                        onValueChange = { vm.setMultiplier(it.toInt()) },
                        valueRange = ModConfig.MULTIPLIER_RANGE.first.toFloat()..ModConfig.MULTIPLIER_RANGE.last.toFloat(),
                        steps = ModConfig.MULTIPLIER_RANGE.count() - 2
                    )
                    Text(
                        "Extra win per spin: x${config.winFactor} of the bet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = config.winFactor.toFloat(),
                        onValueChange = { vm.setWinFactor(it.toInt()) },
                        valueRange = ModConfig.WIN_FACTOR_RANGE.first.toFloat()..ModConfig.WIN_FACTOR_RANGE.last.toFloat(),
                        steps = ModConfig.WIN_FACTOR_RANGE.count() - 2
                    )
                    BetField(
                        value = config.defaultBet.toString(),
                        onChange = { text -> text.toIntOrNull()?.let { vm.setDefaultBet(it) } }
                    )
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Game address", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = config.gameUrl,
                        onValueChange = vm::setGameUrl,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = vm::resetSettings) { Text("Reset all") }
                Button(onClick = vm::clearLog) { Text("Clear log") }
            }

            Text(
                "Changed settings apply the next time the game reloads.",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.fillMaxWidth(0.8f), fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun BetField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("Default bet for a win-less spin") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
