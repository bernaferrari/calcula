package org.solovyev.android.calculator.di

import android.graphics.Typeface
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.solovyev.android.calculator.ActivityLauncher
import org.solovyev.android.calculator.Calculator
import org.solovyev.android.calculator.Display
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Keyboard
import org.solovyev.android.calculator.memory.Memory

/**
 * Hilt EntryPoint for classes that cannot use @AndroidEntryPoint
 * (e.g., plain Kotlin classes instantiated manually).
 *
 * Use EntryPointAccessors.fromApplication(context, AppEntryPoint::class.java)
 * to get an instance.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun keyboard(): Keyboard
    fun editor(): Editor
    fun display(): Display
    fun engine(): Engine
    fun calculator(): Calculator
    fun launcher(): ActivityLauncher
    fun memory(): Memory
    fun typeface(): Typeface
    fun appPreferences(): AppPreferences

}
