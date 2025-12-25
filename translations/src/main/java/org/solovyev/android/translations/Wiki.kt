package org.solovyev.android.translations

import org.apache.commons.codec.Charsets
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.http.util.TextUtils
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLEncoder

object Wiki {

    @JvmStatic
    fun main(vararg args: String) {
        val inFileName = "app/src/main/res/values/strings_converter.xml"
        val inFile = File(inFileName)

        val outDir = File("build/translations/res")
        Utils.delete(outDir)
        outDir.mkdirs()

        val resources = Utils.persister.read(Resources::class.java, inFile)

        HttpClients.createDefault().use { client ->
            val allTranslations = mutableMapOf<String, Resources>()
            for (languageLocale in Utils.languageLocales) {
                val language = toLanguage(languageLocale)
                var translations = allTranslations[language]
                if (translations == null) {
                    translations = Resources()
                    allTranslations[language] = translations
                    for (string in resources.strings) {
                        val translation = translate(client, string.value!!, language)
                        if (!TextUtils.isEmpty(translation)) {
                            translations.strings.add(ResourceString(string.name, translation))
                        }
                    }
                }
                Utils.saveTranslations(translations, languageLocale, outDir, inFile.name)
            }
        }
    }

    private fun translate(client: CloseableHttpClient, word: String, language: String): String? {
        val uri = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=langlinks&titles=" +
                URLEncoder.encode(word, Charsets.UTF_8.toString()) + "&lllang=" + language

        val request = HttpGet(uri)
        var response: CloseableHttpResponse? = null

        return try {
            response = client.execute(request)
            val result = EntityUtils.toString(response.entity)
            if (TextUtils.isEmpty(result)) {
                println("No translation for $word")
                return null
            }

            val json = JSONObject(result)
            val jsonQuery = json.getJSONObject("query")
            val jsonPages = jsonQuery.getJSONObject("pages")

            for (key in jsonPages.keys()) {
                val jsonPage = jsonPages.getJSONObject(key)
                val jsonLangLinks = jsonPage.getJSONArray("langlinks")
                if (jsonLangLinks.length() > 0) {
                    val jsonLangLink = jsonLangLinks.getJSONObject(0)
                    val translation = jsonLangLink.getString("*")
                    if (TextUtils.isBlank(translation)) {
                        return null
                    }
                    val i = translation.lastIndexOf(" (")
                    return if (i >= 0) {
                        translation.substring(0, i)
                    } else {
                        translation
                    }
                }
            }
            null
        } catch (e: IOException) {
            e.printStackTrace()
            System.err.println("Uri=$uri")
            null
        } catch (e: RuntimeException) {
            e.printStackTrace()
            System.err.println("Uri=$uri")
            null
        } finally {
            Utils.close(response)
        }
    }

    private fun toLanguage(languageLocale: String): String {
        val i = languageLocale.indexOf('-')
        return if (i >= 0) {
            languageLocale.substring(0, i)
        } else {
            languageLocale
        }
    }
}
