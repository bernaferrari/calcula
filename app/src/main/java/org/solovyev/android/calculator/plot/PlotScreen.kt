package org.solovyev.android.calculator.plot

import android.graphics.RectF
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jscl.math.function.CustomFunction
import org.solovyev.android.calculator.Calculator
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.ParseException
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.Utils
import org.solovyev.android.calculator.functions.CppFunction
import org.solovyev.android.plotter.Color
import org.solovyev.android.plotter.Plot
import org.solovyev.android.plotter.PlotFunction
import org.solovyev.android.plotter.PlotIconView
import org.solovyev.android.plotter.PlotViewFrame
import org.solovyev.android.plotter.Plotter
import org.solovyev.android.plotter.meshes.MeshSpec

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PlotScreen(
    plotter: Plotter,
    calculator: Calculator,
    onBack: () -> Unit,
    viewModel: PlotComposeViewModel = hiltViewModel()
) {
    val functions by viewModel.functions.collectAsStateWithLifecycle()
    var showFunctions by remember { mutableStateOf(false) }
    var showDimensions by remember { mutableStateOf(false) }
    var editFunction by remember { mutableStateOf<PlotFunction?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<PlotFunction?>(null) }
    var is3d by remember { mutableStateOf(plotter.is3d) }

    Scaffold(
        topBar = {
            PlotTopBar(
                is3d = is3d,
                onToggle3d = { enabled ->
                    is3d = enabled
                    plotter.set3d(enabled)
                },
                onShowDimensions = { showDimensions = true },
                onShowFunctions = { showFunctions = true },
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editFunction = null; showEditDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.cpp_add))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { padding ->
        PlotCanvas(
            plotter = plotter,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }

    // Functions Bottom Sheet
    if (showFunctions) {
        PlotFunctionsSheet(
            functions = functions,
            onDismiss = { showFunctions = false },
            onEdit = { editFunction = it; showEditDialog = true; showFunctions = false },
            onDelete = { confirmDelete = it; showFunctions = false },
            onAdd = { editFunction = null; showEditDialog = true; showFunctions = false }
        )
    }

    // Dimensions Dialog
    if (showDimensions) {
        PlotDimensionsDialog(
            plotter = plotter,
            onDismiss = { showDimensions = false }
        )
    }

    // Edit Function Dialog
    if (showEditDialog) {
        PlotEditFunctionDialog(
            calculator = calculator,
            plotter = plotter,
            function = editFunction,
            onDismiss = { showEditDialog = false }
        )
    }

    // Delete Confirmation
    confirmDelete?.let { function ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(text = stringResource(R.string.cpp_delete)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.function_removal_confirmation_question,
                        function.function.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        plotter.remove(function)
                        confirmDelete = null
                    }
                ) {
                    Text(text = stringResource(R.string.cpp_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) {
                    Text(text = stringResource(R.string.cpp_cancel))
                }
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PlotTopBar(
    is3d: Boolean,
    onToggle3d: (Boolean) -> Unit,
    onShowDimensions: () -> Unit,
    onShowFunctions: () -> Unit,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.cpp_plotter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cpp_back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        actions = {
            // 2D/3D Toggle with SegmentedButton
            SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(end = 8.dp)) {
                SegmentedButton(
                    selected = !is3d,
                    onClick = { onToggle3d(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {}
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timeline,
                        contentDescription = stringResource(R.string.cpp_plot_mode_2d),
                        modifier = Modifier.size(18.dp)
                    )
                }
                SegmentedButton(
                    selected = is3d,
                    onClick = { onToggle3d(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {}
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ViewInAr,
                        contentDescription = stringResource(R.string.cpp_plot_mode_3d),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            IconButton(onClick = onShowDimensions) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = stringResource(R.string.cpp_plot_range)
                )
            }
        }
    )
}

@Composable
private fun PlotCanvas(plotter: Plotter, modifier: Modifier = Modifier) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest.toArgb()
    val axisColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f).toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f).toArgb()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val density = LocalContext.current.resources.displayMetrics.density
    val labelTextSizePx = 12f * density
    var plotView by remember { mutableStateOf<PlotViewFrame?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlotViewFrame(context).also { view ->
                plotView = view
                val axisStyle = plotter.plotData.axisStyle
                axisStyle.backgroundColor = backgroundColor
                axisStyle.axisColor = axisColor
                axisStyle.gridColor = gridColor
                axisStyle.labelColor = labelColor
                axisStyle.labelTextSizePx = labelTextSizePx
                plotter.setAxisStyle(axisStyle)
                view.setPlotter(plotter)
            }
        },
        update = {
            val axisStyle = plotter.plotData.axisStyle
            axisStyle.backgroundColor = backgroundColor
            axisStyle.axisColor = axisColor
            axisStyle.gridColor = gridColor
            axisStyle.labelColor = labelColor
            axisStyle.labelTextSizePx = labelTextSizePx
            plotter.setAxisStyle(axisStyle)
        }
    )

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> plotView?.onResume()
                Lifecycle.Event.ON_PAUSE -> plotView?.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PlotFunctionsSheet(
    functions: List<PlotFunction>,
    onDismiss: () -> Unit,
    onEdit: (PlotFunction) -> Unit,
    onDelete: (PlotFunction) -> Unit,
    onAdd: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cpp_plot_functions),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                FilledTonalButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.cpp_add))
                }
            }

            Spacer(Modifier.height(16.dp))

            if (functions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No functions added yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(functions) { function ->
                        PlotFunctionCard(
                            function = function,
                            onEdit = { onEdit(function) },
                            onDelete = { onDelete(function) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlotFunctionCard(
    function: PlotFunction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ComposeColor(function.meshSpec.color.toInt()))
            )

            // Function name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = function.function.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // Edit button
            FilledTonalIconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.cpp_edit)
                )
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cpp_delete)
                )
            }
        }
    }
}

@Composable
private fun PlotDimensionsDialog(
    plotter: Plotter,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bounds = remember { plotter.dimensions.graph.makeBounds() }
    var xMin by remember { mutableStateOf(bounds.left.toString()) }
    var xMax by remember { mutableStateOf(bounds.right.toString()) }
    var yMin by remember { mutableStateOf(bounds.top.toString()) }
    var yMax by remember { mutableStateOf(bounds.bottom.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Tune, contentDescription = null) },
        title = { Text(text = stringResource(R.string.cpp_plot_range)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = xMin,
                        onValueChange = { xMin = it; error = null },
                        label = { Text(text = stringResource(R.string.cpp_plot_x_min)) },
                        isError = error != null,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = xMax,
                        onValueChange = { xMax = it; error = null },
                        label = { Text(text = stringResource(R.string.cpp_plot_x_max)) },
                        isError = error != null,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = yMin,
                        onValueChange = { yMin = it; error = null },
                        label = { Text(text = stringResource(R.string.cpp_plot_y_min)) },
                        isError = error != null,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = yMax,
                        onValueChange = { yMax = it; error = null },
                        label = { Text(text = stringResource(R.string.cpp_plot_y_max)) },
                        isError = error != null,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newBounds = parseBounds(xMin, xMax, yMin, yMax)
                    if (newBounds == null || newBounds.left >= newBounds.right || newBounds.top >= newBounds.bottom) {
                        error = context.getString(R.string.cpp_nan)
                    } else {
                        Plot.setGraphBounds(null, plotter, newBounds, plotter.is3d)
                        onDismiss()
                    }
                }
            ) {
                Text(text = stringResource(R.string.cpp_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cpp_cancel))
            }
        }
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun PlotEditFunctionDialog(
    calculator: Calculator,
    plotter: Plotter,
    function: PlotFunction?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val initial = remember(function) { toCppFunction(function) }
    val initialMesh = remember(function) { function?.meshSpec ?: defaultMeshSpec(context) }
    val colorOptions = remember { MeshSpec.LightColors.asIntArray().toList() }

    var name by remember(function) { mutableStateOf(initial.name) }
    var body by remember(function) { mutableStateOf(initial.body) }
    var params by remember(function) { mutableStateOf(initial.parameters.toList()) }
    var color by remember(function) { mutableStateOf(initialMesh.color.toInt()) }
    var lineWidth by remember(function) { mutableStateOf(initialMesh.width) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var bodyError by remember { mutableStateOf<String?>(null) }
    var paramsError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial.id == CppFunction.NO_ID) {
                    stringResource(R.string.function_create_function)
                } else {
                    stringResource(R.string.function_edit_function)
                }
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameError = null },
                        label = { Text(text = stringResource(R.string.cpp_name)) },
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(text = it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it; bodyError = null },
                        label = { Text(text = stringResource(R.string.cpp_function_body)) },
                        isError = bodyError != null,
                        supportingText = bodyError?.let { { Text(text = it) } },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.cpp_parameters),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(params.indices.toList()) { index ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = params[index],
                            onValueChange = { value ->
                                params = params.toMutableList().apply { set(index, value) }
                                paramsError = null
                            },
                            label = { Text(text = stringResource(R.string.cpp_parameter)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            params = params.toMutableList().apply { removeAt(index) }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }

                if (params.size < 2) {
                    item {
                        TextButton(onClick = { params = params + "" }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(text = stringResource(R.string.cpp_add_parameter))
                        }
                    }
                }

                paramsError?.let { error ->
                    item {
                        Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.cpp_plot_function_line_color),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        colorOptions.forEach { option ->
                            ColorChip(
                                color = option,
                                selected = option == color,
                                onClick = { color = option }
                            )
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = stringResource(R.string.cpp_plot_function_line_width),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = lineWidth.toFloat(),
                            onValueChange = { lineWidth = it.toInt() },
                            valueRange = MeshSpec.MIN_WIDTH.toFloat()..MeshSpec.MAX_WIDTH.toFloat(),
                            steps = MeshSpec.MAX_WIDTH - MeshSpec.MIN_WIDTH - 1
                        )
                    }
                }

                item {
                    // Preview
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx -> PlotIconView(ctx) },
                            update = { view -> view.setMeshSpec(buildMeshSpec(color, lineWidth)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val nameValue = name.trim()
                    val bodyValue = body.trim()
                    val paramList = params.map { it.trim() }.filter { it.isNotEmpty() }

                    val nameValid = validateName(context, nameValue)
                    if (!nameValid.first) {
                        nameError = nameValid.second
                        return@TextButton
                    }

                    val paramsValid = validateParameters(context, paramList)
                    if (!paramsValid.first) {
                        paramsError = paramsValid.second
                        return@TextButton
                    }

                    val bodyValid = validateBody(context, calculator, bodyValue, paramList)
                    if (!bodyValid.first) {
                        bodyError = bodyValid.second
                        return@TextButton
                    }

                    val prepared = calculator.prepare(bodyValue).value
                    val updated = CppFunction.builder(nameValue, prepared)
                        .withId(initial.id)
                        .withParameters(paramList)
                        .build()
                    val expressionFunction = ExpressionFunction(updated.toJsclBuilder().create())
                    val plotFunction = PlotFunction.create(expressionFunction, buildMeshSpec(color, lineWidth))
                    if (updated.id != CppFunction.NO_ID) {
                        plotter.update(updated.id, plotFunction)
                    } else {
                        plotter.add(plotFunction)
                    }
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.cpp_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cpp_cancel))
            }
        }
    )
}

@Composable
private fun ColorChip(color: Int, selected: Boolean, onClick: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else ComposeColor.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "borderColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .border(width = 3.dp, color = borderColor, shape = CircleShape)
            .padding(3.dp)
            .clip(CircleShape)
            .background(ComposeColor(color))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = ComposeColor.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun buildMeshSpec(color: Int, width: Int): MeshSpec {
    val meshSpec = MeshSpec.create(Color.create(color), width)
    meshSpec.pointsCount = PlotActivity.POINTS_COUNT
    return meshSpec
}

private fun defaultMeshSpec(context: android.content.Context): MeshSpec {
    return buildMeshSpec(
        MeshSpec.LightColors.asIntArray().first(),
        MeshSpec.defaultWidth(context)
    )
}

private fun toCppFunction(function: PlotFunction?): CppFunction {
    if (function == null) {
        return CppFunction.builder("", "").build()
    }
    val name = function.function.name
    val jsclFunction = (function.function as? ExpressionFunction)?.function
    val customFunction = jsclFunction as? CustomFunction
    val body = customFunction?.getContent() ?: ""
    val params = customFunction?.getParameterNames()?.toList().orEmpty()
    return CppFunction.builder(name, body)
        .withId(function.function.id)
        .withParameters(params)
        .build()
}

private fun validateName(context: android.content.Context, name: String): Pair<Boolean, String?> {
    if (name.isEmpty()) {
        return false to context.getString(R.string.cpp_field_cannot_be_empty)
    }
    if (!Engine.isValidName(name)) {
        return false to context.getString(R.string.cpp_name_contains_invalid_characters)
    }
    return true to null
}

private fun validateParameters(
    context: android.content.Context,
    parameters: List<String>
): Pair<Boolean, String?> {
    if (parameters.size > 2) {
        return false to context.getString(R.string.cpp_plot_too_many_variables)
    }
    val seen = mutableSetOf<String>()
    for (param in parameters) {
        if (!Engine.isValidName(param)) {
            return false to context.getString(R.string.cpp_name_contains_invalid_characters)
        }
        if (!seen.add(param)) {
            return false to context.getString(R.string.cpp_duplicate_parameter, param)
        }
    }
    return true to null
}

private fun validateBody(
    context: android.content.Context,
    calculator: Calculator,
    body: String,
    parameters: List<String>
): Pair<Boolean, String?> {
    if (body.isEmpty()) {
        return false to context.getString(R.string.cpp_field_cannot_be_empty)
    }
    return try {
        val pe = calculator.prepare(body)
        if (pe.hasUndefinedVariables()) {
            for (undefined in pe.undefinedVariables) {
                if (!parameters.contains(undefined.name)) {
                    return false to context.getString(R.string.c_error)
                }
            }
        }
        true to null
    } catch (e: ParseException) {
        false to Utils.getErrorMessage(e)
    }
}

private fun parseBounds(xMin: String, xMax: String, yMin: String, yMax: String): RectF? {
    return try {
        RectF(
            xMin.replace(",", ".").replace("−", "-").toFloat(),
            yMin.replace(",", ".").replace("−", "-").toFloat(),
            xMax.replace(",", ".").replace("−", "-").toFloat(),
            yMax.replace(",", ".").replace("−", "-").toFloat()
        )
    } catch (e: NumberFormatException) {
        null
    }
}
