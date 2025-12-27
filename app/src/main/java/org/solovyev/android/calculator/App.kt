package org.solovyev.android.calculator

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.annotation.ColorInt
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.atomicfu.atomic
import org.solovyev.android.Check
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.floating.FloatingCalculatorService

object App {

    const val TAG = "C++"

    @Volatile
    private lateinit var application: Application

    val context: Context
        get() = application

    @Volatile
    lateinit var appPreferences: AppPreferences

    fun init(application: Application, appPreferences: AppPreferences) {
        App.application = application
        App.appPreferences = appPreferences
    }

    fun getTheme(): Preferences.Gui.Theme {
        return appPreferences.settings.getThemeBlocking()
    }

    fun getWidgetTheme(): Preferences.SimpleTheme {
        return appPreferences.settings.getWidgetThemeBlocking()
    }

    fun getThemeFor(context: Context): Preferences.Gui.Theme {
        return if (isFloatingCalculator(context)) {
            val onscreenTheme = appPreferences.settings.getOnscreenThemeBlocking()
            val appTheme = appPreferences.settings.getThemeBlocking()
            onscreenTheme.resolveThemeFor(appTheme).getAppTheme()!!
        } else {
            getTheme()
        }
    }

    private tailrec fun unwrap(context: Context): Context {
        return if (context is ContextThemeWrapper) {
            unwrap(context.baseContext)
        } else {
            context
        }
    }

    fun showDialog(
        dialogFragment: DialogFragment,
        fragmentTag: String,
        fm: FragmentManager
    ) {
        val ft = fm.beginTransaction()

        val prev = fm.findFragmentByTag(fragmentTag)
        if (prev != null) {
            ft.remove(prev)
        }

        // Create and show the dialog.
        dialogFragment.show(ft, fragmentTag)
    }

    fun colorString(s: String, @ColorInt color: Int): SpannableString {
        val spannable = SpannableString(s)
        spannable.setSpan(ForegroundColorSpan(color), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    fun unspan(spannable: CharSequence): String {
        return spannable.toString()
    }

    private val nextViewId = atomic(1)

    fun generateViewId(): Int {
        Check.isMainThread()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId()
        } else {
            // Backwards compatible version, as given by fantouchx@gmail.com in
            // http://stackoverflow.com/questions/6790623/#21000252
            while (true) {
                val result = nextViewId.value
                // aapt-generated IDs have the high byte non-zero. Clamp to the
                // range below that.
                var newValue = result + 1
                if (newValue > 0x00FFFFFF) {
                    newValue = 1
                }
                if (nextViewId.compareAndSet(result, newValue)) {
                    return result
                }
            }
        }
    }

    fun find(tokens: List<String>, text: String, position: Int): String? {
        return tokens.firstOrNull { token ->
            text.startsWith(token, position)
        }
    }

    fun cast(fragment: Fragment): Application {
        return cast(fragment.requireActivity())
    }

    fun cast(context: Context): Application {
        return context.applicationContext as Application
    }

    fun hideIme(fragment: DialogFragment) {
        fragment.dialog?.currentFocus?.let { focusedView ->
            hideIme(focusedView)
        }
    }

    fun hideIme(view: View) {
        view.windowToken?.let { token ->
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(token, 0)
        }
    }

    fun showSystemPermissionSettings(activity: Activity, action: String) {
        try {
            val intent = Intent(action).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Failed to show permission settings for $action", e)
        }
    }

    fun isTablet(context: Context): Boolean {
        return context.resources.getBoolean(R.bool.cpp_tablet)
    }

    internal fun isFloatingCalculator(context: Context): Boolean {
        return unwrap(context) is FloatingCalculatorService
    }

    fun getAppVersionCode(context: Context): Int {
        return try {
            getAppVersionCode(context, context.packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            Check.shouldNotHappen()
            0
        }
    }

    fun getAppVersionCode(context: Context, appPackageName: String): Int {
        val info = context.packageManager.getPackageInfo(appPackageName, 0)
        return PackageInfoCompat.getLongVersionCode(info).toInt()
    }

    fun isUiThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    fun restartActivity(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }

    /**
     * Method runs through view and all it's children recursively and process them via viewProcessor
     *
     * @param view          parent view to be processed, if view is ViewGroup then all it's children will be processed
     * @param viewProcessor object which processes views
     */
    fun processViews(view: View, viewProcessor: ViewProcessor<View>) {
        processViewsOfType0(view, null, viewProcessor)
    }

    private fun <T> processViewsOfType0(
        view: View,
        viewClass: Class<T>?,
        viewProcessor: ViewProcessor<T>
    ) {
        when (view) {
            is ViewGroup -> {
                if (viewClass == null || viewClass.isAssignableFrom(ViewGroup::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    viewProcessor.process(view as T)
                }

                for (index in 0 until view.childCount) {
                    processViewsOfType0(view.getChildAt(index), viewClass, viewProcessor)
                }
            }

            else -> {
                if (viewClass == null || viewClass.isAssignableFrom(view.javaClass)) {
                    @Suppress("UNCHECKED_CAST")
                    viewProcessor.process(view as T)
                }
            }
        }
    }

    fun <T> processViewsOfType(
        view: View,
        viewClass: Class<T>,
        viewProcessor: ViewProcessor<T>
    ) {
        processViewsOfType0(view, viewClass, viewProcessor)
    }

    fun <T> find(view: View, viewClass: Class<T>): T? {
        return find0(view, viewClass)
    }

    private fun <T> find0(view: View, viewClass: Class<T>): T? {
        return when (view) {
            is ViewGroup -> {
                if (viewClass.isAssignableFrom(ViewGroup::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return view as T
                }

                for (index in 0 until view.childCount) {
                    val child = find0(view.getChildAt(index), viewClass)
                    if (child != null) {
                        return child
                    }
                }
                null
            }

            else -> {
                if (viewClass.isAssignableFrom(view.javaClass)) {
                    @Suppress("UNCHECKED_CAST")
                    view as T
                } else {
                    null
                }
            }
        }
    }


    fun interface ViewProcessor<V> {
        fun process(view: V)
    }

    fun getScreenOrientation(activity: Activity): Int {
        return activity.resources.configuration.orientation
    }

    fun addIntentFlags(intent: Intent, detached: Boolean, context: Context) {
        var flags = 0
        if (context !is Activity) {
            flags = flags or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (detached) {
            flags = flags or Intent.FLAG_ACTIVITY_NO_HISTORY
        }
        intent.flags = flags
    }

    fun enableComponent(
        context: Context,
        componentClass: Class<*>,
        enable: Boolean
    ) {
        val pm = context.packageManager

        val componentState = if (enable) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        pm.setComponentEnabledSetting(
            ComponentName(context, componentClass),
            componentState,
            PackageManager.DONT_KILL_APP
        )
    }

    fun toPixels(dm: DisplayMetrics, dps: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, dm).toInt()
    }

    fun toPixels(context: Context, dps: Float): Int {
        return toPixels(context.resources.displayMetrics, dps)
    }

    fun toPixels(view: View, dps: Float): Int {
        return toPixels(view.context, dps)
    }
}
