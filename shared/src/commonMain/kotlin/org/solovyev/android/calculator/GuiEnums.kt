package org.solovyev.android.calculator

enum class GuiTheme(val id: String) {
    material_theme("material_theme"),
    material_light("material_light"),
    material_dark("material_dark");

    companion object {
        fun fromId(id: String): GuiTheme = values().firstOrNull { it.id == id } ?: material_theme
    }
}

enum class GuiMode(val id: String) {
    simple("simple"),
    engineer("engineer"),
    modern("modern");

    companion object {
        fun fromId(id: String): GuiMode = values().firstOrNull { it.id == id } ?: simple
    }
}
