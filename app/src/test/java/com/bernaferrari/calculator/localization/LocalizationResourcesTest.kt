package com.bernaferrari.calculator.localization

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Check the resources actually used by this app, excluding unused legacy resources. */
class LocalizationResourcesTest {
    private val main = File("src/main").let { if (it.isDirectory) it else File("app/src/main") }
    private val resourceReference = Regex("R\\.string\\.([A-Za-z_0-9]+)")
    private val placeholder = Regex("%(?:[0-9]+\\$)?[dsf]")

    @Test fun everySupportedLanguageTranslatesEveryUsedString() {
        val used = main.resolve("java").walkTopDown().filter { it.extension == "kt" }
            .flatMap { resourceReference.findAll(it.readText()).map { match -> match.groupValues[1] } }
            .toSet() - nonTranslatableStrings()
        val english = strings(main.resolve("res/values/strings.xml"))
        for (locale in listOf("de", "es", "fr", "it", "ja", "pt-rBR", "zh")) {
            val translated = strings(main.resolve("res/values-$locale/strings.xml"))
            assertTrue("$locale missing: ${used - translated.keys}", translated.keys.containsAll(used))
            for (key in used) {
                assertTrue("$locale/$key is empty", translated.getValue(key).isNotBlank())
                assertEquals("$locale/$key format arguments", arguments(english.getValue(key)), arguments(translated.getValue(key)))
            }
        }
    }

    @Test fun everyConversionUnitHasALabel() {
        val units = main.resolve("java/com/bernaferrari/calculator/domain/convert/Units.kt").readText()
        val ids = Regex("(?:u|MeasureUnit)\\(\"([^\"]+)\"").findAll(units).map { it.groupValues[1] }.toSet()
        val labels = main.resolve("java/com/bernaferrari/calculator/ui/converter/UnitLabels.kt").readText()
        val labeled = Regex("\"([^\"]+)\" -> R\\.string\\.").findAll(labels).map { it.groupValues[1] }.toSet()
        assertEquals(ids, labeled)
    }

    private fun nonTranslatableStrings(): Set<String> {
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(main.resolve("res/values/strings.xml")).getElementsByTagName("string")
        return (0 until nodes.length).map { nodes.item(it) }
            .filter { it.attributes.getNamedItem("translatable")?.nodeValue == "false" }
            .map { it.attributes.getNamedItem("name").nodeValue }.toSet()
    }

    private fun arguments(text: String) = placeholder.findAll(text).map { it.value }.sorted().toList()

    private fun strings(file: File): Map<String, String> {
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getElementsByTagName("string")
        return (0 until nodes.length).associate { index ->
            val node = nodes.item(index)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }
    }
}
