package com.example.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class CalculatorViewModel : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private val maxDigits = 12

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Decimal -> enterDecimal()
            is CalculatorAction.Clear -> clear()
            is CalculatorAction.Delete -> delete()
            is CalculatorAction.Operation -> enterOperation(action.operation)
            is CalculatorAction.Calculate -> performCalculation()
            is CalculatorAction.ToggleSign -> toggleSign()
            is CalculatorAction.Percentage -> calculatePercentage()
            is CalculatorAction.ToggleTheme -> toggleTheme()
            is CalculatorAction.ToggleHaptic -> toggleHaptic()
            is CalculatorAction.SelectHistory -> selectHistory(action.item)
            is CalculatorAction.ClearHistory -> clearHistory()
        }
    }

    private fun enterNumber(number: Int) {
        _state.update { current ->
            if (current.isResultCalculated && current.operation == null) {
                // User starts fresh after a result
                val newNum = number.toString()
                current.copy(
                    number1 = newNum,
                    number2 = "",
                    displayValue = newNum,
                    expressionPreview = "",
                    isResultCalculated = false
                )
            } else if (current.operation == null) {
                if (current.number1.replace(".", "").replace("-", "").length >= maxDigits) return@update current
                val newNum = if (current.number1 == "0") number.toString() else current.number1 + number
                current.copy(
                    number1 = newNum,
                    displayValue = formatForDisplay(newNum),
                    expressionPreview = ""
                )
            } else {
                if (current.number2.replace(".", "").replace("-", "").length >= maxDigits) return@update current
                val newNum = if (current.number2 == "0") number.toString() else current.number2 + number
                current.copy(
                    number2 = newNum,
                    displayValue = formatForDisplay(newNum),
                    expressionPreview = "${formatForDisplay(current.number1)} ${current.operation.symbol}"
                )
            }
        }
    }

    private fun enterDecimal() {
        _state.update { current ->
            if (current.isResultCalculated && current.operation == null) {
                current.copy(
                    number1 = "0.",
                    displayValue = "0.",
                    expressionPreview = "",
                    isResultCalculated = false
                )
            } else if (current.operation == null) {
                if (current.number1.contains(".")) return@update current
                val newNum = if (current.number1.isBlank()) "0." else "${current.number1}."
                current.copy(
                    number1 = newNum,
                    displayValue = newNum
                )
            } else {
                if (current.number2.contains(".")) return@update current
                val newNum = if (current.number2.isBlank()) "0." else "${current.number2}."
                current.copy(
                    number2 = newNum,
                    displayValue = newNum
                )
            }
        }
    }

    private fun enterOperation(op: CalculatorOperation) {
        _state.update { current ->
            if (current.number1.isBlank()) {
                val num1 = "0"
                return@update current.copy(
                    number1 = num1,
                    operation = op,
                    expressionPreview = "$num1 ${op.symbol}"
                )
            }

            if (current.number2.isNotBlank()) {
                // Consecutive calculation (chaining)
                val intermediateResult = compute(current.number1, current.number2, current.operation)
                if (intermediateResult == null) {
                    return@update current.copy(
                        number1 = "",
                        number2 = "",
                        operation = null,
                        displayValue = "Error",
                        expressionPreview = ""
                    )
                }
                current.copy(
                    number1 = intermediateResult,
                    number2 = "",
                    operation = op,
                    displayValue = formatForDisplay(intermediateResult),
                    expressionPreview = "${formatForDisplay(intermediateResult)} ${op.symbol}",
                    isResultCalculated = false
                )
            } else {
                current.copy(
                    operation = op,
                    expressionPreview = "${formatForDisplay(current.number1)} ${op.symbol}",
                    isResultCalculated = false
                )
            }
        }
    }

    private fun performCalculation() {
        val current = _state.value
        if (current.number1.isBlank() || current.operation == null || current.number2.isBlank()) {
            return
        }

        val result = compute(current.number1, current.number2, current.operation)
        if (result == null) {
            _state.update {
                it.copy(
                    number1 = "",
                    number2 = "",
                    operation = null,
                    displayValue = "Error",
                    expressionPreview = "${it.number1} ${it.operation?.symbol} ${it.number2} =",
                    isResultCalculated = true
                )
            }
            return
        }

        val expression = "${formatForDisplay(current.number1)} ${current.operation.symbol} ${formatForDisplay(current.number2)} ="
        val formattedResult = formatForDisplay(result)
        val historyEntry = CalculationHistoryItem(
            expression = expression,
            result = formattedResult
        )

        _state.update {
            val updatedHistory = listOf(historyEntry) + it.history.take(49)
            it.copy(
                expressionPreview = expression,
                displayValue = formattedResult,
                number1 = result,
                number2 = "",
                operation = null,
                isResultCalculated = true,
                history = updatedHistory
            )
        }
    }

    private fun delete() {
        _state.update { current ->
            if (current.isResultCalculated) {
                return@update current.copy(
                    number1 = "",
                    number2 = "",
                    operation = null,
                    displayValue = "0",
                    expressionPreview = "",
                    isResultCalculated = false
                )
            }

            if (current.operation != null && current.number2.isNotBlank()) {
                val newNum2 = current.number2.dropLast(1)
                current.copy(
                    number2 = newNum2,
                    displayValue = if (newNum2.isBlank()) "0" else formatForDisplay(newNum2)
                )
            } else if (current.operation != null && current.number2.isBlank()) {
                current.copy(
                    operation = null,
                    expressionPreview = "",
                    displayValue = formatForDisplay(current.number1)
                )
            } else if (current.number1.isNotBlank()) {
                val newNum1 = current.number1.dropLast(1)
                current.copy(
                    number1 = newNum1,
                    displayValue = if (newNum1.isBlank()) "0" else formatForDisplay(newNum1)
                )
            } else {
                current.copy(displayValue = "0")
            }
        }
    }

    private fun clear() {
        _state.update {
            it.copy(
                number1 = "",
                number2 = "",
                operation = null,
                displayValue = "0",
                expressionPreview = "",
                isResultCalculated = false
            )
        }
    }

    private fun toggleSign() {
        _state.update { current ->
            if (current.operation == null) {
                if (current.number1.isBlank() || current.number1 == "0") return@update current
                val toggled = if (current.number1.startsWith("-")) {
                    current.number1.removePrefix("-")
                } else {
                    "-${current.number1}"
                }
                current.copy(
                    number1 = toggled,
                    displayValue = formatForDisplay(toggled)
                )
            } else {
                if (current.number2.isBlank() || current.number2 == "0") return@update current
                val toggled = if (current.number2.startsWith("-")) {
                    current.number2.removePrefix("-")
                } else {
                    "-${current.number2}"
                }
                current.copy(
                    number2 = toggled,
                    displayValue = formatForDisplay(toggled)
                )
            }
        }
    }

    private fun calculatePercentage() {
        _state.update { current ->
            if (current.operation == null) {
                if (current.number1.isBlank()) return@update current
                try {
                    val value = BigDecimal(current.number1).divide(BigDecimal(100), 8, RoundingMode.HALF_UP)
                    val resultStr = stripTrailingZeros(value)
                    current.copy(
                        number1 = resultStr,
                        displayValue = formatForDisplay(resultStr)
                    )
                } catch (e: Exception) {
                    current
                }
            } else {
                if (current.number2.isBlank()) return@update current
                try {
                    val base = BigDecimal(current.number1)
                    val percent = BigDecimal(current.number2).divide(BigDecimal(100), 8, RoundingMode.HALF_UP)
                    val calculated = base.multiply(percent)
                    val resultStr = stripTrailingZeros(calculated)
                    current.copy(
                        number2 = resultStr,
                        displayValue = formatForDisplay(resultStr)
                    )
                } catch (e: Exception) {
                    current
                }
            }
        }
    }

    private fun toggleTheme() {
        _state.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    private fun toggleHaptic() {
        _state.update { it.copy(isHapticEnabled = !it.isHapticEnabled) }
    }

    private fun selectHistory(item: CalculationHistoryItem) {
        // Strip formatted commas to extract raw number
        val rawResult = item.result.replace(",", "")
        _state.update {
            it.copy(
                number1 = rawResult,
                number2 = "",
                operation = null,
                displayValue = item.result,
                expressionPreview = item.expression,
                isResultCalculated = true
            )
        }
    }

    private fun clearHistory() {
        _state.update { it.copy(history = emptyList()) }
    }

    private fun compute(n1: String, n2: String, op: CalculatorOperation?): String? {
        if (op == null) return n1
        return try {
            val d1 = BigDecimal(n1)
            val d2 = BigDecimal(n2)
            val result = when (op) {
                CalculatorOperation.ADD -> d1.add(d2)
                CalculatorOperation.SUBTRACT -> d1.subtract(d2)
                CalculatorOperation.MULTIPLY -> d1.multiply(d2)
                CalculatorOperation.DIVIDE -> {
                    if (d2.compareTo(BigDecimal.ZERO) == 0) return null
                    d1.divide(d2, 8, RoundingMode.HALF_UP)
                }
            }
            stripTrailingZeros(result)
        } catch (e: Exception) {
            null
        }
    }

    private fun stripTrailingZeros(value: BigDecimal): String {
        val stripped = value.stripTrailingZeros()
        return stripped.toPlainString()
    }

    private fun formatForDisplay(raw: String): String {
        if (raw.isBlank() || raw == "0") return "0"
        if (raw == "Error") return "Error"
        if (raw.endsWith(".")) return raw
        return try {
            val isNegative = raw.startsWith("-")
            val clean = if (isNegative) raw.removePrefix("-") else raw
            val formatted = if (clean.contains(".")) {
                val parts = clean.split(".")
                val integerPart = parts[0].toLongOrNull()?.let {
                    DecimalFormat("#,###").format(it)
                } ?: parts[0]
                "$integerPart.${parts[1]}"
            } else {
                val num = BigDecimal(clean)
                DecimalFormat("#,###").format(num)
            }
            if (isNegative) "-$formatted" else formatted
        } catch (e: Exception) {
            raw
        }
    }
}
