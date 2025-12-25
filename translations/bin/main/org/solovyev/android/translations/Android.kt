package org.solovyev.android.translations

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.http.util.TextUtils
import java.io.File

object Android {

    @JvmStatic
    fun main(vararg args: String) {
        val options = Options()
        options.addOption(
            Option.builder("prefix")
                .hasArg()
                .desc("Local location of Android project")
                .required(false)
                .build()
        )
        options.addOption(
            Option.builder("project")
                .hasArg()
                .desc("Local location of Android project")
                .build()
        )
        options.addOption(
            Option.builder("resources")
                .hasArg()
                .desc("String identifiers to be copied")
                .build()
        )
        options.addOption(
            Option.builder("output")
                .hasArg()
                .desc("Output folder")
                .required()
                .build()
        )
        options.addOption(
            Option.builder("languages")
                .hasArg()
                .desc("Comma-separated list of languages for translation")
                .required()
                .build()
        )

        val parser = DefaultParser()
        val commandLine = parser.parse(options, args)

        val projects = if (commandLine.hasOption("project")) {
            val projectPaths = commandLine.getOptionValues("project")
            Array(projectPaths.size) { i ->
                makeInputDirectory(projectPaths[i])
            }
        } else {
            null
        }

        val prefix = makePrefix(commandLine.getOptionValue("prefix"))

        val projectsLinks = if (commandLine.hasOption("resources")) {
            val projectResources = commandLine.getOptionValues("resources")
            Array(projectResources.size) { j ->
                val resources = projectResources[j]
                mutableListOf<TranslationLink>().apply {
                    resources.split(",").forEach { resource ->
                        val i = resource.indexOf("-")
                        if (i >= 0) {
                            add(
                                TranslationLink(
                                    resource.substring(0, i),
                                    prefix + resource.substring(i + 1)
                                )
                            )
                        } else {
                            add(TranslationLink(resource, prefix + resource))
                        }
                    }
                }
            }
        } else {
            null
        }

        val languageLocales = mutableListOf<String>()
        languageLocales.addAll(commandLine.getOptionValue("languages").split(","))
        languageLocales.add("")

        val outDir = File(commandLine.getOptionValue("output"))
        Utils.delete(outDir)
        val outResDir = File(outDir, "res")
        outResDir.mkdirs()

        if (!projects.isNullOrEmpty()) {
            if (projectsLinks == null || projectsLinks.size != projects.size) {
                throw IllegalArgumentException(
                    "Projects=${projects.size}, resources=${projectsLinks?.size ?: 0}"
                )
            }
            for (i in projects.indices) {
                val project = projects[i]
                val projectLinks = projectsLinks[i]
                translate(
                    outResDir,
                    languageLocales,
                    "other${if (i == 0) "" else i}",
                    TranslationDef(project, projectLinks)
                )
            }
        }
    }

    private fun makePrefix(prefix: String?): String {
        return if (prefix.isNullOrEmpty()) {
            ""
        } else {
            "${prefix}_"
        }
    }

    private fun translate(
        outDir: File,
        languageLocales: List<String>,
        outPostfix: String,
        vararg translationDefs: TranslationDef
    ) {
        for (languageLocale in languageLocales) {
            val translations = Resources()
            for (def in translationDefs) {
                translate(readResources(def.project, languageLocale), translations, def.links)
            }
            Utils.saveTranslations(translations, languageLocale, outDir, "strings_imported_$outPostfix.xml")
        }
    }

    private fun readResources(from: File, languageLocale: String): Resources {
        var inFile = makeStringsFile(from, languageLocale)
        if (!inFile.exists()) {
            val i = languageLocale.indexOf("-r")
            if (i >= 0) {
                inFile = makeStringsFile(from, languageLocale.substring(0, i))
            }
        }
        val resources = Utils.persister.read(Resources::class.java, inFile)
        resources?.comment = "Copied from $from"
        return resources
    }

    private fun makeStringsFile(from: File, languageLocale: String): File {
        return File(File(from, Utils.valuesFolderName(languageLocale)), "strings.xml")
    }

    private fun makeInputDirectory(dirName: String): File {
        val dir = File(dirName)
        require(dir.exists() && dir.isDirectory) {
            "$dir doesn't exist or not a directory"
        }
        return File(dir, "res")
    }

    private fun translate(from: Resources, to: Resources, links: List<TranslationLink>) {
        to.comment = from.comment
        for (translationLink in links) {
            val translation = translate(from, translationLink)
            if (!TextUtils.isBlank(translation)) {
                to.strings.add(ResourceString(translationLink.outName, translation))
            }
        }
    }

    private fun translate(resources: Resources, translationLink: TranslationLink): String? {
        for (string in resources.strings) {
            if (string.name == translationLink.inName) {
                val value = string.value
                if (TextUtils.isBlank(value)) {
                    return null
                }
                if (value != null && value.length >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    return value.substring(1, value.length - 1)
                }
                return value
            }
        }
        return null
    }

    private data class TranslationDef(
        val project: File,
        val links: List<TranslationLink>
    )

    private data class TranslationLink(
        val inName: String,
        val outName: String
    )
}
