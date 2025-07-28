package com.bernaferrari.calculator.di

import android.content.Context
import androidx.room.Room
import com.bernaferrari.calculator.data.local.dao.CalculationHistoryDao
import com.bernaferrari.calculator.data.source.local.CalculatorDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideCalculatorDatabase(
        @ApplicationContext context: Context,
    ): CalculatorDatabase =
        Room
            .databaseBuilder(
                context.applicationContext,
                CalculatorDatabase::class.java,
                "Calculator.db",
            ).fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideCalculationHistoryDao(database: CalculatorDatabase): CalculationHistoryDao = database.calculationHistoryDao()
}
