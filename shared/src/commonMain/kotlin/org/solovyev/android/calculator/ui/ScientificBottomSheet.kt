@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package org.solovyev.android.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScientificBottomSheet(
    onFunctionClick: (String) -> Unit,
    onConstantClick: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Scientific Functions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Trig Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScientificButton("sin", onClick = { onFunctionClick("sin") })
                ScientificButton("cos", onClick = { onFunctionClick("cos") })
                ScientificButton("tan", onClick = { onFunctionClick("tan") })
            }
            
            // Inverse Trig
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScientificButton("asin", onClick = { onFunctionClick("asin") })
                ScientificButton("acos", onClick = { onFunctionClick("acos") })
                ScientificButton("atan", onClick = { onFunctionClick("atan") })
            }

            // Other functions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScientificButton("ln", onClick = { onFunctionClick("ln") })
                ScientificButton("log", onClick = { onFunctionClick("log") })
                ScientificButton("√", onClick = { onFunctionClick("sqrt") })
                ScientificButton("^", onClick = { onFunctionClick("^") })
            }
            
            // Constants
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScientificButton("π", onClick = { onConstantClick("π") })
                ScientificButton("e", onClick = { onConstantClick("e") })
                ScientificButton("i", onClick = { onConstantClick("i") })
            }
        }
    }
}

@Composable
private fun RowScope.ScientificButton(
    text: String, 
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.filledTonalButtonColors()
    ) {
        Text(text = text)
    }
}
