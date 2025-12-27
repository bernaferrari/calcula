package org.solovyev.android.calculator.operators

import jscl.JsclMathEngine
import jscl.math.operator.Operator
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.entities.BaseEntitiesRegistry
import org.solovyev.android.calculator.entities.Category
import org.solovyev.android.calculator.entities.Entities
import org.solovyev.android.calculator.json.Jsonable
import okio.Path
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OperatorsRegistry @Inject constructor(
    mathEngine: JsclMathEngine
) : BaseEntitiesRegistry<Operator>(mathEngine.getOperatorsRegistry()) {

    init {
        addDescription("mod", R.string.c_op_description_mod)
        addDescription("Σ", R.string.c_op_description_sum)
        addDescription("∏", R.string.c_op_description_product)
        addDescription("∂", R.string.c_op_description_derivative)
        addDescription("∫ab", R.string.c_op_description_integral_ab)
        addDescription("∫", R.string.c_op_description_integral)
        addDescription("Σ", R.string.c_op_description_sum)
    }

    override fun toJsonable(entity: Operator): Jsonable? = null

    override fun getEntitiesFile(): Path? = null

    override fun getCategory(entity: Operator): Category<*>? {
        return Entities.getCategory(entity, OperatorCategory.values())
    }
}
