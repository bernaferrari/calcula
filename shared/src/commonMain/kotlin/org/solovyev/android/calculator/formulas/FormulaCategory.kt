package org.solovyev.android.calculator.formulas

enum class FormulaCategory(val displayName: String, val iconName: String, val description: String) {
    MATHEMATICS("Mathematics", "functions", "Algebra, geometry, and calculus formulas"),
    PHYSICS("Physics", "science", "Mechanics, electricity, and thermodynamics"),
    FINANCE("Finance", "account_balance", "Loans, interest, and investment calculations"),
    ENGINEERING("Engineering", "architecture", "Circuit analysis and structural formulas"),
    HEALTH("Health", "favorite", "BMI, BMR, and fitness calculations"),
    EVERYDAY("Everyday", "lightbulb", "Cooking, conversions, and daily utilities"),
    CUSTOM("Custom", "edit", "Your saved custom formulas");

    companion object {
        fun fromName(name: String): FormulaCategory = entries.find { it.name == name } ?: MATHEMATICS
    }
}
