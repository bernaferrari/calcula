package org.solovyev.android.calculator.release

import android.content.Context
import android.util.SparseArray
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.R
import org.solovyev.common.text.Strings

object ReleaseNotes {

    private val map = SparseArray<ReleaseNote>().apply {
        put(143, ReleaseNote("2.1.4", R.string.cpp_release_notes_143))
        put(148, ReleaseNote("2.2.1", R.string.cpp_release_notes_148))
        put(150, ReleaseNote("2.2.2", R.string.cpp_release_notes_150))
        put(152, ReleaseNote("2.2.3", R.string.cpp_release_notes_152))
    }

    @JvmStatic
    fun getReleaseNotes(context: Context): String {
        return getReleaseNotesString(context, 0)
    }

    @JvmStatic
    fun getReleaseNoteVersion(version: Int): String {
        val releaseNote = map.get(version)
        return releaseNote?.versionName ?: version.toString()
    }

    @JvmStatic
    fun getReleaseNoteDescription(context: Context, version: Int): String {
        val releaseNote = map.get(version)
        return if (releaseNote == null) "" else getDescription(context, releaseNote.description)
    }

    @JvmStatic
    fun getReleaseNotesString(context: Context, minVersion: Int): String = buildString {
        val releaseNotesForTitle = context.getString(R.string.c_release_notes_for_title)
        val currentVersionCode = App.getAppVersionCode(context)

        var first = true
        for (versionCode in currentVersionCode downTo minVersion) {
            val releaseNote = map.get(versionCode) ?: continue

            if (!first) {
                append("<br/><br/>")
            } else {
                first = false
            }

            val descriptionHtml = getDescription(context, releaseNote.description)
            append("<b>")
                .append(releaseNotesForTitle)
                .append(releaseNote.versionName)
                .append("</b><br/><br/>")
            append(descriptionHtml)
        }
    }

    private fun getDescription(context: Context, description: Int): String {
        return context.resources.getString(description).replace("\n", "<br/>")
    }

    @JvmStatic
    fun getReleaseNotesVersions(context: Context, minVersion: Int): List<Int> {
        val releaseNotes = mutableListOf<Int>()
        val currentVersionCode = App.getAppVersionCode(context)

        for (versionCode in currentVersionCode downTo minVersion) {
            if (versionCode == ChooseThemeReleaseNoteStep.VERSION_CODE) {
                releaseNotes.add(ChooseThemeReleaseNoteStep.VERSION_CODE)
            }

            val releaseNote = map.get(versionCode) ?: continue
            val description = context.getString(releaseNote.description)
            if (!Strings.isEmpty(description)) {
                releaseNotes.add(versionCode)
            }
        }

        return releaseNotes
    }

    @JvmStatic
    fun hasReleaseNotes(context: Context, minVersion: Int): Boolean {
        val currentVersionCode = App.getAppVersionCode(context)

        for (versionCode in currentVersionCode downTo minVersion) {
            if (versionCode == ChooseThemeReleaseNoteStep.VERSION_CODE) {
                return true
            }

            val releaseNote = map.get(versionCode) ?: continue
            if (!Strings.isEmpty(context.getString(releaseNote.description))) {
                return true
            }
        }

        return false
    }
}
