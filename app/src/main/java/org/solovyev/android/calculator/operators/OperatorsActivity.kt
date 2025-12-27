package org.solovyev.android.calculator.operators

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ui.compose.entities.EntityListScreen
import org.solovyev.android.calculator.ui.compose.entities.EntityMenuItem
import org.solovyev.android.calculator.ui.compose.entities.EntityRowModel
import org.solovyev.android.calculator.ui.compose.entities.EntityTab
import org.solovyev.android.calculator.ui.compose.entities.OperatorsComposeViewModel
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme

@AndroidEntryPoint
open class OperatorsActivity : BaseActivity(R.string.c_operators) {

    @Composable
    override fun Content() {
        val viewModel: OperatorsComposeViewModel = hiltViewModel()
        val themePreference by appPreferences.settings.theme.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getThemeBlocking()
        )

        CalculatorTheme(theme = themePreference) {
            val tabs = remember { buildOperatorTabs(viewModel) }
            EntityListScreen(
                title = getString(R.string.c_operators),
                tabs = tabs,
                onBack = { finish() }
            )
        }
    }

    private fun buildOperatorTabs(viewModel: OperatorsComposeViewModel): List<EntityTab> {
        return viewModel.getOperatorCategories().map { category ->
            val items = viewModel.getOperatorsFor(category).map { operator ->
                EntityRowModel(
                    id = "operator:${operator.name}",
                    title = operator.toString(),
                    subtitle = viewModel.getOperatorDescription(operator),
                    onUse = {
                        viewModel.useName(operator.name)
                        finish()
                    },
                    menuItems = listOf(
                        EntityMenuItem(
                            label = getString(R.string.c_use),
                            onClick = {
                                viewModel.useName(operator.name)
                                finish()
                            }
                        )
                    )
                )
            }
            EntityTab(
                title = getString(category.title),
                items = items
            )
        }
    }

    class Dialog : OperatorsActivity()

    companion object {
        @JvmStatic
        fun getClass(context: Context): Class<out OperatorsActivity> {
            return if (App.isTablet(context)) Dialog::class.java else OperatorsActivity::class.java
        }
    }
}
