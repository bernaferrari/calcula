package org.solovyev.android.calculator.ui.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.solovyev.android.calculator.AppInfo
import org.solovyev.android.calculator.ResourceProvider
import org.solovyev.android.calculator.ui.*
import org.jetbrains.compose.resources.StringResource

class AboutViewModel(
    private val resourceProvider: ResourceProvider,
    private val appInfo: AppInfo
) : ViewModel() {

    private val _state = MutableStateFlow(AboutScreenData())
    val state: StateFlow<AboutScreenData> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val notes = buildString {
                appendReleaseNote(152, Res.string.cpp_release_notes_152)
                appendReleaseNote(150, Res.string.cpp_release_notes_150)
                appendReleaseNote(148, Res.string.cpp_release_notes_148)
                appendReleaseNote(143, Res.string.cpp_release_notes_143)
            }
            _state.value = AboutScreenData(
                versionName = appInfo.versionName,
                releaseNotesContent = notes
            )
        }
    }

    private suspend fun StringBuilder.appendReleaseNote(version: Int, resId: StringResource) {
        if (isNotEmpty()) {
            append("\n\n")
        }
        val title = getString(Res.string.c_release_notes_for_title)
        append(title).append(getVersionName(version)).append("\n\n")
        append(getString(resId))
    }

    private fun getVersionName(version: Int): String {
        return when (version) {
            152 -> "2.2.3"
            150 -> "2.2.2"
            148 -> "2.2.1"
            143 -> "2.1.4"
            else -> version.toString()
        }
    }
}
