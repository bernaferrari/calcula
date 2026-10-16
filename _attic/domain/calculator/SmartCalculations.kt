package com.bernaferrari.calculator.domain.calculator

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

/**
 * Smart calculation utilities for intuitive calculator operations
 */
object SmartCalculations {
    /**
     * Smart percentage calculations based on context
     * Examples:
     * - 100 + 10% = 110 (adds 10% of 100)
     * - 100 - 10% = 90 (subtracts 10% of 100)
     * - 100 × 10% = 10 (calculates 10% of 100)
     * - 50 ÷ 10% = 500 (what is 50 as 10% of?)
     */
    fun calculateSmartPercentage(
        base: BigDecimal,
        percentage: BigDecimal,
        operator: String,
    ): BigDecimal {
        val percentValue = percentage.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)

        return when (operator) {
            "+" -> base + (base * percentValue)
            "-" -> base - (base * percentValue)
            "×", "*" -> base * percentValue
            "÷", "/" -> base.divide(percentValue, 10, RoundingMode.HALF_UP)
            else -> base
        }
    }

    /**
     * Quick percentage calculations
     */
    fun quickPercentageOf(
        value: BigDecimal,
        percentage: BigDecimal,
    ): BigDecimal = value * percentage.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)

    /**
     * Calculate what percentage one value is of another
     * Example: 25 is what % of 100? Answer: 25%
     */
    fun whatPercentageIs(
        value: BigDecimal,
        ofValue: BigDecimal,
    ): BigDecimal {
        if (ofValue == BigDecimal.ZERO) return BigDecimal.ZERO
        return value.divide(ofValue, 10, RoundingMode.HALF_UP) * BigDecimal("100")
    }

    /**
     * Calculate percentage change between two values
     * Example: From 100 to 150 = 50% increase
     */
    fun percentageChange(
        oldValue: BigDecimal,
        newValue: BigDecimal,
    ): BigDecimal {
        if (oldValue == BigDecimal.ZERO) return BigDecimal.ZERO
        val change = newValue - oldValue
        return change.divide(oldValue, 10, RoundingMode.HALF_UP) * BigDecimal("100")
    }

    /**
     * Smart tip calculation
     */
    fun calculateTip(
        amount: BigDecimal,
        tipPercentage: BigDecimal,
    ): TipResult {
        val tip = quickPercentageOf(amount, tipPercentage)
        val total = amount + tip
        return TipResult(
            tipAmount = tip,
            totalAmount = total,
            tipPercentage = tipPercentage,
        )
    }

    /**
     * Split bill calculation
     */
    fun splitBill(
        totalAmount: BigDecimal,
        numberOfPeople: Int,
        tipPercentage: BigDecimal? = null,
    ): SplitBillResult {
        val tipAmount =
            if (tipPercentage != null) {
                quickPercentageOf(totalAmount, tipPercentage)
            } else {
                BigDecimal.ZERO
            }

        val grandTotal = totalAmount + tipAmount
        val perPerson = grandTotal.divide(BigDecimal(numberOfPeople), 2, RoundingMode.HALF_UP)

        return SplitBillResult(
            totalAmount = totalAmount,
            tipAmount = tipAmount,
            grandTotal = grandTotal,
            perPerson = perPerson,
            numberOfPeople = numberOfPeople,
        )
    }

    /**
     * Loan/Interest calculations
     */
    fun calculateSimpleInterest(
        principal: BigDecimal,
        rate: BigDecimal,
        timeInYears: BigDecimal,
    ): InterestResult {
        val interest = principal * rate.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP) * timeInYears
        val total = principal + interest

        return InterestResult(
            principal = principal,
            interest = interest,
            total = total,
            rate = rate,
            time = timeInYears,
        )
    }

    /**
     * Quick ratio calculations (golden ratio, aspect ratios, etc.)
     */
    fun calculateRatio(
        value1: BigDecimal,
        value2: BigDecimal,
    ): RatioResult {
        val gcd = findGCD(value1.toBigInteger(), value2.toBigInteger())
        val simplified1 = value1.divide(BigDecimal(gcd), 10, RoundingMode.HALF_UP)
        val simplified2 = value2.divide(BigDecimal(gcd), 10, RoundingMode.HALF_UP)

        return RatioResult(
            original = "$value1:$value2",
            simplified = "$simplified1:$simplified2",
            decimal = if (value2 != BigDecimal.ZERO) value1.divide(value2, 10, RoundingMode.HALF_UP) else BigDecimal.ZERO,
        )
    }

    private fun findGCD(
        a: java.math.BigInteger,
        b: java.math.BigInteger,
    ): java.math.BigInteger = if (b == java.math.BigInteger.ZERO) a else findGCD(b, a.remainder(b))

    /**
     * Common calculations patterns
     */
    fun suggestNextCalculation(
        expression: String,
        result: BigDecimal,
    ): List<CalculationSuggestion> {
        val suggestions = mutableListOf<CalculationSuggestion>()

        // Percentage suggestions
        suggestions.add(
            CalculationSuggestion(
                label = "10% of ${formatNumber(result)}",
                expression = "$result × 10%",
                result = quickPercentageOf(result, BigDecimal("10")),
            ),
        )

        suggestions.add(
            CalculationSuggestion(
                label = "20% of ${formatNumber(result)}",
                expression = "$result × 20%",
                result = quickPercentageOf(result, BigDecimal("20")),
            ),
        )

        // Square and square root
        if (result > BigDecimal.ZERO) {
            suggestions.add(
                CalculationSuggestion(
                    label = "√${formatNumber(result)}",
                    expression = "√($result)",
                    result = BigDecimal(kotlin.math.sqrt(result.toDouble())),
                ),
            )
        }

        suggestions.add(
            CalculationSuggestion(
                label = "${formatNumber(result)}²",
                expression = "$result²",
                result = result * result,
            ),
        )

        // Common operations
        suggestions.add(
            CalculationSuggestion(
                label = "× 2",
                expression = "$result × 2",
                result = result * BigDecimal("2"),
            ),
        )

        suggestions.add(
            CalculationSuggestion(
                label = "÷ 2",
                expression = "$result ÷ 2",
                result = result.divide(BigDecimal("2"), 10, RoundingMode.HALF_UP),
            ),
        )

        return suggestions
    }

    private fun formatNumber(value: BigDecimal): String {
        val stripped = value.stripTrailingZeros()
        return when {
            stripped.scale() <= 0 -> stripped.toPlainString()
            abs(stripped.toDouble()) < 0.001 -> String.format("%.2e", value.toDouble())
            abs(stripped.toDouble()) > 1000000 -> String.format("%.2e", value.toDouble())
            else -> stripped.toPlainString()
        }
    }
}

data class TipResult(
    val tipAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val tipPercentage: BigDecimal,
)

data class SplitBillResult(
    val totalAmount: BigDecimal,
    val tipAmount: BigDecimal,
    val grandTotal: BigDecimal,
    val perPerson: BigDecimal,
    val numberOfPeople: Int,
)

data class InterestResult(
    val principal: BigDecimal,
    val interest: BigDecimal,
    val total: BigDecimal,
    val rate: BigDecimal,
    val time: BigDecimal,
)

data class RatioResult(
    val original: String,
    val simplified: String,
    val decimal: BigDecimal,
)

data class CalculationSuggestion(
    val label: String,
    val expression: String,
    val result: BigDecimal,
)
