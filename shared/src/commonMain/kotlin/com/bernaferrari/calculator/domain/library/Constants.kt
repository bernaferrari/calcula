package com.bernaferrari.calculator.domain.library

/**
 * A named value worth having at hand. [value] is in the expression language; names live in
 * resources. In [symbol], what follows `_` is a subscript (`m_e`), drawn small rather than with
 * subscript characters, which not every font has.
 */
data class Constant(val id: String, val symbol: String, val value: String, val unit: String, val group: ConstantGroup) {
    /** The symbol on one line, for a key: `me`, `ε0`. */
    val plainSymbol: String get() = symbol.replace("_", "")
}

enum class ConstantGroup { MATH, PHYSICS, CHEMISTRY, ASTRONOMY }

/** CODATA 2018 (exact where the SI defines them) and IAU values. */
val CONSTANTS = listOf(
    Constant("pi", "π", "3.14159265358979323846", "", ConstantGroup.MATH),
    Constant("e", "e", "2.71828182845904523536", "", ConstantGroup.MATH),
    Constant("phi", "φ", "1.61803398874989484820", "", ConstantGroup.MATH),
    Constant("sqrt2", "√2", "1.41421356237309504880", "", ConstantGroup.MATH),
    Constant("gamma", "γ", "0.57721566490153286061", "", ConstantGroup.MATH),
    Constant("c", "c", "299792458", "m/s", ConstantGroup.PHYSICS),
    Constant("g", "g", "9.80665", "m/s²", ConstantGroup.PHYSICS),
    Constant("gconst", "G", "6.67430E−11", "m³/(kg·s²)", ConstantGroup.PHYSICS),
    Constant("h", "h", "6.62607015E−34", "J·s", ConstantGroup.PHYSICS),
    Constant("hbar", "ħ", "1.054571817E−34", "J·s", ConstantGroup.PHYSICS),
    Constant("qe", "e", "1.602176634E−19", "C", ConstantGroup.PHYSICS),
    Constant("me", "m_e", "9.1093837015E−31", "kg", ConstantGroup.PHYSICS),
    Constant("mp", "m_p", "1.67262192369E−27", "kg", ConstantGroup.PHYSICS),
    Constant("kb", "k", "1.380649E−23", "J/K", ConstantGroup.PHYSICS),
    Constant("eps0", "ε_0", "8.8541878128E−12", "F/m", ConstantGroup.PHYSICS),
    Constant("mu0", "μ_0", "1.25663706212E−6", "N/A²", ConstantGroup.PHYSICS),
    Constant("sigma", "σ", "5.670374419E−8", "W/(m²·K⁴)", ConstantGroup.PHYSICS),
    Constant("na", "N_A", "6.02214076E23", "1/mol", ConstantGroup.CHEMISTRY),
    Constant("r", "R", "8.314462618", "J/(mol·K)", ConstantGroup.CHEMISTRY),
    Constant("f", "F", "96485.33212", "C/mol", ConstantGroup.CHEMISTRY),
    Constant("u", "u", "1.66053906660E−27", "kg", ConstantGroup.CHEMISTRY),
    Constant("atm", "atm", "101325", "Pa", ConstantGroup.CHEMISTRY),
    Constant("au", "AU", "149597870700", "m", ConstantGroup.ASTRONOMY),
    Constant("ly", "ly", "9460730472580800", "m", ConstantGroup.ASTRONOMY),
    Constant("pc", "pc", "3.0856775814913673E16", "m", ConstantGroup.ASTRONOMY),
    Constant("msun", "M☉", "1.98847E30", "kg", ConstantGroup.ASTRONOMY),
    Constant("mearth", "M⊕", "5.9722E24", "kg", ConstantGroup.ASTRONOMY),
    Constant("rearth", "R⊕", "6371000", "m", ConstantGroup.ASTRONOMY),
)
