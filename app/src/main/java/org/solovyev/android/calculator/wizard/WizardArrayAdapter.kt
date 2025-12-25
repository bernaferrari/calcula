package org.solovyev.android.calculator.wizard

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

internal class WizardArrayAdapter<T> constructor(
    context: Context,
    items: List<T>
) : ArrayAdapter<T>(context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, items) {

    constructor(context: Context, items: Array<T>) : this(
        context,
        items.toList()
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        if (view is TextView) {
            view.setTextAppearance(context, android.R.style.TextAppearance_Large)
        }
        return view
    }

    companion object {
        fun create(context: Context, arrayResId: Int): WizardArrayAdapter<String> {
            return WizardArrayAdapter(context, context.resources.getStringArray(arrayResId))
        }
    }
}
