package com.bernaferrari.calculator.domain.calculator

import com.bernaferrari.calculator.domain.model.AngleUnit
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

/**
 * High-precision expression evaluator using BigDecimal for exact arithmetic.
 * Avoids binary floating-point precision issues common in calculators.
 */
class ExpressionEvaluator(
    private val angleUnit: AngleUnit = AngleUnit.RADIANS,
    private val precision: Int = 34 // 34 decimal places for high precision
) {
    
    private val mathContext = MathContext(precision, RoundingMode.HALF_UP)
    private val variables = mutableMapOf<String, BigDecimal>()
    
    // High-precision constants
    private val PI_PRECISION = BigDecimal("3.1415926535897932384626433832795")
    private val E_PRECISION = BigDecimal("2.7182818284590452353602874713527")
    
    fun evaluate(expression: String): Result<BigDecimal> {
        return try {
            val sanitized = sanitizeExpression(expression)
            val result = evaluateExpression(sanitized)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun setVariable(name: String, value: BigDecimal) {
        variables[name] = value
    }
    
    private fun sanitizeExpression(expression: String): String {
        return expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "π") // Keep as token for high-precision replacement
            .replace("e", "e") // Keep as token for high-precision replacement
            .replace("ans", variables["ans"]?.toString() ?: "0")
            .replace("√", "sqrt")
            .replace("²", "^2")
            .replace(" ", "")
    }
    
    private fun evaluateExpression(expression: String): BigDecimal {
        val tokenizer = ExpressionTokenizer(expression)
        return parseExpression(tokenizer)
    }
    
    private fun parseExpression(tokenizer: ExpressionTokenizer): BigDecimal {
        var result = parseTerm(tokenizer)
        
        while (tokenizer.hasNext()) {
            when (val op = tokenizer.peek()) {
                "+" -> {
                    tokenizer.next()
                    result = result.add(parseTerm(tokenizer), mathContext)
                }
                "-" -> {
                    tokenizer.next()
                    result = result.subtract(parseTerm(tokenizer), mathContext)
                }
                else -> break
            }
        }
        
        return result
    }
    
    private fun parseTerm(tokenizer: ExpressionTokenizer): BigDecimal {
        var result = parsePower(tokenizer)
        
        while (tokenizer.hasNext()) {
            when (val op = tokenizer.peek()) {
                "*" -> {
                    tokenizer.next()
                    result = result.multiply(parsePower(tokenizer), mathContext)
                }
                "/" -> {
                    tokenizer.next()
                    val divisor = parsePower(tokenizer)
                    if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                        throw ArithmeticException("Division by zero")
                    }
                    result = result.divide(divisor, mathContext)
                }
                "%" -> {
                    tokenizer.next()
                    val divisor = parsePower(tokenizer)
                    if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                        throw ArithmeticException("Division by zero in modulo")
                    }
                    result = result.remainder(divisor, mathContext)
                }
                else -> break
            }
        }
        
        return result
    }
    
    private fun parsePower(tokenizer: ExpressionTokenizer): BigDecimal {
        var result = parseFactor(tokenizer)
        
        while (tokenizer.hasNext() && tokenizer.peek() == "^") {
            tokenizer.next()
            val exponent = parseFactor(tokenizer)
            result = pow(result, exponent)
        }
        
        return result
    }
    
    private fun parseFactor(tokenizer: ExpressionTokenizer): BigDecimal {
        if (!tokenizer.hasNext()) throw IllegalArgumentException("Unexpected end of expression")
        
        val token = tokenizer.next()
        
        return when {
            token == "(" -> {
                val result = parseExpression(tokenizer)
                if (!tokenizer.hasNext() || tokenizer.next() != ")") {
                    throw IllegalArgumentException("Missing closing parenthesis")
                }
                result
            }
            
            token == "-" -> parseFactor(tokenizer).negate()
            token == "+" -> parseFactor(tokenizer)
            
            token.matches(Regex("\\d+(\\.\\d+)?")) -> BigDecimal(token, mathContext)
            
            // High-precision constants
            token == "π" -> PI_PRECISION
            token == "e" -> E_PRECISION
            
            // Scientific functions (convert to Double for calculation, then back to BigDecimal)
            token == "sin" -> {
                val value = parseFactor(tokenizer)
                val doubleVal = value.toDouble()
                val radians = if (angleUnit == AngleUnit.DEGREES) toRadians(doubleVal) else doubleVal
                BigDecimal(sin(radians).toString(), mathContext)
            }
            token == "cos" -> {
                val value = parseFactor(tokenizer)
                val doubleVal = value.toDouble()
                val radians = if (angleUnit == AngleUnit.DEGREES) toRadians(doubleVal) else doubleVal
                BigDecimal(cos(radians).toString(), mathContext)
            }
            token == "tan" -> {
                val value = parseFactor(tokenizer)
                val doubleVal = value.toDouble()
                val radians = if (angleUnit == AngleUnit.DEGREES) toRadians(doubleVal) else doubleVal
                BigDecimal(tan(radians).toString(), mathContext)
            }
            token == "asin" -> {
                val value = parseFactor(tokenizer)
                val result = asin(value.toDouble())
                val finalResult = if (angleUnit == AngleUnit.DEGREES) toDegrees(result) else result
                BigDecimal(finalResult.toString(), mathContext)
            }
            token == "acos" -> {
                val value = parseFactor(tokenizer)
                val result = acos(value.toDouble())
                val finalResult = if (angleUnit == AngleUnit.DEGREES) toDegrees(result) else result
                BigDecimal(finalResult.toString(), mathContext)
            }
            token == "atan" -> {
                val value = parseFactor(tokenizer)
                val result = atan(value.toDouble())
                val finalResult = if (angleUnit == AngleUnit.DEGREES) toDegrees(result) else result
                BigDecimal(finalResult.toString(), mathContext)
            }
            token == "log" -> {
                val value = parseFactor(tokenizer)
                BigDecimal(log10(value.toDouble()).toString(), mathContext)
            }
            token == "ln" -> {
                val value = parseFactor(tokenizer)
                BigDecimal(ln(value.toDouble()).toString(), mathContext)
            }
            token == "sqrt" -> {
                val value = parseFactor(tokenizer)
                sqrt(value)
            }
            token == "!" -> {
                val value = parseFactor(tokenizer)
                factorial(value.toInt())
            }
            
            // Variables
            variables.containsKey(token) -> variables[token]!!
            
            else -> throw IllegalArgumentException("Unknown token: $token")
        }
    }
    
    /**
     * High-precision square root using Newton's method
     */
    private fun sqrt(value: BigDecimal): BigDecimal {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw IllegalArgumentException("Square root of negative number")
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO
        }
        
        var x = value
        var prev: BigDecimal
        val two = BigDecimal.valueOf(2)
        
        do {
            prev = x
            x = x.add(value.divide(x, mathContext)).divide(two, mathContext)
        } while (x.subtract(prev).abs().compareTo(BigDecimal.valueOf(0.0000000001)) > 0)
        
        return x
    }
    
    /**
     * High-precision power function for integer exponents
     */
    private fun pow(base: BigDecimal, exponent: BigDecimal): BigDecimal {
        // For integer exponents, use exact arithmetic
        if (exponent.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            val exp = exponent.toInt()
            return when {
                exp == 0 -> BigDecimal.ONE
                exp > 0 -> base.pow(exp, mathContext)
                else -> BigDecimal.ONE.divide(base.pow(-exp, mathContext), mathContext)
            }
        }
        
        // For non-integer exponents, fall back to Double calculation
        val result = base.toDouble().pow(exponent.toDouble())
        return BigDecimal(result.toString(), mathContext)
    }
    
    /**
     * High-precision factorial
     */
    private fun factorial(n: Int): BigDecimal {
        if (n < 0) throw IllegalArgumentException("Factorial of negative number")
        if (n > 1000) throw IllegalArgumentException("Factorial too large")
        
        var result = BigInteger.ONE
        for (i in 2..n) {
            result = result.multiply(BigInteger.valueOf(i.toLong()))
        }
        return BigDecimal(result, mathContext)
    }
    
    private fun toRadians(degrees: Double): Double = degrees * PI / 180.0
    private fun toDegrees(radians: Double): Double = radians * 180.0 / PI
}

class ExpressionTokenizer(private val expression: String) {
    private var position = 0
    private val tokens = mutableListOf<String>()
    
    init {
        tokenize()
    }
    
    private fun tokenize() {
        var i = 0
        while (i < expression.length) {
            when {
                expression[i].isWhitespace() -> i++
                expression[i].isDigit() || expression[i] == '.' -> {
                    val start = i
                    while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                        i++
                    }
                    tokens.add(expression.substring(start, i))
                }
                expression[i].isLetter() || expression[i] in "πe" -> {
                    val start = i
                    while (i < expression.length && (expression[i].isLetter() || expression[i] in "πe")) {
                        i++
                    }
                    tokens.add(expression.substring(start, i))
                }
                expression[i] in "+-*/()^!%" -> {
                    tokens.add(expression[i].toString())
                    i++
                }
                else -> i++
            }
        }
    }
    
    fun hasNext(): Boolean = position < tokens.size
    
    fun next(): String = tokens[position++]
    
    fun peek(): String? = if (hasNext()) tokens[position] else null
}