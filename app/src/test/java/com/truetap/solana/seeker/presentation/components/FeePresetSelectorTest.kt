package com.truetap.solana.seeker.presentation.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.truetap.solana.seeker.services.FeePreset
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FeePresetSelectorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun selecting_fast_updates_callback() {
        var selected: FeePreset? = null
        composeTestRule.setContent {
            FeePresetSelector(selected = FeePreset.NORMAL, onSelected = { selected = it })
        }

        composeTestRule.onNodeWithText("Fast").performClick()
        assertEquals(FeePreset.FAST, selected)
    }
}


