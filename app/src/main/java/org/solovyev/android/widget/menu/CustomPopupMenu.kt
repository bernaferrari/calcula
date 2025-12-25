/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.content.Context
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPresenter
import androidx.appcompat.view.menu.SubMenuBuilder
import androidx.appcompat.widget.ForwardingListener
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu

@Suppress("unused", "RestrictedApi")
class CustomPopupMenu @JvmOverloads constructor(
    private val context: Context,
    private val anchor: View,
    gravity: Int = Gravity.NO_GRAVITY,
    popupStyleAttr: Int = androidx.appcompat.R.attr.popupMenuStyle,
    popupStyleRes: Int = 0
) : MenuBuilder.Callback, MenuPresenter.Callback {

    val menu = MenuBuilder(context).apply {
        setCallback(this@CustomPopupMenu)
    }

    private val popup = CustomPopupMenuHelper(
        context,
        menu,
        anchor,
        false,
        popupStyleAttr,
        popupStyleRes
    ).apply {
        setGravity(gravity)
        setCallback(this@CustomPopupMenu)
    }

    private var menuItemClickListener: PopupMenu.OnMenuItemClickListener? = null
    private var dismissListener: PopupMenu.OnDismissListener? = null
    private var dragListener: View.OnTouchListener? = null

    var gravity: Int
        get() = popup.gravity
        set(value) {
            popup.setGravity(value)
        }

    fun setForceShowIcon(forceShow: Boolean) {
        popup.forceShowIcon = forceShow
    }

    fun getDragToOpenListener(): View.OnTouchListener {
        if (dragListener == null) {
            dragListener = object : ForwardingListener(anchor) {
                override fun onForwardingStarted(): Boolean {
                    show()
                    return true
                }

                override fun onForwardingStopped(): Boolean {
                    dismiss()
                    return true
                }

                override fun getPopup(): ListPopupWindow = this@CustomPopupMenu.popup.popup!!
            }
        }
        return dragListener!!
    }

    fun getMenuInflater(): MenuInflater {
        return object : SupportMenuInflater(context) {
            override fun inflate(menuRes: Int, menu: Menu) {
                val sizeBefore = menu.size()
                super.inflate(menuRes, menu)
                CustomPopupMenuHelper.tintMenuItems(context, menu, sizeBefore, menu.size())
            }
        }
    }

    fun inflate(@MenuRes menuRes: Int) {
        getMenuInflater().inflate(menuRes, menu)
    }

    fun show() {
        popup.show()
    }

    fun dismiss() {
        popup.dismiss()
    }

    fun setKeepOnSubMenu(keepOnSubMenu: Boolean) {
        popup.isKeepOnSubMenu = keepOnSubMenu
    }

    fun isShowing(): Boolean = popup.isShowing

    fun setOnMenuItemClickListener(listener: PopupMenu.OnMenuItemClickListener?) {
        menuItemClickListener = listener
    }

    fun setOnDismissListener(listener: PopupMenu.OnDismissListener?) {
        dismissListener = listener
    }

    override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
        return menuItemClickListener?.onMenuItemClick(item) ?: false
    }

    override fun onCloseMenu(menu: MenuBuilder, allMenusAreClosing: Boolean) {
        dismissListener?.onDismiss(null)
    }

    override fun onOpenSubMenu(subMenu: MenuBuilder): Boolean {
        if (!subMenu.hasVisibleItems()) {
            return true
        }
        return true
    }

    override fun onMenuModeChange(menu: MenuBuilder) {}
}
