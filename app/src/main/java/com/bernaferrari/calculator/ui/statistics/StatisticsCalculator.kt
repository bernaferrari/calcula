package com.bernaferrari.calculator.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsCalculator(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var dataValues by remember { mutableStateOf(listOf<Double>()) }
    var inputValue by remember { mutableStateOf("") }
    
    val statistics = remember(dataValues) {
        if (dataValues.isNotEmpty()) {
            calculateStatistics(dataValues)
        } else null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Statistics Calculator",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Input section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Data Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text("Enter value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    Button(
                        onClick = {
                            inputValue.toDoubleOrNull()?.let { value ->
                                dataValues = dataValues + value
                                inputValue = ""
                            }
                        },
                        enabled = inputValue.toDoubleOrNull() != null
                    ) {
                        Icon(Icons.Default.Add, "Add")
                    }
                }
                
                // Quick data entry buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            dataValues = generateSampleData()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sample Data")
                    }
                    
                    OutlinedButton(
                        onClick = { 
                            dataValues = emptyList()
                            inputValue = ""
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear All")
                    }
                }
            }
        }
        
        // Data list
        if (dataValues.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Data Values (${dataValues.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        IconButton(
                            onClick = { dataValues = dataValues.sorted() }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Sort, "Sort")
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(dataValues) { index, value ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ${formatNumber(value)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                IconButton(
                                    onClick = {
                                        dataValues = dataValues.toMutableList().apply {
                                            removeAt(index)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        "Remove",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Statistics results
        statistics?.let { stats ->
            StatisticsResults(stats)
        }
    }
    }
}

@Composable
private fun StatisticsResults(statistics: StatisticsData) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Statistical Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            // Basic statistics
            StatisticRow("Count", statistics.count.toString())
            StatisticRow("Sum", formatNumber(statistics.sum))
            StatisticRow("Mean (μ)", formatNumber(statistics.mean))
            StatisticRow("Median", formatNumber(statistics.median))
            
            if (statistics.mode != null) {
                StatisticRow("Mode", formatNumber(statistics.mode))
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
            
            // Spread measures
            StatisticRow("Range", formatNumber(statistics.range))
            StatisticRow("Min", formatNumber(statistics.min))
            StatisticRow("Max", formatNumber(statistics.max))
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
            
            // Variance and standard deviation
            StatisticRow("Population Std Dev (σ)", formatNumber(statistics.populationStdDev))
            StatisticRow("Sample Std Dev (s)", formatNumber(statistics.sampleStdDev))
            StatisticRow("Population Variance (σ²)", formatNumber(statistics.populationVariance))
            StatisticRow("Sample Variance (s²)", formatNumber(statistics.sampleVariance))
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
            
            // Quartiles
            StatisticRow("Q1 (25th percentile)", formatNumber(statistics.q1))
            StatisticRow("Q3 (75th percentile)", formatNumber(statistics.q3))
            StatisticRow("IQR", formatNumber(statistics.iqr))
        }
    }
}

@Composable
private fun StatisticRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.End
        )
    }
}

data class StatisticsData(
    val count: Int,
    val sum: Double,
    val mean: Double,
    val median: Double,
    val mode: Double?,
    val min: Double,
    val max: Double,
    val range: Double,
    val populationVariance: Double,
    val sampleVariance: Double,
    val populationStdDev: Double,
    val sampleStdDev: Double,
    val q1: Double,
    val q3: Double,
    val iqr: Double
)

private fun calculateStatistics(values: List<Double>): StatisticsData {
    val sorted = values.sorted()
    val n = values.size
    val sum = values.sum()
    val mean = sum / n
    
    // Median
    val median = when {
        n % 2 == 0 -> (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
        else -> sorted[n / 2]
    }
    
    // Mode (most frequent value, null if no repeats)
    val frequency = values.groupingBy { it }.eachCount()
    val maxFrequency = frequency.values.maxOrNull() ?: 0
    val mode = if (maxFrequency > 1) {
        frequency.filter { it.value == maxFrequency }.keys.firstOrNull()
    } else null
    
    // Min, Max, Range
    val min = sorted.first()
    val max = sorted.last()
    val range = max - min
    
    // Variance and Standard Deviation
    val sumOfSquaredDeviations = values.sumOf { (it - mean).pow(2) }
    
    val populationVariance = sumOfSquaredDeviations / n
    val sampleVariance = if (n > 1) sumOfSquaredDeviations / (n - 1) else 0.0
    
    val populationStdDev = sqrt(populationVariance)
    val sampleStdDev = sqrt(sampleVariance)
    
    // Quartiles
    val q1 = percentile(sorted, 25.0)
    val q3 = percentile(sorted, 75.0)
    val iqr = q3 - q1
    
    return StatisticsData(
        count = n,
        sum = sum,
        mean = mean,
        median = median,
        mode = mode,
        min = min,
        max = max,
        range = range,
        populationVariance = populationVariance,
        sampleVariance = sampleVariance,
        populationStdDev = populationStdDev,
        sampleStdDev = sampleStdDev,
        q1 = q1,
        q3 = q3,
        iqr = iqr
    )
}

private fun percentile(sortedValues: List<Double>, percentile: Double): Double {
    val n = sortedValues.size
    val index = (percentile / 100.0) * (n - 1)
    
    return when {
        index == index.toInt().toDouble() -> sortedValues[index.toInt()]
        else -> {
            val lower = sortedValues[floor(index).toInt()]
            val upper = sortedValues[ceil(index).toInt()]
            lower + (index - floor(index)) * (upper - lower)
        }
    }
}

private fun generateSampleData(): List<Double> {
    return listOf(
        85.0, 92.0, 78.0, 96.0, 88.0, 
        75.0, 91.0, 83.0, 89.0, 94.0,
        87.0, 82.0, 90.0, 76.0, 93.0
    )
}

private fun formatNumber(value: Double): String {
    return when {
        value == value.toInt().toDouble() -> value.toInt().toString()
        abs(value) < 0.001 || abs(value) > 1000000 -> String.format("%.3e", value)
        else -> {
            val formatted = String.format("%.6f", value)
            formatted.trimEnd('0').trimEnd('.')
        }
    }
}

// Additional statistical functions for advanced use
object StatisticsUtils {
    
    fun correlation(x: List<Double>, y: List<Double>): Double {
        if (x.size != y.size || x.isEmpty()) return 0.0
        
        val n = x.size
        val meanX = x.average()
        val meanY = y.average()
        
        val numerator = x.zip(y) { xi, yi -> (xi - meanX) * (yi - meanY) }.sum()
        val denomX = sqrt(x.sumOf { (it - meanX).pow(2) })
        val denomY = sqrt(y.sumOf { (it - meanY).pow(2) })
        
        return if (denomX == 0.0 || denomY == 0.0) 0.0 else numerator / (denomX * denomY)
    }
    
    fun linearRegression(x: List<Double>, y: List<Double>): Pair<Double, Double>? {
        if (x.size != y.size || x.size < 2) return null
        
        val n = x.size
        val sumX = x.sum()
        val sumY = y.sum()
        val sumXY = x.zip(y) { xi, yi -> xi * yi }.sum()
        val sumXX = x.sumOf { it * it }
        
        val slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX)
        val intercept = (sumY - slope * sumX) / n
        
        return Pair(slope, intercept)
    }
    
    fun normalDistribution(x: Double, mean: Double = 0.0, stdDev: Double = 1.0): Double {
        val exponent = -0.5 * ((x - mean) / stdDev).pow(2)
        return exp(exponent) / (stdDev * sqrt(2 * PI))
    }
    
    fun standardScore(x: Double, mean: Double, stdDev: Double): Double {
        return (x - mean) / stdDev
    }
}