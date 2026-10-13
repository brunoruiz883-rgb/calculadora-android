package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.CalculatorAction
import com.example.calculator.CalculatorOperation
import com.example.calculator.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Neumorphic Calc", appName)
    }

    @Test
    fun `calculator performs addition correctly`() {
        val vm = CalculatorViewModel()
        vm.onAction(CalculatorAction.Number(5))
        vm.onAction(CalculatorAction.Operation(CalculatorOperation.ADD))
        vm.onAction(CalculatorAction.Number(7))
        vm.onAction(CalculatorAction.Calculate)

        val state = vm.state.value
        assertEquals("12", state.displayValue)
        assertEquals(1, state.history.size)
        assertEquals("5 + 7 =", state.history[0].expression)
        assertEquals("12", state.history[0].result)
    }

    @Test
    fun `calculator handles decimals and clearing`() {
        val vm = CalculatorViewModel()
        vm.onAction(CalculatorAction.Number(3))
        vm.onAction(CalculatorAction.Decimal)
        vm.onAction(CalculatorAction.Number(5))
        assertEquals("3.5", vm.state.value.displayValue)

        vm.onAction(CalculatorAction.Clear)
        assertEquals("0", vm.state.value.displayValue)
    }
}
