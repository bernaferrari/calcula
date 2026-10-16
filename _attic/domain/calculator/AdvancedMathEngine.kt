package com.bernaferrari.calculator.domain.calculator

import com.bernaferrari.calculator.domain.model.AngleUnit
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

/**
 * Advanced mathematical engine supporting calculus, linear algebra, and complex analysis
 */
class AdvancedMathEngine(
    private val precision: Int = 50,
    private val angleUnit: AngleUnit = AngleUnit.RADIANS
) {
    private val mathContext = MathContext(precision, RoundingMode.HALF_UP)
    
    // Cache for expensive computations
    private val computationCache = mutableMapOf<String, Any>()
    
    /**
     * Numerical integration using adaptive Simpson's rule
     */
    fun integrate(
        expression: String,
        variable: String = "x",
        lowerBound: Double,
        upperBound: Double,
        tolerance: Double = 1e-10
    ): Result<BigDecimal> {
        return try {
            val cacheKey = "integrate_${expression}_${variable}_${lowerBound}_${upperBound}_$tolerance"
            val cached = computationCache[cacheKey] as? BigDecimal
            if (cached != null) return Result.success(cached)
            
            val function = compileExpression(expression, variable)
            val result = adaptiveSimpson(function, lowerBound, upperBound, tolerance)
            
            computationCache[cacheKey] = result
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Numerical differentiation using central difference method
     */
    fun differentiate(
        expression: String,
        variable: String = "x",
        atPoint: Double,
        order: Int = 1,
        stepSize: Double = 1e-8
    ): Result<BigDecimal> {
        return try {
            val cacheKey = "diff_${expression}_${variable}_${atPoint}_${order}_$stepSize"
            val cached = computationCache[cacheKey] as? BigDecimal
            if (cached != null) return Result.success(cached)
            
            val function = compileExpression(expression, variable)
            val result = when (order) {
                1 -> centralDifference(function, atPoint, stepSize)
                2 -> secondDerivative(function, atPoint, stepSize)
                else -> higherOrderDerivative(function, atPoint, stepSize, order)
            }
            
            computationCache[cacheKey] = result
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Solve equations using Newton-Raphson method
     */
    fun solveEquation(
        equation: String,
        variable: String = "x",
        initialGuess: Double = 0.0,
        tolerance: Double = 1e-12,
        maxIterations: Int = 100
    ): Result<List<BigDecimal>> {
        return try {
            val solutions = mutableListOf<BigDecimal>()
            
            // Try multiple initial guesses to find different roots
            val guesses = listOf(initialGuess, -1.0, 1.0, 0.5, -0.5, 2.0, -2.0, 10.0, -10.0)
            
            for (guess in guesses) {
                val root = newtonRaphson(equation, variable, guess, tolerance, maxIterations)
                if (root != null && !solutions.any { abs(it.toDouble() - root.toDouble()) < tolerance }) {
                    solutions.add(root)
                }
            }
            
            Result.success(solutions.sortedBy { it.toDouble() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Matrix operations
     */
    inner class Matrix(private val data: Array<Array<BigDecimal>>) {
        val rows = data.size
        val cols = if (rows > 0) data[0].size else 0
        
        operator fun get(i: Int, j: Int) = data[i][j]
        operator fun set(i: Int, j: Int, value: BigDecimal) { data[i][j] = value }
        
        fun multiply(other: Matrix, mathContext: MathContext): Matrix {
            if (this.cols != other.rows) throw IllegalArgumentException("Matrix dimensions incompatible")
            
            val result = Array(this.rows) { Array(other.cols) { BigDecimal.ZERO } }
            
            for (i in 0 until this.rows) {
                for (j in 0 until other.cols) {
                    var sum = BigDecimal.ZERO
                    for (k in 0 until this.cols) {
                        sum = sum.add(this[i, k].multiply(other[k, j]), mathContext)
                    }
                    result[i][j] = sum
                }
            }
            
            return Matrix(result)
        }
        
        fun determinant(mathContext: MathContext): BigDecimal {
            if (rows != cols) throw IllegalArgumentException("Matrix must be square")
            return this@AdvancedMathEngine.calculateDeterminant(data, mathContext)
        }
        
        fun inverse(mathContext: MathContext): Matrix {
            val det = determinant(mathContext)
            if (det.compareTo(BigDecimal.ZERO) == 0) {
                throw IllegalArgumentException("Matrix is singular")
            }
            
            return this@AdvancedMathEngine.gaussJordan(this, mathContext)
        }
        
        fun transpose(): Matrix {
            val result = Array(cols) { Array(rows) { BigDecimal.ZERO } }
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    result[j][i] = data[i][j]
                }
            }
            return Matrix(result)
        }
    }
    
    fun createMatrix(values: Array<Array<Double>>): Matrix {
        val bigDecimalData = values.map { row ->
            row.map { BigDecimal(it.toString(), mathContext) }.toTypedArray()
        }.toTypedArray()
        return Matrix(bigDecimalData)
    }
    
    /**
     * Series expansions and special functions
     */
    fun taylorSeries(
        expression: String,
        variable: String = "x",
        centerPoint: Double = 0.0,
        terms: Int = 10
    ): Result<String> {
        return try {
            val derivatives = mutableListOf<BigDecimal>()
            
            // Calculate derivatives at center point
            for (order in 0 until terms) {
                val derivative = differentiate(expression, variable, centerPoint, order)
                if (derivative.isFailure) return Result.failure(derivative.exceptionOrNull()!!)
                derivatives.add(derivative.getOrThrow())
            }
            
            // Build Taylor series string
            val seriesTerms = mutableListOf<String>()
            for (i in derivatives.indices) {
                val coeff = derivatives[i].divide(factorial(i), mathContext)
                if (coeff.compareTo(BigDecimal.ZERO) != 0) {
                    val term = when (i) {
                        0 -> coeff.toString()
                        1 -> "${coeff}*($variable-$centerPoint)"
                        else -> "${coeff}*($variable-$centerPoint)^$i"
                    }
                    seriesTerms.add(term)
                }
            }
            
            Result.success(seriesTerms.joinToString(" + "))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Statistical functions
     */
    fun statistics(values: List<Double>): StatisticsResult {
        val sorted = values.sorted()
        val n = values.size
        val sum = values.sum()
        val mean = sum / n
        
        val variance = values.map { (it - mean).pow(2) }.sum() / (n - 1)
        val stdDev = sqrt(variance)
        
        val median = when {
            n % 2 == 0 -> (sorted[n / 2 - 1] + sorted[n / 2]) / 2
            else -> sorted[n / 2]
        }
        
        // Mode (most frequent value)
        val frequency = values.groupingBy { it }.eachCount()
        val maxFreq = frequency.values.maxOrNull() ?: 0
        val mode = frequency.filter { it.value == maxFreq }.keys.firstOrNull() ?: 0.0
        
        return StatisticsResult(
            mean = BigDecimal(mean.toString(), mathContext),
            median = BigDecimal(median.toString(), mathContext),
            mode = BigDecimal(mode.toString(), mathContext),
            standardDeviation = BigDecimal(stdDev.toString(), mathContext),
            variance = BigDecimal(variance.toString(), mathContext),
            min = BigDecimal(sorted.first().toString(), mathContext),
            max = BigDecimal(sorted.last().toString(), mathContext),
            sum = BigDecimal(sum.toString(), mathContext),
            count = n
        )
    }
    
    // Private helper methods
    
    private fun compileExpression(expression: String, variable: String): (Double) -> Double {
        return { value ->
            val evaluator = ExpressionEvaluator(angleUnit, precision)
            evaluator.setVariable(variable, BigDecimal(value.toString()))
            evaluator.evaluate(expression).getOrThrow().toDouble()
        }
    }
    
    private fun adaptiveSimpson(
        f: (Double) -> Double,
        a: Double,
        b: Double,
        tolerance: Double,
        depth: Int = 0
    ): BigDecimal {
        if (depth > 20) throw IllegalStateException("Integration did not converge")
        
        val c = (a + b) / 2
        val h = (b - a) / 6
        
        val s1 = h * (f(a) + 4 * f(c) + f(b))
        val s2 = h * (f(a) + 4 * f((a + c) / 2) + 2 * f(c) + 4 * f((c + b) / 2) + f(b)) / 2
        
        return if (abs(s2 - s1) < 15 * tolerance) {
            BigDecimal((s2 + (s2 - s1) / 15).toString(), mathContext)
        } else {
            adaptiveSimpson(f, a, c, tolerance / 2, depth + 1)
                .add(adaptiveSimpson(f, c, b, tolerance / 2, depth + 1), mathContext)
        }
    }
    
    private fun centralDifference(f: (Double) -> Double, x: Double, h: Double): BigDecimal {
        val result = (f(x + h) - f(x - h)) / (2 * h)
        return BigDecimal(result.toString(), mathContext)
    }
    
    private fun secondDerivative(f: (Double) -> Double, x: Double, h: Double): BigDecimal {
        val result = (f(x + h) - 2 * f(x) + f(x - h)) / (h * h)
        return BigDecimal(result.toString(), mathContext)
    }
    
    private fun higherOrderDerivative(
        f: (Double) -> Double,
        x: Double,
        h: Double,
        order: Int
    ): BigDecimal {
        // Use finite difference formula for higher order derivatives
        var result = 0.0
        val binomialCoeffs = binomialCoefficients(order)
        
        for (k in 0..order) {
            val sign = if ((order - k) % 2 == 0) 1 else -1
            result += sign * binomialCoeffs[k] * f(x + (k - order / 2) * h)
        }
        
        result /= h.pow(order)
        return BigDecimal(result.toString(), mathContext)
    }
    
    private fun binomialCoefficients(n: Int): List<Int> {
        val coeffs = mutableListOf<Int>()
        var coeff = 1
        coeffs.add(coeff)
        
        for (k in 1..n) {
            coeff = coeff * (n - k + 1) / k
            coeffs.add(coeff)
        }
        
        return coeffs
    }
    
    private fun newtonRaphson(
        equation: String,
        variable: String,
        initialGuess: Double,
        tolerance: Double,
        maxIterations: Int
    ): BigDecimal? {
        var x = initialGuess
        val f = compileExpression(equation, variable)
        
        for (i in 0 until maxIterations) {
            val fx = f(x)
            if (abs(fx) < tolerance) {
                return BigDecimal(x.toString(), mathContext)
            }
            
            val derivative = centralDifference(f, x, 1e-8).toDouble()
            if (abs(derivative) < 1e-15) break // Avoid division by zero
            
            val newX = x - fx / derivative
            if (abs(newX - x) < tolerance) {
                return BigDecimal(newX.toString(), mathContext)
            }
            x = newX
        }
        
        return null
    }
    
    private fun calculateDeterminant(matrix: Array<Array<BigDecimal>>, mathContext: MathContext): BigDecimal {
        val n = matrix.size
        if (n == 1) return matrix[0][0]
        if (n == 2) {
            return matrix[0][0].multiply(matrix[1][1], mathContext)
                .subtract(matrix[0][1].multiply(matrix[1][0], mathContext), mathContext)
        }
        
        var det = BigDecimal.ZERO
        for (j in 0 until n) {
            val minor = getMinor(matrix, 0, j)
            val cofactor = matrix[0][j].multiply(
                calculateDeterminant(minor, mathContext), mathContext
            )
            
            det = if (j % 2 == 0) {
                det.add(cofactor, mathContext)
            } else {
                det.subtract(cofactor, mathContext)
            }
        }
        
        return det
    }
    
    private fun getMinor(matrix: Array<Array<BigDecimal>>, row: Int, col: Int): Array<Array<BigDecimal>> {
        val n = matrix.size
        val minor = Array(n - 1) { Array(n - 1) { BigDecimal.ZERO } }
        
        var minorRow = 0
        for (i in 0 until n) {
            if (i == row) continue
            var minorCol = 0
            for (j in 0 until n) {
                if (j == col) continue
                minor[minorRow][minorCol] = matrix[i][j]
                minorCol++
            }
            minorRow++
        }
        
        return minor
    }
    
    private fun gaussJordan(matrix: Matrix, mathContext: MathContext): Matrix {
        val n = matrix.rows
        val augmented = Array(n) { Array(2 * n) { BigDecimal.ZERO } }
        
        // Create augmented matrix [A|I]
        for (i in 0 until n) {
            for (j in 0 until n) {
                augmented[i][j] = matrix[i, j]
                augmented[i][j + n] = if (i == j) BigDecimal.ONE else BigDecimal.ZERO
            }
        }
        
        // Perform Gauss-Jordan elimination
        for (i in 0 until n) {
            // Find pivot
            var maxRow = i
            for (k in i + 1 until n) {
                if (augmented[k][i].abs() > augmented[maxRow][i].abs()) {
                    maxRow = k
                }
            }
            
            // Swap rows
            if (maxRow != i) {
                val temp = augmented[i]
                augmented[i] = augmented[maxRow]
                augmented[maxRow] = temp
            }
            
            // Make pivot 1
            val pivot = augmented[i][i]
            for (j in 0 until 2 * n) {
                augmented[i][j] = augmented[i][j].divide(pivot, mathContext)
            }
            
            // Eliminate column
            for (k in 0 until n) {
                if (k != i) {
                    val factor = augmented[k][i]
                    for (j in 0 until 2 * n) {
                        augmented[k][j] = augmented[k][j].subtract(
                            factor.multiply(augmented[i][j], mathContext), mathContext
                        )
                    }
                }
            }
        }
        
        // Extract inverse matrix
        val inverse = Array(n) { Array(n) { BigDecimal.ZERO } }
        for (i in 0 until n) {
            for (j in 0 until n) {
                inverse[i][j] = augmented[i][j + n]
            }
        }
        
        return Matrix(inverse)
    }
    
    private fun factorial(n: Int): BigDecimal {
        if (n < 0) return BigDecimal.ZERO
        if (n <= 1) return BigDecimal.ONE
        
        var result = BigDecimal.ONE
        for (i in 2..n) {
            result = result.multiply(BigDecimal(i.toString()), mathContext)
        }
        
        return result
    }
}

data class StatisticsResult(
    val mean: BigDecimal,
    val median: BigDecimal,
    val mode: BigDecimal,
    val standardDeviation: BigDecimal,
    val variance: BigDecimal,
    val min: BigDecimal,
    val max: BigDecimal,
    val sum: BigDecimal,
    val count: Int
)