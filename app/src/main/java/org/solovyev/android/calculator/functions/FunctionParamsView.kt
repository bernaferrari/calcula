package org.solovyev.android.calculator.functions

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import org.solovyev.android.Check
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.R

class FunctionParamsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val theme = App.getTheme()
    private val clickableAreaSize: Int
    private val imageButtonSize: Int
    private val imageButtonPadding: Int
    private var maxRowId = START_ROW_ID
    private var maxParams = Int.MAX_VALUE
    private lateinit var headerView: LinearLayout

    init {
        val resources = resources
        clickableAreaSize = resources.getDimensionPixelSize(R.dimen.cpp_clickable_area_size)
        imageButtonSize = resources.getDimensionPixelSize(R.dimen.cpp_image_button_size)
        imageButtonPadding = resources.getDimensionPixelSize(R.dimen.cpp_image_button_padding)
        init()
    }

    private fun init() {
        orientation = VERTICAL

        headerView = makeRowView(context)
        val addButton = makeButton(
            if (theme.light) R.drawable.ic_add_black_24dp 
            else R.drawable.ic_add_white_24dp
        )
        addButton.id = R.id.function_params_add
        addButton.setOnClickListener {
            val rowView = addParam(generateParamName())
            val paramView = getParamView(rowView)
            paramView.requestFocus()
        }
        headerView.addView(addButton, makeButtonParams())
        headerView.addView(View(context), LayoutParams(3 * clickableAreaSize, ViewGroup.LayoutParams.WRAP_CONTENT))
        addView(headerView, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    private fun generateParamName(): String? {
        val available = PARAM_NAMES.toMutableList()
        available.removeAll(params.toSet())
        return if (available.isNotEmpty()) available[0] else null
    }

    private fun makeButton(icon: Int): ImageButton {
        return ImageButton(context).apply {
            setImageResource(icon)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(imageButtonPadding, imageButtonPadding, imageButtonPadding, imageButtonPadding)
            val value = TypedValue()
            if (context.theme.resolveAttribute(
                    androidx.appcompat.R.attr.selectableItemBackgroundBorderless, value, true)) {
                setBackgroundResource(value.resourceId)
            }
        }
    }

    private fun makeRowView(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            minimumHeight = clickableAreaSize
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    fun addParams(params: List<String>) {
        params.forEach { addParam(it) }
    }

    fun addParam(param: String?): LinearLayout {
        return addParam(param, maxRowId++)
    }

    private fun addParam(param: String?, id: Int): LinearLayout {
        val rowView = makeRowView(context)

        val removeButton = makeButton(
            if (theme.light) R.drawable.ic_remove_black_24dp 
            else R.drawable.ic_remove_white_24dp
        )
        removeButton.setOnClickListener { removeRow(rowView) }
        rowView.addView(removeButton, makeButtonParams())

        val upButton = makeButton(
            if (theme.light) R.drawable.ic_arrow_upward_black_24dp 
            else R.drawable.ic_arrow_upward_white_24dp
        )
        upButton.setOnClickListener { upRow(rowView) }
        rowView.addView(upButton, makeButtonParams())

        val downButton = makeButton(
            if (theme.light) R.drawable.ic_arrow_downward_black_24dp 
            else R.drawable.ic_arrow_downward_white_24dp
        )
        downButton.setOnClickListener { downRow(rowView) }
        rowView.addView(downButton, makeButtonParams())

        val paramLabel = TextInputLayout(context)
        val paramView = EditText(context).apply {
            if (param != null) {
                setText(param)
            }
            onFocusChangeListener = this@FunctionParamsView.onFocusChangeListener
            setSelectAllOnFocus(true)
            inputType = EditorInfo.TYPE_CLASS_TEXT
            this.id = id
            tag = PARAM_VIEW_TAG
            setHint(R.string.cpp_parameter)
        }
        val textInputLayoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        paramLabel.addView(paramView, textInputLayoutParams)

        rowView.addView(paramLabel, LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        addView(rowView, maxOf(0, childCount - 1), LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        onParamsChanged()
        return rowView
    }

    private fun onParamsChanged() {
        headerView.visibility = if (paramsCount < maxParams) VISIBLE else GONE
    }

    private fun makeButtonParams(): LayoutParams {
        return LayoutParams(imageButtonSize, imageButtonSize)
    }

    private fun downRow(row: ViewGroup) {
        val index = indexOfChild(row)
        if (index < childCount - 1 - FOOTERS) {
            swap(row, getRow(index + 1))
        }
    }

    private fun upRow(row: ViewGroup) {
        val index = indexOfChild(row)
        if (index > 0) {
            swap(row, getRow(index - 1))
        }
    }

    private fun swap(l: ViewGroup, r: ViewGroup) {
        val lParam = getParamView(l)
        val rParam = getParamView(r)
        swap(lParam, rParam)
    }

    private fun swap(l: TextView, r: TextView) {
        val tmp = l.text
        l.text = r.text
        r.text = tmp
    }

    private fun getRow(index: Int): ViewGroup {
        Check.isTrue(index in 0 until paramsCount)
        return getChildAt(index) as ViewGroup
    }

    fun removeRow(row: ViewGroup) {
        removeView(row)
        onParamsChanged()
    }

    val params: List<String>
        get() {
            val params = mutableListOf<String>()
            for (i in 0 until paramsCount) {
                val row = getRow(i)
                val paramView = getParamView(row)
                params.add(paramView.text.toString())
            }
            return params
        }

    private val paramsCount: Int
        get() = childCount - FOOTERS

    fun setMaxParams(maxParams: Int) {
        this.maxParams = maxParams
        onParamsChanged()
    }

    private fun getParamView(row: ViewGroup): EditText {
        val paramLabel = getParamLabel(row)
        return App.find(paramLabel, EditText::class.java) ?: run {
            Check.shouldNotHappen()
            throw IllegalStateException("EditText not found")
        }
    }

    private fun getParamLabel(row: ViewGroup): TextInputLayout {
        return row.getChildAt(PARAM_VIEW_INDEX) as TextInputLayout
    }

    fun getParamLabel(param: Int): TextInputLayout {
        return getParamLabel(getRow(param))
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState!!, rowIds)
    }

    private val rowIds: IntArray
        get() {
            val rowIds = IntArray(childCount - FOOTERS)
            for (i in 0 until childCount - FOOTERS) {
                val row = getRow(i)
                val paramView = getParamView(row)
                rowIds[i] = paramView.id
            }
            return rowIds
        }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        for (rowId in state.rowIds) {
            addParam(null, rowId)
            maxRowId = maxOf(maxRowId, rowId + 1)
        }

        super.onRestoreInstanceState(state.superState)
    }

    class SavedState : BaseSavedState {
        val rowIds: IntArray

        constructor(superState: Parcelable, rowIds: IntArray) : super(superState) {
            this.rowIds = rowIds
        }

        private constructor(parcel: Parcel) : super(parcel) {
            rowIds = parcel.createIntArray() ?: intArrayOf()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeIntArray(rowIds)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

    companion object {
        const val PARAM_VIEW_TAG = "param-view"
        private val PARAM_NAMES = listOf("x", "y", "z", "t", "a", "b", "c")
        private const val FOOTERS = 1
        private const val PARAM_VIEW_INDEX = 3
        private val START_ROW_ID = App.generateViewId()
    }
}
