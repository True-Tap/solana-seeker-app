package com.truetap.solana.seeker.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.truetap.solana.seeker.services.FeePreset

@Composable
fun FeePresetSelector(
    selected: FeePreset,
    onSelected: (FeePreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = remember { listOf(FeePreset.NORMAL, FeePreset.FAST, FeePreset.EXPRESS) }
    val showTooltipState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Row(modifier = modifier) {
        options.forEachIndexed { idx, preset ->
            FilterChip(
                selected = selected == preset,
                onClick = { onSelected(preset) },
                label = { Text(labelFor(preset)) }
            )
            if (idx != options.lastIndex) Spacer(modifier = Modifier.width(8.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = { showTooltipState.value = true }) {
            Icon(Icons.Outlined.Info, contentDescription = "Fee preset info")
        }
    }
    if (showTooltipState.value) {
        AlertDialog(
            onDismissRequest = { showTooltipState.value = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showTooltipState.value = false }) { Text("Got it") }
            },
            title = { Text("Network speed presets") },
            text = {
                Text("Normal (0), Fast (500), Express (5000) microLamports per compute unit. Higher fees speed up confirmation when the network is busy.")
            }
        )
    }
}

private fun labelFor(p: FeePreset) = when (p) {
    FeePreset.NORMAL -> "Normal"
    FeePreset.FAST -> "Fast"
    FeePreset.EXPRESS -> "Express"
}


