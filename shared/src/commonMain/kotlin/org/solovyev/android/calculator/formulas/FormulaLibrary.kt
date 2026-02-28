package org.solovyev.android.calculator.formulas

import kotlinx.serialization.Serializable

@Serializable
data class FormulaVariable(
    val id: String,
    val name: String,
    val symbol: String,
    val defaultValue: String = ""
)

@Serializable
data class Formula(
    val id: String,
    val name: String,
    val description: String,
    val category: FormulaCategory,
    val expression: String,
    val variables: List<FormulaVariable>
)

object FormulaLibrary {
    private val formulas = listOf(
        // Mathematics (4)
        Formula(
            id = "math_quadratic",
            name = "Quadratic Formula",
            description = "Solve ax² + bx + c = 0",
            category = FormulaCategory.MATHEMATICS,
            expression = "(-b+sqrt(b^2-4*a*c))/(2*a)",
            variables = listOf(
                FormulaVariable("a", "Coefficient a", "a", "1"),
                FormulaVariable("b", "Coefficient b", "b", "0"),
                FormulaVariable("c", "Coefficient c", "c", "0")
            )
        ),
        Formula(
            id = "math_distance",
            name = "Distance Formula",
            description = "Distance between two points",
            category = FormulaCategory.MATHEMATICS,
            expression = "sqrt((x2-x1)^2+(y2-y1)^2)",
            variables = listOf(
                FormulaVariable("x1", "Point 1 X", "x₁", "0"),
                FormulaVariable("y1", "Point 1 Y", "y₁", "0"),
                FormulaVariable("x2", "Point 2 X", "x₂", "1"),
                FormulaVariable("y2", "Point 2 Y", "y₂", "1")
            )
        ),
        Formula(
            id = "math_pythagorean",
            name = "Pythagorean Theorem",
            description = "Calculate hypotenuse of right triangle",
            category = FormulaCategory.MATHEMATICS,
            expression = "sqrt(a^2+b^2)",
            variables = listOf(
                FormulaVariable("a", "Side a", "a", "3"),
                FormulaVariable("b", "Side b", "b", "4")
            )
        ),
        Formula(
            id = "math_circle_area",
            name = "Circle Area",
            description = "Calculate area of a circle",
            category = FormulaCategory.MATHEMATICS,
            expression = "pi*r^2",
            variables = listOf(
                FormulaVariable("r", "Radius", "r", "1")
            )
        ),

        // Physics (3)
        Formula(
            id = "physics_force",
            name = "Force (F=ma)",
            description = "Calculate force from mass and acceleration",
            category = FormulaCategory.PHYSICS,
            expression = "m*a",
            variables = listOf(
                FormulaVariable("m", "Mass", "m", "10"),
                FormulaVariable("a", "Acceleration", "a", "9.8")
            )
        ),
        Formula(
            id = "physics_kinetic_energy",
            name = "Kinetic Energy",
            description = "Energy of motion",
            category = FormulaCategory.PHYSICS,
            expression = "0.5*m*v^2",
            variables = listOf(
                FormulaVariable("m", "Mass", "m", "10"),
                FormulaVariable("v", "Velocity", "v", "5")
            )
        ),
        Formula(
            id = "physics_power",
            name = "Power",
            description = "Power from voltage and current",
            category = FormulaCategory.PHYSICS,
            expression = "v*i",
            variables = listOf(
                FormulaVariable("v", "Voltage", "V", "12"),
                FormulaVariable("i", "Current", "I", "2")
            )
        ),

        // Finance (4)
        Formula(
            id = "finance_compound_interest",
            name = "Compound Interest",
            description = "Future value with compound interest",
            category = FormulaCategory.FINANCE,
            expression = "p*(1+r/100)^t",
            variables = listOf(
                FormulaVariable("p", "Principal", "P", "1000"),
                FormulaVariable("r", "Annual Rate (%)", "r", "5"),
                FormulaVariable("t", "Years", "t", "10")
            )
        ),
        Formula(
            id = "finance_tip",
            name = "Tip Calculator",
            description = "Calculate tip amount",
            category = FormulaCategory.FINANCE,
            expression = "bill*(rate/100)",
            variables = listOf(
                FormulaVariable("bill", "Bill Amount", "Bill", "50"),
                FormulaVariable("rate", "Tip %", "%", "15")
            )
        ),
        Formula(
            id = "finance_mortgage",
            name = "Mortgage Payment",
            description = "Monthly mortgage payment",
            category = FormulaCategory.FINANCE,
            expression = "p*(r/100)*(1+r/100)^n/((1+r/100)^n-1)",
            variables = listOf(
                FormulaVariable("p", "Principal", "P", "300000"),
                FormulaVariable("r", "Monthly Rate (%)", "r", "0.5"),
                FormulaVariable("n", "Payments", "n", "360")
            )
        ),
        Formula(
            id = "finance_roi",
            name = "Return on Investment",
            description = "Calculate ROI percentage",
            category = FormulaCategory.FINANCE,
            expression = "((gain-cost)/cost)*100",
            variables = listOf(
                FormulaVariable("gain", "Final Value", "Gain", "1200"),
                FormulaVariable("cost", "Initial Cost", "Cost", "1000")
            )
        ),

        // Health (3)
        Formula(
            id = "health_bmi",
            name = "BMI",
            description = "Body Mass Index calculator",
            category = FormulaCategory.HEALTH,
            expression = "weight/(height^2)",
            variables = listOf(
                FormulaVariable("weight", "Weight (kg)", "W", "70"),
                FormulaVariable("height", "Height (m)", "h", "1.75")
            )
        ),
        Formula(
            id = "health_bmr_male",
            name = "BMR (Male)",
            description = "Basal Metabolic Rate for men",
            category = FormulaCategory.HEALTH,
            expression = "10*weight+6.25*height-5*age+5",
            variables = listOf(
                FormulaVariable("weight", "Weight (kg)", "w", "70"),
                FormulaVariable("height", "Height (cm)", "h", "175"),
                FormulaVariable("age", "Age", "a", "30")
            )
        ),
        Formula(
            id = "health_max_hr",
            name = "Max Heart Rate",
            description = "Estimate maximum heart rate",
            category = FormulaCategory.HEALTH,
            expression = "220-age",
            variables = listOf(
                FormulaVariable("age", "Age", "age", "30")
            )
        ),

        // Everyday (6)
        Formula(
            id = "everyday_c_to_f",
            name = "Celsius to Fahrenheit",
            description = "Convert Celsius to Fahrenheit",
            category = FormulaCategory.EVERYDAY,
            expression = "(9/5)*c+32",
            variables = listOf(
                FormulaVariable("c", "Celsius", "°C", "0")
            )
        ),
        Formula(
            id = "everyday_f_to_c",
            name = "Fahrenheit to Celsius",
            description = "Convert Fahrenheit to Celsius",
            category = FormulaCategory.EVERYDAY,
            expression = "(5/9)*(f-32)",
            variables = listOf(
                FormulaVariable("f", "Fahrenheit", "°F", "32")
            )
        ),
        Formula(
            id = "everyday_km_to_miles",
            name = "Kilometers to Miles",
            description = "Convert kilometers to miles",
            category = FormulaCategory.EVERYDAY,
            expression = "km*0.621371",
            variables = listOf(
                FormulaVariable("km", "Kilometers", "km", "10")
            )
        ),
        Formula(
            id = "everyday_kg_to_lb",
            name = "Kilograms to Pounds",
            description = "Convert kilograms to pounds",
            category = FormulaCategory.EVERYDAY,
            expression = "kg*2.20462",
            variables = listOf(
                FormulaVariable("kg", "Kilograms", "kg", "10")
            )
        ),
        Formula(
            id = "everyday_fuel_efficiency",
            name = "Fuel Efficiency",
            description = "Calculate MPG",
            category = FormulaCategory.EVERYDAY,
            expression = "miles/gallons",
            variables = listOf(
                FormulaVariable("miles", "Distance (miles)", "d", "300"),
                FormulaVariable("gallons", "Gallons", "gal", "10")
            )
        ),
        Formula(
            id = "everyday_split_bill",
            name = "Split Bill",
            description = "Split bill among people",
            category = FormulaCategory.EVERYDAY,
            expression = "total/people",
            variables = listOf(
                FormulaVariable("total", "Total Amount", "Total", "100"),
                FormulaVariable("people", "Number of People", "n", "4")
            )
        )
    )

    fun getAll(): List<Formula> = formulas

    fun getById(id: String): Formula? = formulas.find { it.id == id }

    fun search(query: String): List<Formula> {
        val lower = query.lowercase()
        return formulas.filter {
            it.name.lowercase().contains(lower) ||
            it.description.lowercase().contains(lower) ||
            it.category.displayName.lowercase().contains(lower)
        }
    }
}
