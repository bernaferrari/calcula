package org.solovyev.android.widget.menu

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.appcompat.view.menu.MenuView

class MenuItemDivider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), MenuView.ItemView {

    init {
        background = divider
        isEnabled = false
    }

    private val divider: Drawable?
        get() {
            val a = context.obtainStyledAttributes(ATTRS)
            return try {
                a.getDrawable(0)
            } finally {
                a.recycle()
            }
        }

    override fun initialize(itemData: MenuItemImpl, menuType: Int) {
        background = divider
        isEnabled = false
    }

    override fun getItemData(): MenuItemImpl? = null

    override fun setTitle(title: CharSequence?) {}

    override fun setEnabled(enabled: Boolean) {}

    override fun setCheckable(checkable: Boolean) {}

    override fun setChecked(checked: Boolean) {}

    override fun setShortcut(showShortcut: Boolean, shortcutKey: Char) {}

    override fun setIcon(icon: Drawable?) {}

    override fun prefersCondensedTitle(): Boolean = false

    override fun showsIcon(): Boolean = false

    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }
}
