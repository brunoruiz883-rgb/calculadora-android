package com.example.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.OutfitFontFamily

@Composable
fun CalculatorScreen(
    state: CalculatorState,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNeumorphicColors.current
    var showHistorySheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top App Bar with Controls
            TopControlsBar(
                isDark = colors.isDark,
                isHapticEnabled = state.isHapticEnabled,
                historyCount = state.history.size,
                onToggleTheme = { onAction(CalculatorAction.ToggleTheme) },
                onToggleHaptic = { onAction(CalculatorAction.ToggleHaptic) },
                onOpenHistory = { showHistorySheet = true }
            )

            // Display Screen Area
            NeumorphicDisplay(
                expressionPreview = state.expressionPreview,
                displayValue = state.displayValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            // Keypad Grid
            KeypadGrid(
                state = state,
                onAction = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        // History Bottom Sheet Overlay
        AnimatedVisibility(
            visible = showHistorySheet,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            HistoryOverlay(
                history = state.history,
                onSelectItem = { item ->
                    onAction(CalculatorAction.SelectHistory(item))
                    showHistorySheet = false
                },
                onClearHistory = {
                    onAction(CalculatorAction.ClearHistory)
                },
                onClose = { showHistorySheet = false }
            )
        }
    }
}

@Composable
private fun TopControlsBar(
    isDark: Boolean,
    isHapticEnabled: Boolean,
    historyCount: Int,
    onToggleTheme: () -> Unit,
    onToggleHaptic: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val colors = LocalNeumorphicColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NEOCALC",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                letterSpacing = 1.5.sp,
                color = colors.textPrimary
            )
        }

        // Control Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // History Button with Badge
            Box {
                NeumorphicIconButton(
                    icon = Icons.Default.History,
                    contentDescription = stringResource(R.string.calc_history),
                    onClick = onOpenHistory,
                    testTag = "btn_history"
                )
                if (historyCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(colors.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (historyCount > 9) "9+" else "$historyCount",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Haptics Toggle
            NeumorphicIconButton(
                icon = Icons.Default.Vibration,
                contentDescription = stringResource(R.string.calc_vibration_toggle),
                onClick = onToggleHaptic,
                isActive = isHapticEnabled,
                testTag = "btn_toggle_haptic"
            )

            // Theme Toggle
            NeumorphicIconButton(
                icon = if (isDark) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                contentDescription = if (isDark) stringResource(R.string.calc_theme_light) else stringResource(R.string.calc_theme_dark),
                onClick = onToggleTheme,
                testTag = "btn_toggle_theme"
            )
        }
    }
}

@Composable
private fun KeypadGrid(
    state: CalculatorState,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = state.isHapticEnabled
    val spacing = 12.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Row 1: AC, +/-, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            val isClearAll = state.number1.isBlank() && state.number2.isBlank() && state.displayValue == "0"
            NeumorphicCalculatorButton(
                text = if (isClearAll) "AC" else "C",
                onClick = { onAction(CalculatorAction.Clear) },
                styleType = ButtonStyleType.SPECIAL,
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_clear"
            )
            NeumorphicCalculatorButton(
                text = "±",
                onClick = { onAction(CalculatorAction.ToggleSign) },
                styleType = ButtonStyleType.SPECIAL,
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_toggle_sign"
            )
            NeumorphicCalculatorButton(
                text = "%",
                onClick = { onAction(CalculatorAction.Percentage) },
                styleType = ButtonStyleType.SPECIAL,
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_percent"
            )
            NeumorphicCalculatorButton(
                text = CalculatorOperation.DIVIDE.symbol,
                onClick = { onAction(CalculatorAction.Operation(CalculatorOperation.DIVIDE)) },
                styleType = ButtonStyleType.OPERATOR,
                isSelectedOperator = state.operation == CalculatorOperation.DIVIDE && state.number2.isBlank(),
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_op_divide"
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            NeumorphicCalculatorButton(
                text = "7",
                onClick = { onAction(CalculatorAction.Number(7)) },
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_num_7"
            )
            NeumorphicCalculatorButton(
                text = "8",
                onClick = { onAction(CalculatorAction.Number(8)) },
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_num_8"
            )
            NeumorphicCalculatorButton(
                text = "9",
                onClick = { onAction(CalculatorAction.Number(9)) },
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_num_9"
            )
            NeumorphicCalculatorButton(
                text = CalculatorOperation.MULTIPLY.symbol,
                onClick = { onAction(CalculatorAction.Operation(CalculatorOperation.MULTIPLY)) },
                styleType = ButtonStyleType.OPERATOR,
                isSelectedOperator = state.operation == CalculatorOperation.MULTIPLY && state.number2.isBlank(),
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_op_multiply"
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            NeumorphicCalculatorButton(
                text = "4",
                onClick = { onAction(CalculatorAction.Number(4)) },
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_num_4"
            )
            NeumorphicCalculatorButton(
                text = "5",
                onClick = { onAction(CalculatorAction.Number(5)) },
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_num_5"
            )
            NeumorphicCalculatorButton(
                text = "6",
                onClick = { onAction(CalculatorAction.Number(6)) },
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_num_6"
            )
            NeumorphicCalculatorButton(
                text = CalculatorOperation.SUBTRACT.symbol,
                onClick = { onAction(CalculatorAction.Operation(CalculatorOperation.SUBTRACT)) },
                styleType = ButtonStyleType.OPERATOR,
                isSelectedOperator = state.operation == CalculatorOperation.SUBTRACT && state.number2.isBlank(),
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_op_subtract"
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            NeumorphicCalculatorButton(
                text = "1",
                onClick = { onAction(CalculatorAction.Number(1)) },
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_num_1"
            )
            NeumorphicCalculatorButton(
                text = "2",
                onClick = { onAction(CalculatorAction.Number(2)) },
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_num_2"
            )
            NeumorphicCalculatorButton(
                text = "3",
                onClick = { onAction(CalculatorAction.Number(3)) },
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_num_3"
            )
            NeumorphicCalculatorButton(
                text = CalculatorOperation.ADD.symbol,
                onClick = { onAction(CalculatorAction.Operation(CalculatorOperation.ADD)) },
                styleType = ButtonStyleType.OPERATOR,
                isSelectedOperator = state.operation == CalculatorOperation.ADD && state.number2.isBlank(),
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_op_add"
            )
        }

        // Row 5: 0, ., ⌫, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            NeumorphicCalculatorButton(
                text = "0",
                onClick = { onAction(CalculatorAction.Number(0)) },
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_num_0"
            )
            NeumorphicCalculatorButton(
                text = ".",
                onClick = { onAction(CalculatorAction.Decimal) },
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_decimal"
            )
            NeumorphicCalculatorButton(
                text = "⌫",
                onClick = { onAction(CalculatorAction.Delete) },
                styleType = ButtonStyleType.SPECIAL,
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_delete"
            )
            NeumorphicCalculatorButton(
                text = "=",
                onClick = { onAction(CalculatorAction.Calculate) },
                styleType = ButtonStyleType.EQUALS,
                enabledHaptics = haptics,
                modifier = Modifier.weight(1f),
                testTag = "btn_calculate"
            )
        }
    }
}

@Composable
private fun HistoryOverlay(
    history: List<CalculationHistoryItem>,
    onSelectItem: (CalculationHistoryItem) -> Unit,
    onClearHistory: () -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalNeumorphicColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {} // Prevent closing when tapping sheet
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(colors.surface)
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.calc_history),
                    fontSize = 20.sp,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (history.isNotEmpty()) {
                        IconButton(
                            onClick = onClearHistory,
                            modifier = Modifier.testTag("btn_clear_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = stringResource(R.string.calc_clear_history),
                                tint = colors.textSecondary
                            )
                        }
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("btn_close_history")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = colors.textSecondary
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = colors.textSecondary.copy(alpha = 0.2f)
            )

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.calc_no_history),
                        color = colors.textSecondary,
                        fontSize = 15.sp,
                        fontFamily = OutfitFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(history, key = { it.id }) { item ->
                        NeumorphicBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectItem(item) },
                            shape = RoundedCornerShape(16.dp),
                            elevation = 3.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = item.expression,
                                    fontSize = 14.sp,
                                    fontFamily = OutfitFontFamily,
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "= ${item.result}",
                                    fontSize = 20.sp,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
