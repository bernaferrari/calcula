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

import android.app.Application
import android.content.SharedPreferences
import android.os.Handler
import androidx.annotation.StringRes
import com.squareup.otto.Bus
import org.solovyev.android.Check
import org.solovyev.android.calculator.EntitiesRegistry
import org.solovyev.android.calculator.ErrorReporter
import org.solovyev.android.calculator.di.AppCoroutineScope
import org.solovyev.android.calculator.di.AppDirectories
import org.solovyev.android.calculator.json.Json
import org.solovyev.android.calculator.json.Jsonable
import org.solovyev.android.io.FileSaver
import org.solovyev.android.io.FileSystem
import org.solovyev.common.math.MathEntity
import org.solovyev.common.math.MathRegistry
import java.io.File
import java.io.IOException
import javax.inject.Inject

abstract class BaseEntitiesRegistry<T : MathEntity>(
    private val mathRegistry: MathRegistry<T>
) : EntitiesRegistry<T> {

    protected val lock: Any = this

    @Inject
    lateinit var handler: Handler

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var application: Application

    @Inject
    lateinit var bus: Bus

    @Inject
    lateinit var errorReporter: ErrorReporter

    @Inject
    lateinit var fileSystem: FileSystem

    @Inject
    lateinit var directories: AppDirectories

    @Inject
    lateinit var appScope: AppCoroutineScope

    private val descriptions = mutableMapOf<String, Int>()
    private val writeTask = WriteTask()

    // synchronized on lock
    @Volatile
    private var initialized = false

    protected fun addDescription(name: String, @StringRes description: Int) {
        descriptions[name] = description
    }

    override fun getDescription(name: String): String? {
        val description = descriptions[name]
        if (description == null || description == 0) {
            return null
        }
        return application.resources.getString(description)
    }

    final override fun init() {
        try {
            mathRegistry.init()
            onInit()
        } finally {
            setInitialized()
        }
    }

    protected open fun onInit() {
    }

    protected fun <E> loadEntities(creator: Json.Creator<E>): List<E> {
        val file = getEntitiesFile() ?: return emptyList()
        return try {
            Json.load(file, fileSystem, creator)
        } catch (e: IOException) {
            errorReporter.onException(e)
            emptyList()
        } catch (e: org.json.JSONException) {
            errorReporter.onException(e)
            emptyList()
        }
    }

    private fun setInitialized() {
        synchronized(lock) {
            Check.isTrue(!initialized)
            initialized = true
        }
    }

    fun isInitialized(): Boolean {
        synchronized(lock) {
            return initialized
        }
    }

    override fun save() {
        handler.removeCallbacks(writeTask)
        handler.postDelayed(writeTask, 500)
    }

    override fun getEntities(): List<T> = mathRegistry.getEntities()

    override fun getSystemEntities(): List<T> = mathRegistry.getSystemEntities()

    override fun addOrUpdate(entity: T): T {
        val result = mathRegistry.addOrUpdate(entity)
        if (!result.isSystem() && isInitialized()) {
            save()
        }
        return result
    }

    protected fun addSafely(entity: T): T? {
        return try {
            addOrUpdate(entity)
        } catch (e: Exception) {
            errorReporter.onException(e)
            null
        }
    }

    override fun remove(entity: T) {
        mathRegistry.remove(entity)
        save()
    }

    override fun getNames(): List<String> = mathRegistry.getNames()

    override fun contains(name: String): Boolean = mathRegistry.contains(name)

    override fun get(name: String): T? = mathRegistry.get(name)

    override fun getById(id: Int): T? = mathRegistry.getById(id)

    protected abstract fun toJsonable(entity: T): Jsonable?

    protected abstract fun getEntitiesFile(): File?

    private inner class WriteTask : Runnable {
        override fun run() {
            Check.isMainThread()
            val file = getEntitiesFile() ?: return

            val entities = mutableListOf<Jsonable>()
            for (entity in getEntities()) {
                if (entity.isSystem()) {
                    continue
                }
                val jsonable = toJsonable(entity)
                if (jsonable != null) {
                    entities.add(jsonable)
                }
            }

            appScope.launchIO {
                val array = Json.toJson(entities)
                try {
                    FileSaver.save(file, array.toString())
                } catch (e: IOException) {
                    errorReporter.onException(e)
                }
            }
        }
    }
}
