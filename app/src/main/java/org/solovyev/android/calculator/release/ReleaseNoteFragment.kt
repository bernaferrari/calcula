package org.solovyev.android.calculator.release

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.wizard.WizardFragment

@AndroidEntryPoint
class ReleaseNoteFragment : WizardFragment() {

    private var version: Int = 0

    override fun getViewResId(): Int = R.layout.cpp_release_note_step

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        version = arguments?.getInt(ARG_VERSION, 0) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            findViewById<TextView>(R.id.release_note_title).text =
                getString(R.string.cpp_new_in_version, ReleaseNotes.getReleaseNoteVersion(version))

            findViewById<TextView>(R.id.release_note_message).text =
                HtmlCompat.fromHtml(
                    ReleaseNotes.getReleaseNoteDescription(requireActivity(), version),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
        }
    }

    companion object {
        const val ARG_VERSION = "version"
    }
}
