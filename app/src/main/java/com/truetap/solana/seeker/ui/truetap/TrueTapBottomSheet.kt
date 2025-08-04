package com.truetap.solana.seeker.ui.truetap

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.truetap.solana.seeker.ui.theme.TrueTapContainer
import com.truetap.solana.seeker.ui.truetap.components.*
import com.truetap.solana.seeker.viewmodels.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueTapBottomSheet(
    viewModel: WalletViewModel,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var currentStep by remember { mutableStateOf(TrueTapStep.SELECT_RECIPIENT) }
    val trueTapState by viewModel.trueTapState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    LaunchedEffect(trueTapState.lastTransaction) {
        if (trueTapState.lastTransaction != null) {
            currentStep = TrueTapStep.SUCCESS
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetTrueTap()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = TrueTapContainer
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally() togetherWith
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally()
            },
            label = "step_transition"
        ) { step ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp)
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                when (step) {
                    TrueTapStep.SELECT_RECIPIENT -> {
                        RecipientSelector(
                            contacts = viewModel.trueTapContacts.collectAsState().value,
                            onSelect = { contact ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.selectRecipient(contact)
                                currentStep = TrueTapStep.ENTER_AMOUNT
                            }
                        )
                    }
                    
                    TrueTapStep.ENTER_AMOUNT -> {
                        AmountInput(
                            balance = viewModel.balance.collectAsState().value,
                            onConfirm = { amount, emoji ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setAmount(amount, emoji)
                                currentStep = TrueTapStep.CONFIRM
                            },
                            onBack = { currentStep = TrueTapStep.SELECT_RECIPIENT }
                        )
                    }
                    
                    TrueTapStep.CONFIRM -> {
                        SwipeToSend(
                            recipient = trueTapState.selectedRecipient!!,
                            amount = trueTapState.amount,
                            message = trueTapState.emojiMessage,
                            isLoading = trueTapState.isLoading,
                            onSend = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.executeTrueTap()
                            },
                            onBack = { currentStep = TrueTapStep.ENTER_AMOUNT }
                        )
                    }
                    
                    TrueTapStep.SUCCESS -> {
                        SuccessAnimation(
                            transaction = trueTapState.lastTransaction!!,
                            onDone = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.resetTrueTap()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class TrueTapStep {
    SELECT_RECIPIENT,
    ENTER_AMOUNT,
    CONFIRM,
    SUCCESS
}