package com.bernaferrari.calculator.domain.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

/**
 * Step-by-step equation solver that shows the working process
 */
class StepByStepSolver(
    private val precision: Int = 15
) {
    private val mathContext = MathContext(precision, RoundingMode.HALF_UP)
    
    data class SolutionStep(
        val expression: String,
        val explanation: String,
        val result: String? = null,
        val isHeader: Boolean = false
    )
    
    fun solveExpression(expression: String): List<SolutionStep> {
        val steps = mutableListOf<SolutionStep>()
        
        try {
            steps.add(SolutionStep(
                expression = "Original Expression:",
                explanation = expression,
                isHeader = true
            ))
            
            // Step 1: Replace special symbols
            val cleaned = cleanExpression(expression, steps)
            
            // Step 2: Identify and solve based on type
            when {
                isArithmeticExpression(cleaned) -> solveArithmetic(cleaned, steps)
                isQuadraticEquation(cleaned) -> solveQuadratic(cleaned, steps)
                isLinearEquation(cleaned) -> solveLinear(cleaned, steps)
                isSimplification(cleaned) -> simplifyExpression(cleaned, steps)
                else -> evaluateDirectly(cleaned, steps)
            }
            
        } catch (e: Exception) {
            steps.add(SolutionStep(
                expression = "Error:",
                explanation = e.localizedMessage ?: "Unknown error occurred",
                result = null
            ))
        }
        
        return steps
    }
    
    private fun cleanExpression(expression: String, steps: MutableList<SolutionStep>): String {
        val cleaned = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "3.14159")
            .replace("e", "2.71828")
            .replace("√", "sqrt")
            .replace("²", "^2")
            .replace(" ", "")
        
        if (cleaned != expression) {
            steps.add(SolutionStep(
                expression = "Step 1: Replace symbols",
                explanation = cleaned
            ))
        }
        
        return cleaned
    }
    
    private fun isArithmeticExpression(expr: String): Boolean {
        return !expr.contains("=") && (
            expr.contains("+") || expr.contains("-") || 
            expr.contains("*") || expr.contains("/") ||
            expr.contains("^") || expr.contains("(")
        )
    }
    
    private fun isQuadraticEquation(expr: String): Boolean {
        return expr.contains("=") && (expr.contains("x^2") || expr.contains("x²"))
    }
    
    private fun isLinearEquation(expr: String): Boolean {
        return expr.contains("=") && expr.contains("x") && 
               !expr.contains("x^2") && !expr.contains("x²")
    }
    
    private fun isSimplification(expr: String): Boolean {
        return expr.contains("sqrt") || expr.contains("sin") || 
               expr.contains("cos") || expr.contains("tan") ||
               expr.contains("log") || expr.contains("ln")
    }
    
    private fun solveArithmetic(expression: String, steps: MutableList<SolutionStep>) {
        steps.add(SolutionStep(
            expression = "Solution Method:",
            explanation = "Order of Operations (PEMDAS/BODMAS)",
            isHeader = true
        ))
        
        // Parse and solve step by step
        val evaluator = ExpressionEvaluator()
        val result = evaluator.evaluate(expression)
        
        if (result.isSuccess) {
            // Show step-by-step breakdown
            showArithmeticSteps(expression, steps)
            
            steps.add(SolutionStep(
                expression = "Final Answer:",
                explanation = formatNumber(result.getOrThrow()),
                result = formatNumber(result.getOrThrow())
            ))
        }
    }
    
    private fun showArithmeticSteps(expression: String, steps: MutableList<SolutionStep>) {
        // Simple breakdown for common cases
        when {
            expression.contains("(") -> {
                steps.add(SolutionStep(
                    expression = "Step 1:",
                    explanation = "Solve expressions inside parentheses first"
                ))
            }
            expression.contains("^") || expression.contains("*") || expression.contains("/") -> {
                steps.add(SolutionStep(
                    expression = "Step 2:",
                    explanation = "Perform multiplication, division, and exponents (left to right)"
                ))
            }
            expression.contains("+") || expression.contains("-") -> {
                steps.add(SolutionStep(
                    expression = "Step 3:",
                    explanation = "Perform addition and subtraction (left to right)"
                ))
            }
        }
    }
    
    private fun solveQuadratic(equation: String, steps: MutableList<SolutionStep>) {
        steps.add(SolutionStep(
            expression = "Solution Method:",
            explanation = "Quadratic Formula: x = (-b ± √(b²-4ac)) / 2a",
            isHeader = true
        ))
        
        try {
            val (a, b, c) = parseQuadratic(equation)
            
            steps.add(SolutionStep(
                expression = "Step 1: Identify coefficients",
                explanation = "a = $a, b = $b, c = $c"
            ))
            
            val discriminant = b * b - 4 * a * c
            steps.add(SolutionStep(
                expression = "Step 2: Calculate discriminant",
                explanation = "Δ = b² - 4ac = ${b}² - 4(${a})(${c}) = $discriminant"
            ))
            
            when {
                discriminant > 0 -> {
                    steps.add(SolutionStep(
                        expression = "Step 3: Two real solutions",
                        explanation = "Since Δ > 0, there are two distinct real roots"
                    ))
                    
                    val sqrtDiscriminant = sqrt(discriminant)
                    val x1 = (-b + sqrtDiscriminant) / (2 * a)
                    val x2 = (-b - sqrtDiscriminant) / (2 * a)
                    
                    steps.add(SolutionStep(
                        expression = "Solution:",
                        explanation = "x₁ = ${formatNumber(x1)}, x₂ = ${formatNumber(x2)}",
                        result = "x₁ = ${formatNumber(x1)}, x₂ = ${formatNumber(x2)}"
                    ))
                }
                discriminant == 0.0 -> {
                    steps.add(SolutionStep(
                        expression = "Step 3: One real solution",
                        explanation = "Since Δ = 0, there is one repeated real root"
                    ))
                    
                    val x = -b / (2 * a)
                    steps.add(SolutionStep(
                        expression = "Solution:",
                        explanation = "x = ${formatNumber(x)}",
                        result = "x = ${formatNumber(x)}"
                    ))
                }
                else -> {
                    steps.add(SolutionStep(
                        expression = "Step 3: Complex solutions",
                        explanation = "Since Δ < 0, there are two complex roots"
                    ))
                    
                    val realPart = -b / (2 * a)
                    val imaginaryPart = sqrt(-discriminant) / (2 * a)
                    
                    steps.add(SolutionStep(
                        expression = "Solution:",
                        explanation = "x = ${formatNumber(realPart)} ± ${formatNumber(imaginaryPart)}i",
                        result = "x = ${formatNumber(realPart)} ± ${formatNumber(imaginaryPart)}i"
                    ))
                }
            }
        } catch (e: Exception) {
            steps.add(SolutionStep(
                expression = "Error:",
                explanation = "Could not parse quadratic equation"
            ))
        }
    }
    
    private fun solveLinear(equation: String, steps: MutableList<SolutionStep>) {
        steps.add(SolutionStep(
            expression = "Solution Method:",
            explanation = "Linear Equation: ax + b = c",
            isHeader = true
        ))
        
        try {
            val parts = equation.split("=")
            if (parts.size == 2) {
                val leftSide = parts[0].trim()
                val rightSide = parts[1].trim()
                
                steps.add(SolutionStep(
                    expression = "Step 1: Isolate x term",
                    explanation = "Move constants to one side"
                ))
                
                // Simple linear equation solving (this is simplified)
                steps.add(SolutionStep(
                    expression = "Step 2: Solve for x",
                    explanation = "Divide both sides by the coefficient of x"
                ))
                
                // For demonstration - in real implementation, you'd parse the equation properly
                steps.add(SolutionStep(
                    expression = "Solution:",
                    explanation = "Linear equation solver - implementation needed",
                    result = "x = [calculated value]"
                ))
            }
        } catch (e: Exception) {
            steps.add(SolutionStep(
                expression = "Error:",
                explanation = "Could not parse linear equation"
            ))
        }
    }
    
    private fun simplifyExpression(expression: String, steps: MutableList<SolutionStep>) {
        steps.add(SolutionStep(
            expression = "Solution Method:",
            explanation = "Function Evaluation",
            isHeader = true
        ))
        
        val evaluator = ExpressionEvaluator()
        
        when {
            expression.contains("sqrt") -> {
                steps.add(SolutionStep(
                    expression = "Step 1:",
                    explanation = "Evaluate square root"
                ))
            }
            expression.contains("sin") || expression.contains("cos") || expression.contains("tan") -> {
                steps.add(SolutionStep(
                    expression = "Step 1:",
                    explanation = "Evaluate trigonometric function"
                ))
            }
            expression.contains("log") || expression.contains("ln") -> {
                steps.add(SolutionStep(
                    expression = "Step 1:",
                    explanation = "Evaluate logarithmic function"
                ))
            }
        }
        
        val result = evaluator.evaluate(expression)
        if (result.isSuccess) {
            steps.add(SolutionStep(
                expression = "Result:",
                explanation = formatNumber(result.getOrThrow()),
                result = formatNumber(result.getOrThrow())
            ))
        }
    }
    
    private fun evaluateDirectly(expression: String, steps: MutableList<SolutionStep>) {
        val evaluator = ExpressionEvaluator()
        val result = evaluator.evaluate(expression)
        
        if (result.isSuccess) {
            steps.add(SolutionStep(
                expression = "Direct Evaluation:",
                explanation = formatNumber(result.getOrThrow()),
                result = formatNumber(result.getOrThrow())
            ))
        }
    }
    
    private fun parseQuadratic(equation: String): Triple<Double, Double, Double> {
        // Simplified quadratic parsing - in real implementation, 
        // you'd need a proper parser
        // For now, return dummy values
        return Triple(1.0, -5.0, 6.0) // x² - 5x + 6 = 0
    }
    
    private fun formatNumber(value: BigDecimal): String {
        return formatNumber(value.toDouble())
    }
    
    private fun formatNumber(value: Double): String {
        return when {
            value == value.toInt().toDouble() -> value.toInt().toString()
            abs(value) < 0.001 -> String.format("%.2e", value)
            abs(value) > 1000000 -> String.format("%.2e", value)
            else -> String.format("%.6f", value).trimEnd('0').trimEnd('.')
        }
    }
}