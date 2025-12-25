package org.solovyev.android.translations

import org.apache.http.util.TextUtils
import org.simpleframework.xml.core.Persister
import java.io.Closeable
import java.io.File
import java.io.FileWriter

object Utils {

    val languageLocales = mutableListOf<String>()
    val persister = Persister()

    init {
        languageLocales.add("ar")
        languageLocales.add("cs")
        languageLocales.add("es")
        languageLocales.add("de")
        languageLocales.add("fi")
        languageLocales.add("fr")
        languageLocales.add("it")
        languageLocales.add("nl")
        languageLocales.add("pl")
        languageLocales.add("pt-rBR")
        languageLocales.add("pt-rPT")
        languageLocales.add("ru")
        languageLocales.add("tr")
        languageLocales.add("vi")
        languageLocales.add("ja")
        languageLocales.add("ja")
        languageLocales.add("uk")
        languageLocales.add("zh-rCN")
        languageLocales.add("zh-rTW")
    }

    fun saveTranslations(
        translations: Resources,
        language: String,
        outDir: File,
        fileName: String
    ) {
        val dir = File(outDir, valuesFolderName(language))
        dir.mkdirs()
        var out: FileWriter? = null
        try {
            out = FileWriter(File(dir, fileName))
            out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
            if (!translations.comment.isNullOrEmpty()) {
                out.write("<!-- ${translations.comment} -->\n")
            }
            persister.write(translations, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            close(out)
        }
    }

    fun close(closeable: Closeable?) {
        closeable?.use { }
    }

    fun delete(file: File): Boolean {
        if (!file.exists()) {
            return true
        }
        if (file.isFile) {
            return file.delete()
        }
        var deleted = true
        file.listFiles()?.forEach { child ->
            deleted = deleted and delete(child)
        }
        return deleted && file.delete()
    }

    fun valuesFolderName(languageLocale: String): String {
        return if (TextUtils.isEmpty(languageLocale)) {
            "values"
        } else {
            "values-$languageLocale"
        }
    }
}
