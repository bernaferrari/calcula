package org.solovyev.android.calculator.matrix

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

/**
 * User: Solovyev_S
 * Date: 12.10.12
 * Time: 15:41
 */
class MatrixView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TableLayout(context, attrs) {

    companion object {
        private const val DEFAULT_CELL_TEXT = "0"
        private const val NUMBER_INDEX = -1
    }

    var rows: Int = 0
        private set

    var cols: Int = 0
        private set

    var defaultCellText: CharSequence? = DEFAULT_CELL_TEXT

    private var initialized = false

    fun setMatrixCols(newCols: Int) {
        setMatrixDimensions(rows, newCols)
    }

    fun setMatrixRows(newRows: Int) {
        setMatrixDimensions(newRows, cols)
    }

    fun setMatrixDimensions(newRows: Int, newCols: Int) {
        require(newRows > 1) { "Number of rows must be more than 1: $newRows" }
        require(newCols > 1) { "Number of columns must be more than 1: $newCols" }

        val rowsChanged = this.rows != newRows
        val colsChanged = this.cols != newCols

        if (rowsChanged || colsChanged) {
            if (!initialized) {
                addRow(NUMBER_INDEX, 0)
                initialized = true
            }

            when {
                this.cols > newCols -> removeCols(newCols)
                this.cols < newCols -> addCols(newCols)
            }

            this.cols = newCols

            when {
                this.rows > newRows -> removeRows(newRows)
                this.rows < newRows -> addRows(newRows)
            }

            this.rows = newRows
        }
    }

    fun setMatrix(matrix: Array<Array<Any>>) {
        val rows = matrix.size
        val cols = matrix[0].size

        setMatrixDimensions(rows, cols)
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                setCell(row, col, matrix[row][col])
            }
        }
    }

    fun toMatrix(): Array<Array<String>> {
        val result = Array(rows) { Array(cols) { "" } }

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val cellTextView = getCell(this, row, col) as? TextView
                cellTextView?.let {
                    result[row][col] = it.text.toString()
                }
            }
        }

        return result
    }

    private fun setCell(row: Int, col: Int, o: Any?) {
        val cellTextView = getCell(this, row, col) as? TextView
        cellTextView?.text = o?.toString()
    }

    private fun addRows(newRows: Int) {
        for (row in this.rows until newRows) {
            addRow(row, cols)
        }
    }

    private fun removeRows(newRows: Int) {
        for (row in this.rows - 1 downTo newRows) {
            removeRow(row)
        }
    }

    private fun addCols(newCols: Int) {
        for (row in NUMBER_INDEX until rows) {
            val rowView = getRow(row)
            rowView?.let {
                for (col in this.cols until newCols) {
                    it.addView(createCellView(row, col))
                }
            }
        }
    }

    private fun removeCols(newCols: Int) {
        for (row in NUMBER_INDEX until rows) {
            val rowView = getRow(row)
            rowView?.let {
                for (col in this.cols - 1 downTo newCols) {
                    val cellView = getCell(it, row, col)
                    cellView?.let { cell -> it.removeView(cell) }
                }
            }
        }
    }

    private fun addRow(row: Int, newCols: Int) {
        addView(createRowView(row, newCols))
    }

    private fun removeRow(row: Int) {
        val rowView = getRow(row)
        rowView?.let { removeView(it) }
    }

    private fun getRow(row: Int): TableRow? =
        findViewWithTag(getRowTag(row)) as? TableRow

    private fun getCell(view: View, row: Int, col: Int): View? =
        view.findViewWithTag(getCellTag(row, col))

    private fun getRowTag(row: Int): String =
        if (row != NUMBER_INDEX) "row_$row" else "row_index"

    private fun createRowView(row: Int, cols: Int): View {
        val rowView = TableRow(context).apply {
            tag = getRowTag(row)
        }

        if (row != NUMBER_INDEX) {
            rowView.addView(createCellView(row, NUMBER_INDEX))
        } else {
            // empty cell
            rowView.addView(View(context))
        }

        for (col in 0 until cols) {
            rowView.addView(createCellView(row, col))
        }

        return rowView
    }

    private fun createCellView(row: Int, col: Int): View {
        val result: TextView = if (row != NUMBER_INDEX && col != NUMBER_INDEX) {
            EditText(context).apply {
                setText(defaultCellText)
            }
        } else {
            TextView(context).apply {
                text = when {
                    row == NUMBER_INDEX -> (col + 1).toString()
                    else -> (row + 1).toString()
                }
            }
        }

        result.tag = getCellTag(row, col)
        return result
    }

    private fun getCellTag(row: Int, col: Int): String =
        if (row != NUMBER_INDEX) "cell_${row}_$col" else "cell_index_$col"
}
