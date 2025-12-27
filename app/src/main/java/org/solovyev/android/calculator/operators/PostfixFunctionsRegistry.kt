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
class PostfixFunctionsRegistry @Inject constructor(
    mathEngine: JsclMathEngine
) : BaseEntitiesRegistry<Operator>(mathEngine.getPostfixFunctionsRegistry()) {

    init {
        addDescription("%", R.string.c_pf_description_percent)
        addDescription("!", R.string.c_pf_description_factorial)
        addDescription("!!", R.string.c_pf_description_double_factorial)
        addDescription("°", R.string.c_pf_description_degree)
    }

    override fun toJsonable(entity: Operator): Jsonable? = null

    override fun getEntitiesFile(): Path? = null

    override fun getCategory(entity: Operator): Category<*>? {
        return Entities.getCategory(entity, OperatorCategory.values())
    }
}
