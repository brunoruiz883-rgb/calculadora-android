package com.example.calculator

import java.util.UUID

sealed interface CalculatorAction {
    data class Number(val number: Int) : CalculatorAction
    data object Decimal : CalculatorAction
    data object Clear : CalculatorAction
    data object Delete : CalculatorAction
    data class Operation(val operation: CalculatorOperation) : CalculatorAction
    data object Calculate : CalculatorAction
    data object ToggleSign : CalculatorAction
    data object Percentage : CalculatorAction
    data object ToggleTheme : CalculatorAction
    data object ToggleHaptic : CalculatorAction
    data class SelectHistory(val item: CalculationHistoryItem) : CalculatorAction
    data object ClearHistory : CalculatorAction
}

enum class CalculatorOperation(val symbol: String) {
    ADD("+"),
    SUBTRACT("−"),
    MULTIPLY("×"),
    DIVIDE("÷")
}

data class CalculationHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CalculatorState(
    val number1: String = "",
    val number2: String = "",
    val operation: CalculatorOperation? = null,
    val displayValue: String = "0",
    val expressionPreview: String = "",
    val isResultCalculated: Boolean = false,
    val history: List<CalculationHistoryItem> = emptyList(),
    val isDarkTheme: Boolean = true,
    val isHapticEnabled: Boolean = true
)
