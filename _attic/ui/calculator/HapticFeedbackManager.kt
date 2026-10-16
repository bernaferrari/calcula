package com.bernaferrari.calculator.ui.calculator

import androidx.compose.runtime.*
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.bernaferrari.calculator.domain.model.ButtonFunction
import com.bernaferrari.calculator.domain.model.ButtonType
import kotlinx.coroutines.*

/**
 * Advanced haptic feedback manager that provides contextually appropriate
 * tactile feedback for different calculator interactions.
 */
class HapticFeedbackManager(
    private val hapticFeedback: HapticFeedback,
    private val scope: CoroutineScope
) {
    var isEnabled = true
    
    /**
     * Provides haptic feedback for number button presses
     * Light, quick feedback for typing numbers
     */
    fun numberTap() {
        if (!isEnabled) return
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Provides haptic feedback for basic operator buttons (+, -, ×, ÷)
     * Medium intensity for important operations
     */
    fun operatorTap() {
        if (!isEnabled) return
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    /**
     * Provides haptic feedback for scientific function buttons
     * Distinctive pattern for advanced functions
     */
    fun functionTap() {
        if (!isEnabled) return
        scope.launch {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(50)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    /**
     * Provides haptic feedback for equals button
     * Strong, satisfying feedback for calculation completion
     */
    fun equalsTap() {
        if (!isEnabled) return
        scope.launch {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(100)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    /**
     * Provides haptic feedback for memory operations
     * Unique pattern for memory functions
     */
    fun memoryTap() {
        if (!isEnabled) return
        scope.launch {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(30)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(30)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    /**
     * Provides haptic feedback for delete/backspace operations
     * Distinctive "removing" pattern
     */
    fun deleteTap() {
        if (!isEnabled) return
        scope.launch {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(30)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    /**
     * Provides haptic feedback for clear operations
     * Strong feedback for destructive action
     */
    fun clearTap() {
        if (!isEnabled) return
        scope.launch {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(50)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    /**
     * Provides haptic feedback for secondary function activation
     * Special pattern for drag-up gestures
     */
    fun secondaryFunctionActivation() {
        if (!isEnabled) return
        scope.launch {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(40)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(40)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    /**
     * Provides haptic feedback when dragging reaches secondary function threshold
     */
    fun secondaryFunctionPreview() {
        if (!isEnabled) return
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Provides haptic feedback for undo/redo gestures
     */
    fun undoRedo() {
        if (!isEnabled) return
        scope.launch {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(60)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(60)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    /**
     * Provides haptic feedback for calculation errors
     * Warning pattern for errors
     */
    fun error() {
        if (!isEnabled) return
        scope.launch {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(100)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(100)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    /**
     * Provides haptic feedback for flying text animations
     * Light feedback when text lands
     */
    fun flyingTextLand(isSpecialConstant: Boolean = false) {
        if (!isEnabled) return
        if (isSpecialConstant) {
            // Special pattern for π, e, etc.
            scope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(25)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    /**
     * Provides haptic feedback for long press actions (like step-by-step)
     */
    fun longPressAction() {
        if (!isEnabled) return
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    /**
     * Provides haptic feedback for suggestion taps
     * Light, pleasant feedback for helpful suggestions
     */
    fun suggestionTap() {
        if (!isEnabled) return
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Contextually appropriate haptic feedback based on button type and function
     */
    fun buttonPress(buttonType: ButtonType, buttonFunction: ButtonFunction) {
        when (buttonFunction) {
            ButtonFunction.EQUALS -> equalsTap()
            ButtonFunction.CLEAR -> clearTap()
            ButtonFunction.BACKSPACE -> deleteTap()
            
            ButtonFunction.MEMORY_STORE, ButtonFunction.MEMORY_RECALL,
            ButtonFunction.MEMORY_CLEAR, ButtonFunction.MEMORY_PLUS,
            ButtonFunction.MEMORY_MINUS -> memoryTap()
            
            else -> when (buttonType) {
                ButtonType.NUMBER -> numberTap()
                ButtonType.OPERATOR -> operatorTap()
                ButtonType.FUNCTION -> functionTap()
                ButtonType.SPECIAL -> operatorTap()
                ButtonType.PARENTHESIS -> operatorTap()
            }
        }
    }
}

@Composable
fun rememberHapticFeedbackManager(): HapticFeedbackManager {
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    return remember(hapticFeedback) {
        HapticFeedbackManager(hapticFeedback, scope)
    }
}