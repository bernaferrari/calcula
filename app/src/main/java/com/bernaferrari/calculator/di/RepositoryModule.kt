package com.bernaferrari.calculator.di

import com.bernaferrari.calculator.data.repository.CalculatorRepositoryImpl
import com.bernaferrari.calculator.data.repository.PreferencesRepositoryImpl
import com.bernaferrari.calculator.domain.calculator.ExpressionEvaluator
import com.bernaferrari.calculator.domain.model.AngleUnit
import com.bernaferrari.calculator.domain.repository.CalculatorRepository
import com.bernaferrari.calculator.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCalculatorRepository(calculatorRepositoryImpl: CalculatorRepositoryImpl): CalculatorRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(preferencesRepositoryImpl: PreferencesRepositoryImpl): PreferencesRepository

    companion object {
        @Provides
        @Singleton
        fun provideExpressionEvaluator(): ExpressionEvaluator = 
            ExpressionEvaluator(AngleUnit.RADIANS, precision = 34)
    }
}
