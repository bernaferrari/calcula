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

package org.solovyev.android.calculator.entities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.ClipboardManager
import android.text.TextUtils
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
import org.solovyev.android.Check
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.BaseFragment
import org.solovyev.android.calculator.CalculatorActivity
import org.solovyev.android.calculator.Keyboard
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.databinding.FragmentEntitiesBinding
import org.solovyev.android.calculator.databinding.FragmentEntitiesItemBinding
import org.solovyev.common.math.MathEntity
import org.solovyev.common.text.Strings
import javax.inject.Inject

abstract class BaseEntitiesFragment<E : MathEntity> : BaseFragment(R.layout.fragment_entities) {

    lateinit var recyclerView: RecyclerView

    @Inject
    lateinit var keyboard: Keyboard

    private var adapter: EntitiesAdapter? = null
    private var category: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            category = bundle.getString(ARG_CATEGORY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        val binding = FragmentEntitiesBinding.bind(view)
        recyclerView = binding.entitiesRecyclerview
        val context = inflater.context

        adapter = EntitiesAdapter(
            context,
            if (TextUtils.isEmpty(category)) getEntities().toMutableList() else getEntities(category!!)
        )

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        return view
    }

    protected fun onClick(entity: E) {
        keyboard.buttonPressed(entity.name)
        val activity = requireActivity()
        if (activity !is CalculatorActivity) {
            activity.finish()
        }
    }

    private fun getEntities(category: String): MutableList<E> {
        Check.isNotEmpty(category)
        return getEntities().filter { isInCategory(it, category) }.toMutableList()
    }

    protected fun isInCategory(entity: E): Boolean {
        return TextUtils.isEmpty(category) || isInCategory(entity, category!!)
    }

    private fun isInCategory(entity: E, category: String): Boolean {
        val entityCategory = getCategory(entity) ?: return false
        return TextUtils.equals(entityCategory.getCategoryName(), category)
    }

    protected abstract fun getEntities(): List<E>

    protected abstract fun getCategory(entity: E): Category<*>?

    protected fun getAdapter(): EntitiesAdapter? = adapter

    @Suppress("DEPRECATION")
    protected fun copyText(text: String?) {
        if (!Strings.isEmpty(text)) {
            return
        }
        val clipboard = requireActivity().getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.text = text
    }

    protected fun onEntityAdded(entity: E) {
        val adapter = getAdapter() ?: return
        if (!isInCategory(entity)) {
            return
        }
        adapter.add(entity)
    }

    protected fun onEntityChanged(entity: E) {
        val adapter = getAdapter() ?: return
        if (!isInCategory(entity)) {
            return
        }
        adapter.update(entity)
    }

    protected fun onEntityRemoved(entity: E) {
        val adapter = getAdapter() ?: return
        if (!isInCategory(entity)) {
            return
        }
        adapter.remove(entity)
    }

    protected abstract fun getDescription(entity: E): String?

    protected abstract fun getName(entity: E): String

    protected abstract fun onCreateContextMenu(
        menu: ContextMenu,
        entity: E,
        listener: MenuItem.OnMenuItemClickListener
    )

    protected abstract fun onMenuItemClicked(item: MenuItem, entity: E): Boolean

    inner class EntityViewHolder(binding: FragmentEntitiesItemBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener,
        View.OnCreateContextMenuListener,
        MenuItem.OnMenuItemClickListener {

        val textView: TextView = binding.entityText
        val descriptionView: TextView = binding.entityDescription
        private var entity: E? = null

        init {
            BaseActivity.fixFonts(itemView, typeface)
            itemView.setOnClickListener(this)
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(entity: E) {
            this.entity = entity
            textView.text = getName(entity)

            val description = getDescription(entity)
            if (!Strings.isEmpty(description)) {
                descriptionView.visibility = View.VISIBLE
                descriptionView.text = description
            } else {
                descriptionView.visibility = View.GONE
            }
        }

        override fun onClick(v: View) {
            Check.isNotNull(entity)
            this@BaseEntitiesFragment.onClick(entity!!)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            Check.isNotNull(entity)
            this@BaseEntitiesFragment.onCreateContextMenu(menu, entity!!, this)
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            Check.isNotNull(entity)
            return this@BaseEntitiesFragment.onMenuItemClicked(item, entity!!)
        }
    }

    inner class EntitiesAdapter(
        context: Context,
        private val list: MutableList<E>
    ) : RecyclerView.Adapter<EntityViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        init {
            list.sortWith(COMPARATOR)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityViewHolder {
            return EntityViewHolder(FragmentEntitiesItemBinding.inflate(inflater, parent, false))
        }

        override fun onBindViewHolder(holder: EntityViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int = list.size

        fun getItem(position: Int): E = list[position]

        fun set(position: Int, entity: E) {
            list[position] = entity
        }

        fun add(entity: E) {
            val itemCount = getItemCount()
            for (i in 0 until itemCount) {
                val adapterEntity = getItem(i)
                if (COMPARATOR.compare(adapterEntity, entity) > 0) {
                    list.add(i, entity)
                    notifyItemInserted(i)
                    return
                }
            }
            list.add(itemCount, entity)
            notifyItemInserted(itemCount)
        }

        fun remove(entity: E) {
            val i = list.indexOf(entity)
            if (i >= 0) {
                list.removeAt(i)
                notifyItemRemoved(i)
            }
        }

        fun update(entity: E) {
            if (!entity.isIdDefined()) {
                return
            }
            for (i in 0 until itemCount) {
                val adapterEntity = getItem(i)
                if (adapterEntity.isIdDefined() && entity.getId() == adapterEntity.getId()) {
                    set(i, entity)
                    notifyItemChanged(i)
                    break
                }
            }
        }
    }

    companion object {
        const val ARG_CATEGORY = "category"

        private val COMPARATOR = Comparator<MathEntity> { l, r ->
            l.name.compareTo(r.name)
        }
    }
}
