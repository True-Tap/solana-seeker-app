package com.truetap.solana.seeker.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
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
    Row(modifier = modifier) {
        options.forEachIndexed { idx, preset ->
            FilterChip(
                selected = selected == preset,
                onClick = { onSelected(preset) },
                label = { Text(labelFor(preset)) }
            )
            if (idx != options.lastIndex) Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

private fun labelFor(p: FeePreset) = when (p) {
    FeePreset.NORMAL -> "Normal"
    FeePreset.FAST -> "Fast"
    FeePreset.EXPRESS -> "Express"
}


