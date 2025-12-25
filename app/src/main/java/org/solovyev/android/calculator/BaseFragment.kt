package org.solovyev.android.calculator

import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.ads.AdUi
import org.solovyev.android.plotter.Check
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseFragment(@LayoutRes private val layout: Int) : Fragment() {

    @Inject
    lateinit var adUi: AdUi

    @Inject
    lateinit var typeface: Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adUi.onCreate()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(layout, container, false)
        adUi.onCreateView(view)
        BaseActivity.fixFonts(view, typeface)
        return view
    }

    override fun onResume() {
        super.onResume()
        adUi.onResume()
    }

    override fun onPause() {
        adUi.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        adUi.onDestroyView()
        super.onDestroyView()
    }

    override fun onDestroy() {
        adUi.onDestroy()
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun addMenu(
            menu: ContextMenu,
            @StringRes label: Int,
            listener: MenuItem.OnMenuItemClickListener
        ): MenuItem {
            return menu.add(ContextMenu.NONE, label, ContextMenu.NONE, label)
                .setOnMenuItemClickListener(listener)
        }

        @JvmStatic
        fun <P : Parcelable> getParcelable(bundle: Bundle, key: String): P {
            val parcelable = bundle.getParcelable<P>(key)
            Check.isNotNull(parcelable)
            return parcelable!!
        }
    }
}
