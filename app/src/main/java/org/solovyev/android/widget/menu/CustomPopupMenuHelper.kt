/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.widget.menu

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Parcelable
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.appcompat.view.menu.ListMenuItemView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.appcompat.view.menu.MenuPresenter
import androidx.appcompat.view.menu.MenuView
import androidx.appcompat.view.menu.SubMenuBuilder
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ActionProvider
import androidx.core.view.MenuItemCompat
import org.solovyev.android.Check

@Suppress("unused", "RestrictedApi")
@SuppressLint("PrivateResource")
class CustomPopupMenuHelper @JvmOverloads constructor(
    private val context: Context,
    private val menu: MenuBuilder,
    private var anchorView: View? = null,
    private val overflowOnly: Boolean = false,
    private val popupStyleAttr: Int = androidx.appcompat.R.attr.popupMenuStyle,
    private val popupStyleRes: Int = 0
) : AdapterView.OnItemClickListener,
    View.OnKeyListener,
    ViewTreeObserver.OnGlobalLayoutListener,
    PopupWindow.OnDismissListener,
    MenuPresenter {

    private val inflater = LayoutInflater.from(context)
    private val adapter = MenuAdapter(menu)
    private val popupMaxWidth: Int

    var forceShowIcon = false
    var popup: ListPopupWindow? = null
        private set

    private var treeObserver: ViewTreeObserver? = null
    private var presenterCallback: MenuPresenter.Callback? = null
    private var measureParent: ViewGroup? = null
    var isKeepOnSubMenu = false
    private var hasContentWidth = false
    private var contentWidth = 0
    var gravity = Gravity.NO_GRAVITY
        private set

    init {
        val res = context.resources
        popupMaxWidth = maxOf(
            res.displayMetrics.widthPixels / 2,
            res.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_config_prefDialogWidth)
        )
        menu.addMenuPresenter(this, context)
    }

    fun setGravity(gravity: Int) {
        this.gravity = gravity
    }

    fun show() {
        check(tryShow()) { "MenuPopupHelper cannot be used without an anchor" }
    }

    fun tryShow(): Boolean {
        popup = ListPopupWindow(context, null, popupStyleAttr, popupStyleRes).apply {
            setOnDismissListener(this@CustomPopupMenuHelper)
            setOnItemClickListener(this@CustomPopupMenuHelper)
            setAdapter(adapter)
            isModal = true
        }

        val anchor = anchorView ?: return false

        val addGlobalListener = treeObserver == null
        treeObserver = anchor.viewTreeObserver.also { observer ->
            if (addGlobalListener) observer.addOnGlobalLayoutListener(this)
        }

        popup?.apply {
            anchorView = anchor
            setDropDownGravity(gravity)

            if (!hasContentWidth) {
                contentWidth = measureContentWidth()
                hasContentWidth = true
            }

            setContentWidth(contentWidth)
            inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            show()
            listView?.setOnKeyListener(this@CustomPopupMenuHelper)
        }

        return true
    }

    fun dismiss() {
        if (isShowing) {
            popup?.dismiss()
        }
    }

    override fun onDismiss() {
        popup = null
        menu.close()
        treeObserver?.let { observer ->
            if (!observer.isAlive) treeObserver = anchorView?.viewTreeObserver
            @Suppress("DEPRECATION")
            observer.removeGlobalOnLayoutListener(this)
        }
        treeObserver = null
    }

    val isShowing: Boolean
        get() = popup?.isShowing == true

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        adapter.adapterMenu.performItemAction(adapter.getItem(position), 0)
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_MENU) {
            dismiss()
            return true
        }
        return false
    }

    private fun measureContentWidth(): Int {
        var maxWidth = 0
        var itemView: View? = null
        var itemType = 0

        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val count = adapter.count

        for (i in 0 until count) {
            val positionType = adapter.getItemViewType(i)
            if (positionType != itemType) {
                itemType = positionType
                itemView = null
            }

            if (measureParent == null) {
                measureParent = FrameLayout(context)
            }

            itemView = adapter.getView(i, itemView, measureParent!!)
            itemView.measure(widthMeasureSpec, heightMeasureSpec)

            val itemWidth = itemView.measuredWidth
            if (itemWidth >= popupMaxWidth) {
                return popupMaxWidth
            } else if (itemWidth > maxWidth) {
                maxWidth = itemWidth
            }
        }

        return maxWidth
    }

    override fun onGlobalLayout() {
        if (!isShowing) return

        val anchor = anchorView
        if (anchor == null || !anchor.isShown) {
            dismiss()
        } else {
            popup?.show()
        }
    }

    override fun initForMenu(context: Context, menu: MenuBuilder) {}

    override fun getMenuView(root: ViewGroup): MenuView {
        throw UnsupportedOperationException("MenuPopupHelpers manage their own views")
    }

    override fun updateMenuView(cleared: Boolean) {
        hasContentWidth = false
        adapter.notifyDataSetChanged()
    }

    override fun setCallback(cb: MenuPresenter.Callback?) {
        presenterCallback = cb
    }

    override fun onSubMenuSelected(subMenu: SubMenuBuilder): Boolean {
        if (!subMenu.hasVisibleItems()) return false

        val subPopup = CustomPopupMenuHelper(
            context,
            subMenu,
            anchorView,
            false,
            popupStyleAttr,
            popupStyleRes
        ).apply {
            setGravity(gravity)
            setCallback(presenterCallback)
            isKeepOnSubMenu = this@CustomPopupMenuHelper.isKeepOnSubMenu

            val preserveIconSpacing = (0 until subMenu.size()).any { i ->
                val childItem = subMenu.getItem(i)
                childItem.isVisible && childItem.icon != null
            }
            forceShowIcon = preserveIconSpacing
        }

        return if (subPopup.tryShow()) {
            presenterCallback?.onOpenSubMenu(subMenu)
            true
        } else {
            false
        }
    }

    override fun onCloseMenu(menu: MenuBuilder, allMenusAreClosing: Boolean) {
        if (menu != this.menu) return

        if (isKeepOnSubMenu && !allMenusAreClosing) return

        dismiss()
        presenterCallback?.onCloseMenu(menu, allMenusAreClosing)
    }

    override fun flagActionItems(): Boolean = false

    override fun expandItemActionView(menu: MenuBuilder, item: MenuItemImpl): Boolean = false

    override fun collapseItemActionView(menu: MenuBuilder, item: MenuItemImpl): Boolean = false

    override fun getId(): Int = 0

    override fun onSaveInstanceState(): Parcelable? = null

    override fun onRestoreInstanceState(state: Parcelable?) {}

    private inner class MenuAdapter(val adapterMenu: MenuBuilder) : BaseAdapter() {
        private var expandedIndex = -1

        init {
            findExpandedIndex()
        }

        override fun getCount(): Int {
            val items = if (overflowOnly) {
                adapterMenu.nonActionItems
            } else {
                adapterMenu.visibleItems
            }
            return if (expandedIndex < 0) items.size else items.size - 1
        }

        override fun getItem(position: Int): MenuItemImpl {
            var pos = position
            val items = if (overflowOnly) {
                adapterMenu.nonActionItems
            } else {
                adapterMenu.visibleItems
            }
            if (expandedIndex >= 0 && pos >= expandedIndex) {
                pos++
            }
            return items[pos]
        }

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val item = getItem(position)
            val actionProvider = MenuItemCompat.getActionProvider(item)

            if (actionProvider != null) {
                val actionView = actionProvider.onCreateActionView(item)
                fixLayoutParams(actionView, parent)
                return actionView
            }

            val actionView = MenuItemCompat.getActionView(item)
            if (actionView != null) {
                (actionView as MenuView.ItemView).initialize(item, 0)
                fixLayoutParams(actionView, parent)
                return actionView
            }

            return getDefaultView(item, convertView, parent)
        }

        private fun fixLayoutParams(actionView: View, parent: ViewGroup) {
            if (parent is FrameLayout) return

            Check.isTrue(parent is AbsListView)
            val lp = actionView.layoutParams
            if (lp != null && lp !is AbsListView.LayoutParams) {
                actionView.layoutParams = AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        private fun getDefaultView(item: MenuItemImpl, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (view == null || view.getTag(DEFAULT_VIEW_TAG_KEY) != DEFAULT_VIEW_TAG) {
                view = inflater.inflate(
                    androidx.appcompat.R.layout.abc_popup_menu_item_layout,
                    parent,
                    false
                ).apply {
                    setTag(DEFAULT_VIEW_TAG_KEY, DEFAULT_VIEW_TAG)
                }
            }

            val itemView = view as MenuView.ItemView
            if (forceShowIcon && view is ListMenuItemView) {
                val preserveIconSpacing = ListMenuItemViewCompat.getPreserveIconSpacing(view)
                view.setForceShowIcon(true)
                ListMenuItemViewCompat.setPreserveIconSpacing(view, preserveIconSpacing)
            }
            itemView.initialize(item, 0)
            return view
        }

        fun findExpandedIndex() {
            val expandedItem = menu.expandedItem
            if (expandedItem != null) {
                val items = menu.nonActionItems
                expandedIndex = items.indexOf(expandedItem)
            } else {
                expandedIndex = -1
            }
        }

        override fun notifyDataSetChanged() {
            findExpandedIndex()
            super.notifyDataSetChanged()
        }

        fun indexOf(item: MenuItem): Int {
            return menu.visibleItems.indexOf(item)
        }
    }

    companion object {
        private val DEFAULT_VIEW_TAG_KEY = org.solovyev.android.calculator.R.id.cpm_default_view_tag_key
        private val COLOR_ATTRS = intArrayOf(androidx.appcompat.R.attr.colorControlNormal)
        private val DEFAULT_VIEW_TAG = Any()

        internal fun tintMenuItem(item: MenuItemImpl, tintColorStateList: ColorStateList) {
            item.icon?.let { icon ->
                val wrappedIcon = DrawableCompat.wrap(icon)
                DrawableCompat.setTintList(wrappedIcon, tintColorStateList)
                item.icon = wrappedIcon
            }

            if (item.hasSubMenu()) {
                val subMenu = item.subMenu ?: return
                for (i in 0 until subMenu.size()) {
                    val subItem = subMenu.getItem(i)
                    if (subItem is MenuItemImpl) {
                        tintMenuItem(subItem, tintColorStateList)
                    }
                }
            }
        }

        internal fun getTintColorStateList(context: Context): ColorStateList? {
            val a = context.obtainStyledAttributes(null, COLOR_ATTRS)
            return try {
                a.getColorStateList(0)
            } finally {
                a.recycle()
            }
        }

        internal fun tintMenuItems(context: Context, menu: Menu, from: Int, to: Int) {
            val tintColorStateList = getTintColorStateList(context) ?: return

            for (i in from until to) {
                val item = menu.getItem(i)
                if (item is MenuItemImpl) {
                    tintMenuItem(item, tintColorStateList)
                }
            }
        }
    }
}
