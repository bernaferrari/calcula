package org.solovyev.android.calculator.view

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.EntryPointAccessors
import org.solovyev.android.calculator.FragmentTab
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.di.AppEntryPoint
import org.solovyev.android.calculator.entities.BaseEntitiesFragment
import org.solovyev.android.calculator.entities.Category

class Tabs(private val activity: AppCompatActivity) {

    private val preferences: SharedPreferences

    private val adapter = TabFragments(activity.supportFragmentManager)
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var defaultSelectedTab = -1

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            activity.applicationContext,
            AppEntryPoint::class.java
        )
        preferences = entryPoint.tabsPreferences()
    }

    fun onCreate() {
        tabLayout = activity.findViewById(R.id.tabs)
        viewPager = activity.findViewById(R.id.viewPager)

        val tabLayoutView = tabLayout ?: return
        val viewPagerView = viewPager ?: return

        val tabs = adapter.count
        if (tabs == 0) {
            tabLayoutView.visibility = View.GONE
            return
        }

        viewPagerView.adapter = adapter
        tabLayoutView.tabMode = if (tabs > 3) TabLayout.MODE_SCROLLABLE else TabLayout.MODE_FIXED
        tabLayoutView.setupWithViewPager(viewPagerView)
        restoreSelectedTab()
    }

    fun addTab(category: Category<*>, tab: FragmentTab) {
        addTab(category, tab, activity.getString(category.title))
    }

    fun addTab(category: Category<*>, tab: FragmentTab, title: CharSequence) {
        val arguments = Bundle(1).apply {
            putString(BaseEntitiesFragment.ARG_CATEGORY, category.getCategoryName())
        }
        addTab(tab.type, arguments, title)
    }

    fun addTab(tab: FragmentTab) {
        addTab(tab.type, null, activity.getString(tab.title))
    }

    fun addTab(
        fragmentClass: Class<out Fragment>,
        fragmentArgs: Bundle?,
        title: CharSequence
    ) {
        adapter.add(TabFragment(fragmentClass, fragmentArgs, title))
    }

    val currentFragment: Fragment?
        get() = viewPager?.let { adapter.getItem(it.currentItem) }

    val currentTab: Int
        get() = viewPager?.currentItem ?: -1

    val tabCount: Int
        get() = adapter.count

    fun selectTab(index: Int) {
        tabLayout?.getTabAt(index)?.select()
    }

    fun setDefaultSelectedTab(defaultSelectedTab: Int) {
        this.defaultSelectedTab = defaultSelectedTab
    }

    fun restoreSelectedTab() {
        val selectedTab = preferences.getInt(makeTabKey(activity), defaultSelectedTab)
        if (selectedTab in 0 until tabCount) {
            selectTab(selectedTab)
        }
    }

    fun onPause() {
        saveSelectedTab()
    }

    private fun saveSelectedTab() {
        val selectedTab = currentTab
        if (selectedTab >= 0) {
            preferences.edit()
                .putInt(makeTabKey(activity), selectedTab)
                .apply()
        }
    }

    private inner class TabFragments(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val list = mutableListOf<TabFragment>()

        override fun getItem(position: Int): Fragment =
            list[position].makeFragment()

        fun add(tabFragment: TabFragment) {
            list.add(tabFragment)
            notifyDataSetChanged()
        }

        override fun getPageTitle(position: Int): CharSequence =
            list[position].title

        override fun getCount(): Int = list.size
    }

    private inner class TabFragment(
        val fragmentClass: Class<out Fragment>,
        val fragmentArgs: Bundle?,
        val title: CharSequence
    ) {
        fun makeFragment(): Fragment =
            Fragment.instantiate(activity, fragmentClass.name, fragmentArgs)
    }

    companion object {
        private fun makeTabKey(activity: Activity): String =
            activity::class.java.simpleName
    }
}
