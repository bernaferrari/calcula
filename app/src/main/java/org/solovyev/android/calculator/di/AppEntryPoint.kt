package org.solovyev.android.calculator.di

import android.content.SharedPreferences
import android.graphics.Typeface
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.solovyev.android.calculator.ActivityLauncher
import org.solovyev.android.calculator.AppModule
import org.solovyev.android.calculator.Calculator
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.Keyboard
import org.solovyev.android.calculator.memory.Memory
import javax.inject.Named

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
    fun preferences(): SharedPreferences
    fun keyboard(): Keyboard
    fun editor(): Editor
    fun calculator(): Calculator
    fun launcher(): ActivityLauncher
    fun memory(): Memory
    fun typeface(): Typeface

    // Named preferences
    @Named(AppModule.PREFS_TABS)
    fun tabsPreferences(): SharedPreferences

    @Named(AppModule.PREFS_FLOATING)
    fun floatingPreferences(): SharedPreferences
}
