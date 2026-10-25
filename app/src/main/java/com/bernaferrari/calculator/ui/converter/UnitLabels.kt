package com.bernaferrari.calculator.ui.converter

import androidx.annotation.StringRes
import com.bernaferrari.calculator.R
import com.bernaferrari.calculator.domain.convert.MeasureUnit

/** UI labels stay separate from stable conversion IDs and mathematical definitions. */
@get:StringRes
internal val MeasureUnit.nameResource: Int
    get() = when (id) {
        "mm" -> R.string.measure_mm
        "cm" -> R.string.measure_cm
        "m" -> R.string.measure_m
        "km" -> R.string.measure_km
        "in" -> R.string.measure_in
        "ft" -> R.string.measure_ft
        "yd" -> R.string.measure_yd
        "mi" -> R.string.measure_mi
        "nmi" -> R.string.measure_nmi
        "mg" -> R.string.measure_mg
        "g" -> R.string.measure_g
        "kg" -> R.string.measure_kg
        "t" -> R.string.measure_t
        "oz" -> R.string.measure_oz
        "lb" -> R.string.measure_lb
        "st" -> R.string.measure_st
        "c" -> R.string.measure_c
        "f" -> R.string.measure_f
        "k" -> R.string.measure_k
        "cm2" -> R.string.measure_cm2
        "m2" -> R.string.measure_m2
        "ha" -> R.string.measure_ha
        "km2" -> R.string.measure_km2
        "ft2" -> R.string.measure_ft2
        "ac" -> R.string.measure_ac
        "mi2" -> R.string.measure_mi2
        "ml" -> R.string.measure_ml
        "l" -> R.string.measure_l
        "m3" -> R.string.measure_m3
        "floz" -> R.string.measure_floz
        "cup" -> R.string.measure_cup
        "gal" -> R.string.measure_gal
        "mps" -> R.string.measure_mps
        "kmh" -> R.string.measure_kmh
        "mph" -> R.string.measure_mph
        "kn" -> R.string.measure_kn
        "ms" -> R.string.measure_ms
        "s" -> R.string.measure_s
        "min" -> R.string.measure_min
        "h" -> R.string.measure_h
        "d" -> R.string.measure_d
        "wk" -> R.string.measure_wk
        "yr" -> R.string.measure_yr
        "b" -> R.string.measure_b
        "B" -> R.string.measure_byte
        "KB" -> R.string.measure_kb
        "MB" -> R.string.measure_mb
        "GB" -> R.string.measure_gb
        "TB" -> R.string.measure_tb
        "KiB" -> R.string.measure_kib
        "MiB" -> R.string.measure_mib
        "GiB" -> R.string.measure_gib
        "J" -> R.string.measure_j
        "kJ" -> R.string.measure_kj
        "cal" -> R.string.measure_cal
        "kcal" -> R.string.measure_kcal
        "Wh" -> R.string.measure_wh
        "kWh" -> R.string.measure_kwh
        "Pa" -> R.string.measure_pa
        "kPa" -> R.string.measure_kpa
        "bar" -> R.string.measure_bar
        "atm" -> R.string.measure_atm
        "psi" -> R.string.measure_psi
        "mmHg" -> R.string.measure_mmhg
        else -> error("Missing unit label: $id")
    }
