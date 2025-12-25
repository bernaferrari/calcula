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

import android.app.Application
import android.content.SharedPreferences
import android.os.Handler
import com.google.common.base.Strings
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.solovyev.android.Check
import org.solovyev.android.calculator.Calculator
import org.solovyev.android.calculator.Display
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.ErrorReporter
import org.solovyev.android.calculator.di.AppCoroutineScope
import org.solovyev.android.calculator.di.AppDirectories
import org.solovyev.android.calculator.di.AppDispatchers
import org.solovyev.android.calculator.json.Json
import org.solovyev.android.io.FileSystem
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class History @Inject constructor(
    private val application: Application,
    private val bus: Bus,
    private val handler: Handler,
    private val preferences: SharedPreferences,
    private val editor: Editor,
    private val display: Display,
    private val errorReporter: ErrorReporter,
    private val fileSystem: FileSystem,
    private val directories: AppDirectories,
    private val dispatchers: AppDispatchers,
    private val appScope: AppCoroutineScope
) {

    private val recent = RecentHistory()
    private val saved = mutableListOf<HistoryState>()
    private val whenLoadedRunnables = mutableListOf<Runnable>()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private val _recentEvents = MutableSharedFlow<HistoryEvent>(extraBufferCapacity = 10)
    val recentEvents: SharedFlow<HistoryEvent> = _recentEvents.asSharedFlow()

    private val _savedEvents = MutableSharedFlow<HistoryEvent>(extraBufferCapacity = 10)
    val savedEvents: SharedFlow<HistoryEvent> = _savedEvents.asSharedFlow()

    fun init() {
        Check.isMainThread()
        bus.register(this)
        appScope.launchIO {
            initAsync()
        }
    }

    private suspend fun initAsync() {
        migrateOldHistory()
        val recentStates = tryLoadStates(recentHistoryFile)
        val savedStates = tryLoadStates(savedHistoryFile)
        withContext(dispatchers.main) {
            onLoaded(recentStates, savedStates)
        }
    }

    private fun onLoaded(recentStates: List<HistoryState>, savedStates: List<HistoryState>) {
        Check.isTrue(saved.isEmpty())
        Check.isMainThread()
        val wasEmpty = recent.isEmpty()
        recent.addInitial(recentStates)
        saved.addAll(savedStates)
        if (wasEmpty) {
            editor.onHistoryLoaded(recent)
        } else {
            postRecentWrite()
        }
        _loaded.value = true
        whenLoadedRunnables.forEach { it.run() }
        whenLoadedRunnables.clear()
    }

    private suspend fun tryLoadStates(file: File): List<HistoryState> {
        return try {
            Json.load(file, fileSystem, HistoryState.JSON_CREATOR)
        } catch (e: Exception) {
            when (e) {
                is IOException, is JSONException -> {
                    errorReporter.onException(e)
                    emptyList()
                }
                else -> throw e
            }
        }
    }

    private suspend fun migrateOldHistory() {
        try {
            val xml = preferences.getString(OLD_HISTORY_PREFS_KEY, null)
            if (xml.isNullOrEmpty()) {
                return
            }
            val states = convertOldHistory(xml) ?: return
            val json = Json.toJson(states)
            fileSystem.write(savedHistoryFile, json.toString())
            preferences.edit().remove(OLD_HISTORY_PREFS_KEY).apply()
        } catch (e: Exception) {
            errorReporter.onException(e)
        }
    }

    fun addRecent(state: HistoryState) {
        Check.isMainThread()
        if (recent.isEmpty() && state.isEmpty()) {
            return
        }
        if (recent.add(state)) {
            onRecentChanged(AddedEvent(state, true))
        }
    }

    fun updateSaved(state: HistoryState) {
        Check.isMainThread()
        val i = saved.indexOf(state)
        if (i >= 0) {
            saved[i] = state
            onSavedChanged(UpdatedEvent(state, false))
        } else {
            saved.add(state)
            onSavedChanged(AddedEvent(state, false))
        }
    }

    private fun onRecentChanged(event: HistoryEvent) {
        postRecentWrite()
        bus.post(event)
        appScope.launch(dispatchers.main) {
            _recentEvents.emit(event)
        }
    }

    private fun postRecentWrite() {
        handler.removeCallbacks(writeRecent)
        handler.postDelayed(writeRecent, 5000)
    }

    private fun onSavedChanged(event: HistoryEvent) {
        postSavedWrite()
        bus.post(event)
        appScope.launch(dispatchers.main) {
            _savedEvents.emit(event)
        }
    }

    private fun postSavedWrite() {
        handler.removeCallbacks(writeSaved)
        handler.postDelayed(writeSaved, 500)
    }

    fun getRecent(): List<HistoryState> = getRecent(true)

    private fun getRecent(forUi: Boolean): List<HistoryState> {
        Check.isMainThread()
        val result = mutableListOf<HistoryState>()
        val separator = Engine.Preferences.Output.separator.getPreference(preferences) ?: '\u0000'
        val states = recent.asList()
        val statesCount = states.size
        var streak = 0

        for (i in 1 until statesCount) {
            val olderState = states[i - 1]
            val newerState = states[i]
            val olderText = olderState.editor.getTextString()
            val newerText = newerState.editor.getTextString()
            if (streak >= MAX_INTERMEDIATE_STREAK || !isIntermediate(olderText, newerText, separator)) {
                result.add(0, olderState)
                streak = 0
            } else {
                streak++
            }
        }

        if (statesCount > 0) {
            val state = states[statesCount - 1]
            if (!state.editor.isEmpty() || !forUi) {
                result.add(0, state)
            }
        }
        return result
    }

    fun getSaved(): List<HistoryState> {
        Check.isMainThread()
        return ArrayList(saved)
    }

    fun clearRecent() {
        Check.isMainThread()
        recent.clear()
        onRecentChanged(ClearedEvent(true))
    }

    fun clearSaved() {
        Check.isMainThread()
        saved.clear()
        onSavedChanged(ClearedEvent(false))
    }

    fun undo() {
        val state = recent.undo() ?: return
        applyHistoryState(state)
    }

    fun redo() {
        val state = recent.redo() ?: return
        applyHistoryState(state)
    }

    private fun applyHistoryState(state: HistoryState) {
        editor.setState(state.editor)
        display.setState(state.display)
    }

    fun removeSaved(state: HistoryState) {
        Check.isMainThread()
        saved.remove(state)
        onSavedChanged(RemovedEvent(state, false))
    }

    @Subscribe
    fun onDisplayChanged(e: Display.ChangedEvent) {
        val editorState = editor.state
        val displayState = e.newState
        if (editorState.sequence != displayState.sequence) {
            return
        }
        addRecent(HistoryState.builder(editorState, displayState).build())
    }

    fun runWhenLoaded(runnable: Runnable) {
        Check.isTrue(!_loaded.value)
        whenLoadedRunnables.add(runnable)
    }

    private val writeRecent = Runnable {
        writeHistory(true)
    }

    private val writeSaved = Runnable {
        writeHistory(false)
    }

    private fun writeHistory(recent: Boolean) {
        Check.isMainThread()
        if (!_loaded.value) {
            return
        }
        val states = if (recent) getRecent(false) else getSaved()
        appScope.launchIO {
            val file = if (recent) recentHistoryFile else savedHistoryFile
            val array = Json.toJson(states)
            fileSystem.writeSilently(file, array.toString())
        }
    }

    private val recentHistoryFile: File
        get() = directories.getFile("history-recent.json")

    private val savedHistoryFile: File
        get() = directories.getFile("history-saved.json")

    sealed class HistoryEvent {
        abstract val recent: Boolean
    }

    data class ClearedEvent(override val recent: Boolean) : HistoryEvent()

    abstract class StateEvent(val state: HistoryState, override val recent: Boolean) : HistoryEvent()

    data class RemovedEvent(val historyState: HistoryState, val isRecent: Boolean) :
        StateEvent(historyState, isRecent)

    data class AddedEvent(val historyState: HistoryState, val isRecent: Boolean) :
        StateEvent(historyState, isRecent)

    data class UpdatedEvent(val historyState: HistoryState, val isRecent: Boolean) :
        StateEvent(historyState, isRecent)

    companion object {
        const val OLD_HISTORY_PREFS_KEY = "org.solovyev.android.calculator.CalculatorModel_history"
        private const val MAX_INTERMEDIATE_STREAK = 5

        @JvmStatic
        fun convertOldHistory(xml: String): List<HistoryState>? {
            val history = OldHistory.fromXml(xml) ?: return null
            val states = mutableListOf<HistoryState>()
            for (state in history.getItems()) {
                val oldEditor = state.getEditorState()
                val oldDisplay = state.getDisplayState()
                val editorText = oldEditor.getText()
                val editor = EditorState.create(Strings.nullToEmpty(editorText), oldEditor.getCursorPosition())
                val display = DisplayState.createValid(
                    oldDisplay.getJsclOperation(),
                    null,
                    Strings.nullToEmpty(oldDisplay.getEditorState().getText()),
                    Calculator.NO_SEQUENCE
                )
                states.add(
                    HistoryState.builder(editor, display)
                        .withTime(state.getTime())
                        .withComment(state.getComment())
                        .build()
                )
            }
            return states
        }

        private fun isIntermediate(olderText: String, newerText: String, separator: Char): Boolean {
            if (olderText.isEmpty()) {
                return true
            }
            if (newerText.isEmpty()) {
                return false
            }
            val trimmedOlder = trimGroupingSeparators(olderText, separator)
            val trimmedNewer = trimGroupingSeparators(newerText, separator)

            val diff = trimmedNewer.length - trimmedOlder.length
            return when {
                diff >= 1 -> trimmedNewer.startsWith(trimmedOlder)
                diff <= 1 -> trimmedOlder.startsWith(trimmedNewer)
                diff == 0 -> trimmedOlder == trimmedNewer
                else -> false
            }
        }

        @JvmStatic
        fun trimGroupingSeparators(text: String, separator: Char): String {
            if (separator.code == 0) {
                return text
            }
            val sb = StringBuilder(text.length)
            for (i in text.indices) {
                if (i == 0 || i == text.length - 1) {
                    sb.append(text[i])
                    continue
                }
                if (text[i - 1].isDigit() && text[i] == separator && text[i + 1].isDigit()) {
                    continue
                }
                sb.append(text[i])
            }
            return sb.toString()
        }
    }
}
