/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.history

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.ClipboardManager
import android.text.format.DateUtils
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.Check
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.BaseFragment
import org.solovyev.android.calculator.CalculatorActivity
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.databinding.FragmentHistoryBinding
import org.solovyev.android.calculator.databinding.FragmentHistoryItemBinding
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.common.text.Strings
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseHistoryFragment(private val recentHistory: Boolean) :
    BaseFragment(R.layout.fragment_history) {

    @Inject
    lateinit var history: History

    @Inject
    lateinit var editor: Editor

    @Inject
    lateinit var bus: Bus

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view!!)
        recyclerView = binding.historyRecyclerview

        val context = inflater.context
        adapter = HistoryAdapter(context)
        bus.register(adapter)

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        return view
    }

    override fun onDestroyView() {
        bus.unregister(adapter)
        _binding = null
        super.onDestroyView()
    }

    fun useState(state: HistoryState) {
        editor.setState(state.editor)
        val activity = requireActivity()
        if (activity !is CalculatorActivity) {
            activity.finish()
        }
    }

    @Suppress("DEPRECATION")
    protected fun copyResult(state: HistoryState) {
        val context = requireActivity()
        val displayText = state.display.text
        if (Strings.isEmpty(displayText)) {
            return
        }
        val clipboard = context.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.text = displayText
    }

    @Suppress("DEPRECATION")
    protected fun copyExpression(state: HistoryState) {
        val context = requireActivity()
        val editorText = state.editor.getTextString()
        if (Strings.isEmpty(editorText)) {
            return
        }
        val clipboard = context.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.text = editorText
    }

    protected fun shouldHaveCopyResult(state: HistoryState): Boolean {
        return !state.display.valid || !Strings.isEmpty(state.display.text)
    }

    inner class HistoryViewHolder(binding: FragmentHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnCreateContextMenuListener,
        View.OnClickListener,
        MenuItem.OnMenuItemClickListener {

        private val valueView: TextView = binding.historyItemValue
        private val commentView: TextView = binding.historyItemComment
        private val timeView: TextView = binding.historyItemTime
        private var state: HistoryState? = null

        init {
            BaseActivity.fixFonts(binding.root, typeface)
            itemView.setOnCreateContextMenuListener(this)
            itemView.setOnClickListener(this)
        }

        fun bind(state: HistoryState) {
            this.state = state
            valueView.text = getHistoryText(state)
            timeView.text = DateUtils.formatDateTime(
                itemView.context,
                state.time,
                DATETIME_FORMAT
            )
            val comment = state.comment
            if (!Strings.isEmpty(comment)) {
                commentView.text = comment
                commentView.visibility = View.VISIBLE
            } else {
                commentView.text = null
                commentView.visibility = View.GONE
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            val currentState = state
            Check.isNotNull(currentState)
            currentState!!

            if (recentHistory) {
                addMenu(menu, R.string.c_use, this)
                addMenu(menu, R.string.c_copy_expression, this)
                if (shouldHaveCopyResult(currentState)) {
                    addMenu(menu, R.string.c_copy_result, this)
                }
                addMenu(menu, R.string.c_save, this)
            } else {
                addMenu(menu, R.string.c_use, this)
                addMenu(menu, R.string.c_copy_expression, this)
                if (shouldHaveCopyResult(currentState)) {
                    addMenu(menu, R.string.c_copy_result, this)
                }
                addMenu(menu, R.string.cpp_edit, this)
                addMenu(menu, R.string.cpp_delete, this)
            }
        }

        override fun onClick(v: View) {
            val currentState = state
            Check.isNotNull(currentState)
            useState(currentState!!)
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            val currentState = state
            Check.isNotNull(currentState)
            currentState!!

            return when (item.itemId) {
                R.string.c_use -> {
                    useState(currentState)
                    true
                }
                R.string.c_copy_expression -> {
                    copyExpression(currentState)
                    true
                }
                R.string.c_copy_result -> {
                    copyResult(currentState)
                    true
                }
                R.string.cpp_edit -> {
                    EditHistoryFragment.show(currentState, false, parentFragmentManager)
                    true
                }
                R.string.c_save -> {
                    EditHistoryFragment.show(currentState, true, parentFragmentManager)
                    true
                }
                R.string.cpp_delete -> {
                    history.removeSaved(currentState)
                    true
                }
                else -> false
            }
        }
    }

    inner class HistoryAdapter(context: Context) :
        RecyclerView.Adapter<HistoryViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private val list: MutableList<HistoryState> = loadHistory()

        init {
            setHasStableIds(true)
        }

        private fun loadHistory(): MutableList<HistoryState> {
            return if (recentHistory) {
                history.getRecent().toMutableList()
            } else {
                history.getSaved().toMutableList()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            return HistoryViewHolder(
                FragmentHistoryItemBinding.inflate(inflater, parent, false)
            )
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemId(position: Int): Long {
            return list[position].hashCode().toLong()
        }

        override fun getItemCount(): Int {
            return list.size
        }

        @Subscribe
        fun onHistoryCleared(e: History.ClearedEvent) {
            if (e.recent != recentHistory) {
                return
            }
            list.clear()
            notifyDataSetChanged()
        }

        @Subscribe
        fun onHistoryAdded(e: History.AddedEvent) {
            if (e.recent != recentHistory) {
                return
            }
            list.add(e.state)
            notifyItemInserted(0)
        }

        @Subscribe
        fun onHistoryUpdated(e: History.UpdatedEvent) {
            if (e.recent != recentHistory) {
                return
            }
            val i = list.indexOf(e.state)
            if (i >= 0) {
                list[i] = e.state
                notifyItemChanged(i)
            }
        }

        @Subscribe
        fun onHistoryRemoved(e: History.RemovedEvent) {
            if (e.recent != recentHistory) {
                return
            }
            val i = list.indexOf(e.state)
            if (i >= 0) {
                list.removeAt(i)
                notifyItemRemoved(i)
            }
        }
    }

    companion object {
        private const val DATETIME_FORMAT = (DateUtils.FORMAT_SHOW_TIME
                or DateUtils.FORMAT_SHOW_DATE
                or DateUtils.FORMAT_ABBREV_MONTH
                or DateUtils.FORMAT_ABBREV_TIME)

        @JvmStatic
        fun getHistoryText(state: HistoryState): String {
            return state.editor.getTextString() + getIdentitySign(state.display.operation) + state.display.text
        }

        private fun getIdentitySign(operation: JsclOperation): String {
            return if (operation == JsclOperation.simplify) "≡" else "="
        }
    }
}
