package org.solovyev.android.calculator.preferences

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import dagger.hilt.android.AndroidEntryPoint
import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.NumeralBase
import org.solovyev.android.calculator.ActivityLauncher
import org.solovyev.android.calculator.AdView
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.Preferences.Gui.Theme
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.feedback.FeedbackReporter
import org.solovyev.android.calculator.language.Language
import org.solovyev.android.calculator.language.Languages
import org.solovyev.android.calculator.wizard.CalculatorWizards
import org.solovyev.android.checkout.BillingRequests
import org.solovyev.android.checkout.Checkout
import org.solovyev.android.checkout.ProductTypes
import org.solovyev.android.checkout.RequestListener
import org.solovyev.android.prefs.StringPreference
import org.solovyev.android.wizard.WizardUi
import org.solovyev.android.wizard.Wizards
import org.solovyev.common.text.CharacterMapper
import javax.inject.Inject

@AndroidEntryPoint
class PreferencesFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var adView: AdView? = null

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var languages: Languages

    @Inject
    lateinit var wizards: Wizards

    @Inject
    lateinit var engine: JsclMathEngine

    @Inject
    lateinit var feedbackReporter: FeedbackReporter

    @Inject
    lateinit var launcher: ActivityLauncher

    @Inject
    lateinit var bus: Bus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences.registerOnSharedPreferenceChangeListener(this)
        bus.register(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val preferencesResId = requireArguments().getInt(ARG_PREFERENCES)
        addPreferencesFromResource(preferencesResId)
    }

    private fun setPreferenceIntent(xml: Int, def: PreferencesActivity.PrefDef) {
        val preference = findPreference<Preference>(def.id) ?: return
        val context = requireActivity()
        val intent = Intent(context, PreferencesActivity.getClass(context)).apply {
            putExtra(PreferencesActivity.EXTRA_PREFERENCE, xml)
            putExtra(PreferencesActivity.EXTRA_PREFERENCE_TITLE, def.title)
        }
        preference.intent = intent
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        val fragmentTag = "fragment:${preference.key}"
        if (parentFragmentManager.findFragmentByTag(fragmentTag) != null) return

        if (preference is PrecisionPreference) {
            val f = PrecisionPreference.Dialog()
            @Suppress("DEPRECATION")
            f.setTargetFragment(this, 0)
            f.show(parentFragmentManager, fragmentTag)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preference = requireArguments().getInt(ARG_PREFERENCES)
        when (preference) {
            R.xml.preferences -> {
                prepareScreens()
                prepareIntroduction()
                prepareReportBug()
                prepareAbout()
                prepareSupportProject()
                prepareMode()
                prepareAngles()
                prepareRadix()
            }
            R.xml.preferences_number_format -> {
                prepareListPreference(Engine.Preferences.Output.notation, Engine.Notation::class.java)
                preparePrecisionPreference()
                prepareSeparatorPreference()
                prepareNumberFormatExamplesPreference()
            }
            R.xml.preferences_onscreen -> {
                updateFloatingCalculatorPreferences()
            }
        }

        prepareLanguagePreference(preference)
        prepareThemePreference(preference)

        getCheckout().whenReady(object : Checkout.EmptyListener() {
            override fun onReady(requests: BillingRequests) {
                requests.isPurchased(ProductTypes.IN_APP, "ad_free", object : RequestListener<Boolean> {
                    override fun onSuccess(purchased: Boolean) {
                        findPreference<Preference>("prefs.supportProject")?.apply {
                            isEnabled = !purchased
                            isSelectable = !purchased
                        }
                        onShowAd(!purchased)
                    }

                    override fun onError(i: Int, e: Exception) {
                        onShowAd(false)
                    }
                })
            }
        })
    }

    private fun prepareReportBug() {
        findPreference<Preference>("prefs.reportBug")?.setOnPreferenceClickListener {
            feedbackReporter.report()
            true
        }
    }

    private fun prepareSupportProject() {
        findPreference<Preference>("prefs.supportProject")?.apply {
            isEnabled = false
            isSelectable = false
            setOnPreferenceClickListener {
                startActivity(Intent(activity, PurchaseDialogActivity::class.java))
                true
            }
        }
    }

    private fun prepareScreens() {
        val preferences = PreferencesActivity.getPreferenceDefs()
        for (i in 0 until preferences.size()) {
            setPreferenceIntent(preferences.keyAt(i), preferences.valueAt(i))
        }
    }

    private fun prepareIntroduction() {
        findPreference<Preference>("prefs.introduction")?.setOnPreferenceClickListener {
            WizardUi.startWizard(wizards, CalculatorWizards.DEFAULT_WIZARD_FLOW, requireActivity())
            true
        }
    }

    private fun prepareAbout() {
        findPreference<Preference>("prefs.about")?.setOnPreferenceClickListener {
            launcher.showAbout()
            true
        }
    }

    private fun prepareNumberFormatExamplesPreference() {
        findPreference<NumberFormatExamplesPreference>("numberFormat.examples")?.update(engine)
    }

    private fun prepareSeparatorPreference() {
        findPreference<ListPreference>(Engine.Preferences.Output.separator.key)?.apply {
            val currentSeparator = Engine.Preferences.Output.separator.getPreference(preferences) ?: '\u0000'
            summary = getString(separatorName(currentSeparator))
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                summary = getString(separatorName(CharacterMapper.parseValue(newValue.toString())))
                true
            }
        }
    }

    private fun separatorName(separator: Char): Int {
        return when (separator) {
            '\'' -> R.string.cpp_thousands_separator_apostrophe
            ' ' -> R.string.cpp_thousands_separator_space
            0.toChar() -> R.string.cpp_thousands_separator_no
            else -> R.string.cpp_thousands_separator_no
        }
    }

    private fun preparePrecisionPreference() {
        findPreference<PrecisionPreference>(Engine.Preferences.Output.precision.key)?.apply {
            summary = Engine.Preferences.Output.precision.getPreference(preferences).toString()
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                summary = newValue.toString()
                true
            }
        }
    }

    private fun <E> prepareListPreference(p: StringPreference<E>, type: Class<E>)
            where E : Enum<E>, E : PreferenceEntry {
        val preference = findPreference<ListPreference>(p.key) ?: return
        val entries = type.enumConstants ?: return
        val activity = requireActivity()
        populate(preference, entries.toList())
        preference.summary = p.getPreference(preferences)?.getName(activity)
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            for (entry in entries) {
                if (entry.id == newValue) {
                    preference.summary = entry.getName(activity)
                    break
                }
            }
            true
        }
    }

    private fun prepareMode() {
        findPreference<ListPreference>(Preferences.Gui.mode.key)?.apply {
            summary = Preferences.Gui.getMode(preferences).name
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                summary = Preferences.Gui.Mode.valueOf(newValue.toString()).name
                true
            }
        }
    }

    private fun prepareAngles() {
        findPreference<ListPreference>(Engine.Preferences.angleUnit.key)?.apply {
            val currentAngleUnit = Engine.Preferences.angleUnit.getPreference(preferences)
            summary = getString(Engine.Preferences.angleUnitName(currentAngleUnit ?: AngleUnit.deg))
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                summary = getString(Engine.Preferences.angleUnitName(AngleUnit.valueOf(newValue.toString())))
                true
            }
        }
    }

    private fun prepareRadix() {
        findPreference<ListPreference>(Engine.Preferences.numeralBase.key)?.apply {
            val currentNumeralBase = Engine.Preferences.numeralBase.getPreference(preferences)
            summary = getString(Engine.Preferences.numeralBaseName(currentNumeralBase ?: NumeralBase.dec))
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                summary = getString(Engine.Preferences.numeralBaseName(NumeralBase.valueOf(newValue.toString())))
                true
            }
        }
    }

    private fun prepareThemePreference(preference: Int) {
        if (preference != R.xml.preferences_appearance) return

        findPreference<ListPreference>(Preferences.Gui.theme.key)?.apply {
            val context = requireActivity()
            populate(
                this,
                listOf(
                    Theme.material_theme,
                    Theme.material_black_theme,
                    Theme.material_light_theme,
                    Theme.metro_blue_theme,
                    Theme.metro_green_theme,
                    Theme.metro_purple_theme
                )
            )
            summary = Preferences.Gui.getTheme(preferences).getName(context)
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val newTheme = Theme.valueOf(newValue.toString())
                summary = newTheme.getName(context)
                true
            }
        }
    }

    private fun prepareLanguagePreference(preference: Int) {
        if (preference != R.xml.preferences_appearance) return

        findPreference<ListPreference>(Preferences.Gui.language.key)?.apply {
            populate(this, languages.getList())
            summary = languages.getCurrent().getName(requireActivity())
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val l = languages.get(newValue.toString())
                summary = l.getName(requireActivity())
                true
            }
        }
    }

    private fun getCheckout(): Checkout {
        return (requireActivity() as PreferencesActivity).checkout
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String?) {
        key?.let {
            if (Preferences.Onscreen.showAppIcon.isSameKey(it)) {
                updateFloatingCalculatorPreferences()
            }
        }
    }

    private fun updateFloatingCalculatorPreferences() {
        findPreference<Preference>(Preferences.Onscreen.theme.key)?.apply {
            isEnabled = Preferences.Onscreen.showAppIcon.getPreference(preferences) ?: false
        }
    }

    @Subscribe
    fun onEngineChanged(e: Engine.ChangedEvent) {
        prepareNumberFormatExamplesPreference()
    }

    override fun onResume() {
        super.onResume()
        adView?.resume()
    }

    override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    override fun onDestroyView() {
        adView?.destroy()
        super.onDestroyView()
    }

    override fun onDestroy() {
        bus.unregister(this)
        preferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    protected fun onShowAd(show: Boolean) {
        val root = view ?: return
        if (root !is ViewGroup) return

        if (show) {
            if (adView != null) return
            adView = AdView(requireActivity()).apply {
                show()
                root.addView(this)
            }
        } else {
            adView?.let {
                root.removeView(it)
                it.hide()
            }
            adView = null
        }
    }

    companion object {
        private const val ARG_PREFERENCES = "preferences"

        fun create(preferences: Int): PreferencesFragment {
            return PreferencesFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PREFERENCES, preferences)
                }
            }
        }

        private fun populate(preference: ListPreference, vararg entries: PreferenceEntry) {
            populate(preference, entries.toList())
        }

        private fun populate(preference: ListPreference, entries: List<PreferenceEntry>) {
            val size = entries.size
            val e = Array<CharSequence>(size) { "" }
            val v = Array<CharSequence>(size) { "" }
            val context = preference.context

            for (i in 0 until size) {
                val entry = entries[i]
                e[i] = entry.getName(context)
                v[i] = entry.id
            }

            preference.entries = e
            preference.entryValues = v
        }
    }
}
