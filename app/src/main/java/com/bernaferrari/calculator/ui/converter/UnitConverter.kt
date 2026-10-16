package com.bernaferrari.calculator.ui.converter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

enum class UnitCategory(val displayName: String) {
    LENGTH("Length"),
    WEIGHT("Weight"),
    TEMPERATURE("Temperature"),
    AREA("Area"),
    VOLUME("Volume"),
    TIME("Time"),
    SPEED("Speed"),
    ENERGY("Energy"),
    POWER("Power"),
    PRESSURE("Pressure")
}

data class Unit(
    val name: String,
    val symbol: String,
    val baseMultiplier: BigDecimal, // Factor to convert to base unit
    val category: UnitCategory,
    val isTemperature: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    onNavigateBack: () -> kotlin.Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    var inputValue by remember { mutableStateOf("1") }
    var fromUnit by remember { mutableStateOf<Unit?>(null) }
    var toUnit by remember { mutableStateOf<Unit?>(null) }
    
    val availableUnits = remember(selectedCategory) {
        getUnitsForCategory(selectedCategory)
    }
    
    // Set default units when category changes
    LaunchedEffect(selectedCategory) {
        val units = getUnitsForCategory(selectedCategory)
        if (units.isNotEmpty()) {
            fromUnit = units[0]
            toUnit = if (units.size > 1) units[1] else units[0]
        }
    }
    
    val convertedValue = remember(inputValue, fromUnit, toUnit) {
        if (fromUnit != null && toUnit != null && inputValue.isNotEmpty()) {
            convertUnits(inputValue, fromUnit!!, toUnit!!)
        } else "0"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Unit Converter",
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
        // Category selector
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Unit Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(UnitCategory.values()) { category ->
                        FilterChip(
                            onClick = { selectedCategory = category },
                            label = { Text(category.displayName) },
                            selected = category == selectedCategory
                        )
                    }
                }
            }
        }
        
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
                    text = "Convert From",
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    fromUnit?.let { unit ->
                        OutlinedButton(
                            onClick = { /* TODO: Show searchable unit picker dialog */ }
                        ) { Text("${unit.symbol}") }
                    }
                }
                
                // Unit selector for FROM
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableUnits) { unit ->
                        FilterChip(
                            onClick = { fromUnit = unit },
                            label = { Text("${unit.name} (${unit.symbol})") },
                            selected = unit == fromUnit
                        )
                    }
                }
            }
        }
        
        // Swap button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    val temp = fromUnit
                    fromUnit = toUnit
                    toUnit = temp
                }
            ) {
                Icon(Icons.Default.SwapVert, "Swap units")
            }
        }
        
        // Output section
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
                    text = "Convert To",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = convertedValue,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    
                    toUnit?.let { unit ->
                        OutlinedButton(
                            onClick = { /* TODO: Show searchable unit picker dialog */ }
                        ) { Text("${unit.symbol}") }
                    }
                }
                
                // Unit selector for TO
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableUnits) { unit ->
                        FilterChip(
                            onClick = { toUnit = unit },
                            label = { Text("${unit.name} (${unit.symbol})") },
                            selected = unit == toUnit,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedContainerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
        
        // Quick conversions
        if (fromUnit != null && toUnit != null) {
            QuickConversions(
                fromUnit = fromUnit!!,
                toUnit = toUnit!!,
                onValueSelect = { value ->
                    inputValue = value.toString()
                }
            )
        }
    }
    }
}

@Composable
private fun QuickConversions(
    fromUnit: Unit,
    toUnit: Unit,
    onValueSelect: (Int) -> kotlin.Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Conversions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val quickValues = listOf(1, 5, 10, 25, 50, 100, 500, 1000)
            
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(quickValues) { value ->
                    val converted = convertUnits(value.toString(), fromUnit, toUnit)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onValueSelect(value) }
                        ) {
                            Text("$value ${fromUnit.symbol}")
                        }
                        
                        Text(
                            text = "= $converted ${toUnit.symbol}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

private fun convertUnits(inputValue: String, fromUnit: Unit, toUnit: Unit): String {
    return try {
        val value = BigDecimal(inputValue)
        val mathContext = MathContext(10, RoundingMode.HALF_UP)
        
        val result = when {
            fromUnit.category != toUnit.category -> BigDecimal.ZERO // Can't convert between different categories
            fromUnit.isTemperature && toUnit.isTemperature -> {
                convertTemperature(value, fromUnit, toUnit, mathContext)
            }
            else -> {
                // Convert to base unit, then to target unit
                val baseValue = value.multiply(fromUnit.baseMultiplier, mathContext)
                baseValue.divide(toUnit.baseMultiplier, mathContext)
            }
        }
        
        formatResult(result)
    } catch (e: Exception) {
        "Error"
    }
}

private fun convertTemperature(
    value: BigDecimal,
    fromUnit: Unit,
    toUnit: Unit,
    mathContext: MathContext
): BigDecimal {
    // Convert to Celsius first, then to target
    val celsius = when (fromUnit.name) {
        "Celsius" -> value
        "Fahrenheit" -> (value - BigDecimal("32")).multiply(BigDecimal("5")).divide(BigDecimal("9"), mathContext)
        "Kelvin" -> value - BigDecimal("273.15")
        "Rankine" -> (value - BigDecimal("459.67")).multiply(BigDecimal("5")).divide(BigDecimal("9"), mathContext)
        else -> value
    }
    
    return when (toUnit.name) {
        "Celsius" -> celsius
        "Fahrenheit" -> celsius.multiply(BigDecimal("9")).divide(BigDecimal("5"), mathContext) + BigDecimal("32")
        "Kelvin" -> celsius + BigDecimal("273.15")
        "Rankine" -> celsius.multiply(BigDecimal("9")).divide(BigDecimal("5"), mathContext) + BigDecimal("459.67")
        else -> celsius
    }
}

private fun formatResult(value: BigDecimal): String {
    val stripped = value.stripTrailingZeros()
    return when {
        stripped.scale() <= 0 -> stripped.toPlainString()
        stripped.abs() < BigDecimal("0.001") -> String.format("%.3e", value.toDouble())
        stripped.abs() > BigDecimal("1000000") -> String.format("%.3e", value.toDouble())
        else -> stripped.toPlainString()
    }
}

private fun getUnitsForCategory(category: UnitCategory): List<Unit> {
    return when (category) {
        UnitCategory.LENGTH -> listOf(
            Unit("Millimeter", "mm", BigDecimal("0.001"), UnitCategory.LENGTH),
            Unit("Centimeter", "cm", BigDecimal("0.01"), UnitCategory.LENGTH),
            Unit("Meter", "m", BigDecimal("1"), UnitCategory.LENGTH),
            Unit("Kilometer", "km", BigDecimal("1000"), UnitCategory.LENGTH),
            Unit("Inch", "in", BigDecimal("0.0254"), UnitCategory.LENGTH),
            Unit("Foot", "ft", BigDecimal("0.3048"), UnitCategory.LENGTH),
            Unit("Yard", "yd", BigDecimal("0.9144"), UnitCategory.LENGTH),
            Unit("Mile", "mi", BigDecimal("1609.344"), UnitCategory.LENGTH)
        )
        
        UnitCategory.WEIGHT -> listOf(
            Unit("Milligram", "mg", BigDecimal("0.000001"), UnitCategory.WEIGHT),
            Unit("Gram", "g", BigDecimal("0.001"), UnitCategory.WEIGHT),
            Unit("Kilogram", "kg", BigDecimal("1"), UnitCategory.WEIGHT),
            Unit("Pound", "lb", BigDecimal("0.453592"), UnitCategory.WEIGHT),
            Unit("Ounce", "oz", BigDecimal("0.0283495"), UnitCategory.WEIGHT),
            Unit("Ton", "t", BigDecimal("1000"), UnitCategory.WEIGHT)
        )
        
        UnitCategory.TEMPERATURE -> listOf(
            Unit("Celsius", "°C", BigDecimal("1"), UnitCategory.TEMPERATURE, true),
            Unit("Fahrenheit", "°F", BigDecimal("1"), UnitCategory.TEMPERATURE, true),
            Unit("Kelvin", "K", BigDecimal("1"), UnitCategory.TEMPERATURE, true),
            Unit("Rankine", "°R", BigDecimal("1"), UnitCategory.TEMPERATURE, true)
        )
        
        UnitCategory.AREA -> listOf(
            Unit("Square Millimeter", "mm²", BigDecimal("0.000001"), UnitCategory.AREA),
            Unit("Square Centimeter", "cm²", BigDecimal("0.0001"), UnitCategory.AREA),
            Unit("Square Meter", "m²", BigDecimal("1"), UnitCategory.AREA),
            Unit("Square Kilometer", "km²", BigDecimal("1000000"), UnitCategory.AREA),
            Unit("Square Inch", "in²", BigDecimal("0.00064516"), UnitCategory.AREA),
            Unit("Square Foot", "ft²", BigDecimal("0.092903"), UnitCategory.AREA),
            Unit("Acre", "acre", BigDecimal("4046.86"), UnitCategory.AREA)
        )
        
        UnitCategory.VOLUME -> listOf(
            Unit("Milliliter", "ml", BigDecimal("0.001"), UnitCategory.VOLUME),
            Unit("Liter", "L", BigDecimal("1"), UnitCategory.VOLUME),
            Unit("Cubic Meter", "m³", BigDecimal("1000"), UnitCategory.VOLUME),
            Unit("Fluid Ounce", "fl oz", BigDecimal("0.0295735"), UnitCategory.VOLUME),
            Unit("Cup", "cup", BigDecimal("0.236588"), UnitCategory.VOLUME),
            Unit("Pint", "pt", BigDecimal("0.473176"), UnitCategory.VOLUME),
            Unit("Quart", "qt", BigDecimal("0.946353"), UnitCategory.VOLUME),
            Unit("Gallon", "gal", BigDecimal("3.78541"), UnitCategory.VOLUME)
        )
        
        UnitCategory.TIME -> listOf(
            Unit("Millisecond", "ms", BigDecimal("0.001"), UnitCategory.TIME),
            Unit("Second", "s", BigDecimal("1"), UnitCategory.TIME),
            Unit("Minute", "min", BigDecimal("60"), UnitCategory.TIME),
            Unit("Hour", "hr", BigDecimal("3600"), UnitCategory.TIME),
            Unit("Day", "day", BigDecimal("86400"), UnitCategory.TIME),
            Unit("Week", "wk", BigDecimal("604800"), UnitCategory.TIME),
            Unit("Year", "yr", BigDecimal("31536000"), UnitCategory.TIME)
        )
        
        UnitCategory.SPEED -> listOf(
            Unit("Meter per Second", "m/s", BigDecimal("1"), UnitCategory.SPEED),
            Unit("Kilometer per Hour", "km/h", BigDecimal("0.277778"), UnitCategory.SPEED),
            Unit("Mile per Hour", "mph", BigDecimal("0.44704"), UnitCategory.SPEED),
            Unit("Foot per Second", "ft/s", BigDecimal("0.3048"), UnitCategory.SPEED),
            Unit("Knot", "kn", BigDecimal("0.514444"), UnitCategory.SPEED)
        )
        
        UnitCategory.ENERGY -> listOf(
            Unit("Joule", "J", BigDecimal("1"), UnitCategory.ENERGY),
            Unit("Kilojoule", "kJ", BigDecimal("1000"), UnitCategory.ENERGY),
            Unit("Calorie", "cal", BigDecimal("4.184"), UnitCategory.ENERGY),
            Unit("Kilocalorie", "kcal", BigDecimal("4184"), UnitCategory.ENERGY),
            Unit("Watt Hour", "Wh", BigDecimal("3600"), UnitCategory.ENERGY),
            Unit("Kilowatt Hour", "kWh", BigDecimal("3600000"), UnitCategory.ENERGY)
        )
        
        UnitCategory.POWER -> listOf(
            Unit("Watt", "W", BigDecimal("1"), UnitCategory.POWER),
            Unit("Kilowatt", "kW", BigDecimal("1000"), UnitCategory.POWER),
            Unit("Horsepower", "hp", BigDecimal("745.7"), UnitCategory.POWER),
            Unit("BTU per Hour", "BTU/h", BigDecimal("0.293071"), UnitCategory.POWER)
        )
        
        UnitCategory.PRESSURE -> listOf(
            Unit("Pascal", "Pa", BigDecimal("1"), UnitCategory.PRESSURE),
            Unit("Kilopascal", "kPa", BigDecimal("1000"), UnitCategory.PRESSURE),
            Unit("Bar", "bar", BigDecimal("100000"), UnitCategory.PRESSURE),
            Unit("Atmosphere", "atm", BigDecimal("101325"), UnitCategory.PRESSURE),
            Unit("PSI", "psi", BigDecimal("6894.76"), UnitCategory.PRESSURE),
            Unit("Torr", "Torr", BigDecimal("133.322"), UnitCategory.PRESSURE)
        )
    }
}