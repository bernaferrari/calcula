package org.solovyev.android.calculator.preferences

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.language.Languages
import org.solovyev.android.checkout.ActivityCheckout
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.Checkout
import javax.inject.Inject

@AndroidEntryPoint
open class PreferencesActivity : BaseActivity(R.layout.activity_empty, R.string.cpp_settings) {

    @Inject
    lateinit var billing: Billing

    lateinit var checkout: ActivityCheckout
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val preferenceTitle = intent.getIntExtra(EXTRA_PREFERENCE_TITLE, 0)
        if (preferenceTitle != 0) {
            setTitle(preferenceTitle)
        }

        if (savedInstanceState == null) {
            val preference = intent.getIntExtra(EXTRA_PREFERENCE, R.xml.preferences)
            supportFragmentManager.beginTransaction()
                .add(R.id.main, PreferencesFragment.create(preference))
                .commit()
        }

        checkout = Checkout.forActivity(this, billing)
        checkout.start()
    }

    override fun onDestroy() {
        checkout.stop()
        super.onDestroy()
    }

    data class PrefDef(
        val id: String,
        @StringRes val title: Int
    )

    class Dialog : PreferencesActivity()

    companion object {
        const val EXTRA_PREFERENCE = "preference"
        const val EXTRA_PREFERENCE_TITLE = "preference-title"

        private val preferenceDefs = SparseArray<PrefDef>().apply {
            append(R.xml.preferences, PrefDef("screen-main", R.string.cpp_settings))
            append(R.xml.preferences_number_format, PrefDef("screen-number-format", R.string.cpp_number_format))
            append(R.xml.preferences_appearance, PrefDef("screen-appearance", R.string.cpp_appearance))
            append(R.xml.preferences_other, PrefDef("screen-other", R.string.cpp_other))
            append(R.xml.preferences_onscreen, PrefDef("screen-onscreen", R.string.cpp_floating_calculator))
            append(R.xml.preferences_widget, PrefDef("screen-widget", R.string.cpp_widget))
        }

        fun getPreferenceDefs(): SparseArray<PrefDef> = preferenceDefs

        fun getClass(context: Context): Class<out PreferencesActivity> {
            return if (App.isTablet(context)) Dialog::class.java else PreferencesActivity::class.java
        }

        fun makeIntent(
            context: Context,
            @XmlRes preference: Int,
            @StringRes title: Int
        ): Intent {
            val intent = Intent(context, getClass(context))
            intent.putExtra(EXTRA_PREFERENCE, preference)
            if (title != 0) {
                intent.putExtra(EXTRA_PREFERENCE_TITLE, title)
            }
            return intent
        }
    }
}
