package com.bernaferrari.calculator.ui.programmer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NumberBase(val displayName: String, val radix: Int) {
    BINARY("BIN", 2),
    OCTAL("OCT", 8),
    DECIMAL("DEC", 10),
    HEXADECIMAL("HEX", 16)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgrammerModeDisplay(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentValue by remember { mutableStateOf(0L) }
    var currentBase by remember { mutableStateOf(NumberBase.DECIMAL) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Programmer Mode",
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
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
        // Base selector
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(NumberBase.values()) { base ->
                BaseChip(
                    base = base,
                    isSelected = base == currentBase,
                    onClick = { currentBase = base }
                )
            }
        }
        
        // Value displays for all bases
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberBase.values().forEach { base ->
                    BaseValueRow(
                        base = base,
                        value = currentValue,
                        isCurrent = base == currentBase
                    )
                }
            }
        }
        
        // Bit representation for binary
        if (currentBase == NumberBase.BINARY || currentValue != 0L) {
            BitRepresentation(
                value = currentValue,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
    }
}

@Composable
private fun BaseChip(
    base: NumberBase,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    )
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = base.displayName,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun BaseValueRow(
    base: NumberBase,
    value: Long,
    isCurrent: Boolean
) {
    val displayValue = when (base) {
        NumberBase.BINARY -> if (value >= 0) value.toString(2) else {
            // Handle negative numbers in two's complement
            (value and 0xFFFFFFFFL).toString(2).padStart(32, '0')
        }
        NumberBase.OCTAL -> if (value >= 0) value.toString(8) else {
            (value and 0xFFFFFFFFL).toString(8)
        }
        NumberBase.DECIMAL -> value.toString()
        NumberBase.HEXADECIMAL -> if (value >= 0) value.toString(16).uppercase() else {
            (value and 0xFFFFFFFFL).toString(16).uppercase()
        }
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${base.displayName}:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        
        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BitRepresentation(
    value: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Bit Representation (32-bit)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Display bits in groups of 4
            val binaryString = (value and 0xFFFFFFFFL).toString(2).padStart(32, '0')
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Split into 4 rows of 8 bits each
                for (row in 0..3) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (group in 0..1) {
                            val startIndex = row * 8 + group * 4
                            val bitGroup = binaryString.substring(startIndex, startIndex + 4)
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                bitGroup.forEach { bit ->
                                    BitDisplay(
                                        bit = bit,
                                        isSet = bit == '1'
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Bit position labels
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bit 31",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Bit 0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BitDisplay(
    bit: Char,
    isSet: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSet) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        }
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSet) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    )
    
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = bit.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
fun ProgrammerButtons(
    currentBase: NumberBase,
    onNumberClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bitwise operations row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProgrammerButton(
                text = "AND",
                enabled = true,
                onClick = { onOperatorClick("AND") },
                modifier = Modifier.weight(1f)
            )
            ProgrammerButton(
                text = "OR",
                enabled = true,
                onClick = { onOperatorClick("OR") },
                modifier = Modifier.weight(1f)
            )
            ProgrammerButton(
                text = "XOR",
                enabled = true,
                onClick = { onOperatorClick("XOR") },
                modifier = Modifier.weight(1f)
            )
            ProgrammerButton(
                text = "NOT",
                enabled = true,
                onClick = { onOperatorClick("NOT") },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Shift operations row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProgrammerButton(
                text = "<<",
                enabled = true,
                onClick = { onOperatorClick("LSH") },
                modifier = Modifier.weight(1f)
            )
            ProgrammerButton(
                text = ">>",
                enabled = true,
                onClick = { onOperatorClick("RSH") },
                modifier = Modifier.weight(1f)
            )
            ProgrammerButton(
                text = "ROL",
                enabled = true,
                onClick = { onOperatorClick("ROL") },
                modifier = Modifier.weight(1f)
            )
            ProgrammerButton(
                text = "ROR",
                enabled = true,
                onClick = { onOperatorClick("ROR") },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Number pad based on current base
        when (currentBase) {
            NumberBase.BINARY -> BinaryNumberPad(onNumberClick)
            NumberBase.OCTAL -> OctalNumberPad(onNumberClick)
            NumberBase.DECIMAL -> DecimalNumberPad(onNumberClick)
            NumberBase.HEXADECIMAL -> HexNumberPad(onNumberClick)
        }
    }
}

@Composable
private fun BinaryNumberPad(onNumberClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProgrammerButton("1", true, { onNumberClick("1") }, Modifier.weight(1f))
            ProgrammerButton("0", true, { onNumberClick("0") }, Modifier.weight(1f))
            ProgrammerButton("C", true, { onNumberClick("C") }, Modifier.weight(1f))
            ProgrammerButton("⌫", true, { onNumberClick("⌫") }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun OctalNumberPad(onNumberClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            listOf("7", "6", "5", "4"),
            listOf("3", "2", "1", "0"),
            listOf("C", "⌫", "+", "-")
        ).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { digit ->
                    ProgrammerButton(
                        text = digit,
                        enabled = true,
                        onClick = { onNumberClick(digit) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DecimalNumberPad(onNumberClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            listOf("9", "8", "7", "÷"),
            listOf("6", "5", "4", "×"),
            listOf("3", "2", "1", "-"),
            listOf("0", "C", "⌫", "+")
        ).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { digit ->
                    ProgrammerButton(
                        text = digit,
                        enabled = true,
                        onClick = { onNumberClick(digit) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HexNumberPad(onNumberClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            listOf("F", "E", "D", "C"),
            listOf("B", "A", "9", "8"),
            listOf("7", "6", "5", "4"),
            listOf("3", "2", "1", "0"),
            listOf("C", "⌫", "+", "-")
        ).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { digit ->
                    ProgrammerButton(
                        text = digit,
                        enabled = true,
                        onClick = { onNumberClick(digit) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgrammerButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.aspectRatio(1.2f),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

// Conversion utilities
object NumberBaseConverter {
    fun convert(value: String, fromBase: NumberBase, toBase: NumberBase): String {
        return try {
            val decimalValue = when (fromBase) {
                NumberBase.BINARY -> value.toLong(2)
                NumberBase.OCTAL -> value.toLong(8)
                NumberBase.DECIMAL -> value.toLong(10)
                NumberBase.HEXADECIMAL -> value.toLong(16)
            }
            
            when (toBase) {
                NumberBase.BINARY -> decimalValue.toString(2)
                NumberBase.OCTAL -> decimalValue.toString(8)
                NumberBase.DECIMAL -> decimalValue.toString()
                NumberBase.HEXADECIMAL -> decimalValue.toString(16).uppercase()
            }
        } catch (e: NumberFormatException) {
            "Error"
        }
    }
    
    fun performBitwiseOperation(left: Long, right: Long, operation: String): Long {
        return when (operation) {
            "AND" -> left and right
            "OR" -> left or right
            "XOR" -> left xor right
            "LSH" -> left shl right.toInt()
            "RSH" -> left shr right.toInt()
            "ROL" -> (left shl right.toInt()) or (left ushr (32 - right.toInt()))
            "ROR" -> (left ushr right.toInt()) or (left shl (32 - right.toInt()))
            else -> left
        }
    }
    
    fun performUnaryOperation(value: Long, operation: String): Long {
        return when (operation) {
            "NOT" -> value.inv()
            else -> value
        }
    }
}