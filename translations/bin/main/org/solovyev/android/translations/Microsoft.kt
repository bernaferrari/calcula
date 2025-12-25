package org.solovyev.android.translations

import org.apache.commons.codec.Charsets
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.http.util.TextUtils
import java.io.File
import java.io.IOException
import java.util.regex.Pattern

object Microsoft {

    private val TRANSLATION_REGEX = Pattern.compile("<TranslatedText>(.+?)</TranslatedText>")

    private const val xmlVersions = """<ter:Versions>
<ter:Version>
<ter:Name>${"$"}{version}</ter:Name>
</ter:Version>
</ter:Versions>
"""

    private const val xmlPre = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ter="http://api.terminology.microsoft.com/terminology">
<soapenv:Header/>
<soapenv:Body>
<ter:GetTranslations>
<ter:text>${"$"}{text}</ter:text>
<ter:from>en-us</ter:from>
<ter:to>${"$"}{to}</ter:to>
<ter:searchOperator>Contains</ter:searchOperator>
<ter:sources>
<ter:TranslationSource>UiStrings</ter:TranslationSource>
</ter:sources>
<ter:unique>false</ter:unique>
<ter:maxTranslations>1</ter:maxTranslations>
<ter:includeDefinitions>true</ter:includeDefinitions>
<ter:products>
<ter:Product>
<ter:Name>${"$"}{product}</ter:Name>
"""

    private const val xmlPost = """</ter:Product>
</ter:products>
</ter:GetTranslations>
</soapenv:Body>
</soapenv:Envelope>"""

    private val xml = xmlPre + xmlVersions + xmlPost
    private val xmlNoVersion = xmlPre + xmlPost

    @JvmStatic
    fun main(vararg args: String) {
        val inFileName = "app/src/main/res/values/strings_microsoft.xml"
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
                        if (TextUtils.isEmpty(string.comment)) {
                            System.err.println("No product/version for ${string.value}")
                            continue
                        }
                        val comments = string.comment!!.split("-")
                        val translation = translate(
                            client,
                            string.value!!,
                            language,
                            comments[0],
                            if (comments.size > 1) comments[1] else ""
                        )
                        if (!TextUtils.isEmpty(translation)) {
                            translations.strings.add(ResourceString(string.name, translation))
                        }
                    }
                }
                Utils.saveTranslations(translations, languageLocale, outDir, inFile.name)
            }
        }
    }

    private fun translate(
        client: CloseableHttpClient,
        word: String,
        language: String,
        product: String,
        version: String
    ): String? {
        val request = HttpPost("http://api.terminology.microsoft.com/Terminology.svc")
        request.addHeader("Content-Type", "text/xml; charset=utf-8")
        request.addHeader(
            "SOAPAction",
            "\"http://api.terminology.microsoft.com/terminology/Terminology/GetTranslations\""
        )

        val xml = if (version.isEmpty()) {
            xmlNoVersion
        } else {
            Microsoft.xml.replace("\${version}", version)
        }

        val processedWord = word
            .replace("%1\$s", "{0}")
            .replace("%2\$s", "{1}")
            .replace("\\'", "'")

        val body = xml
            .replace("\${text}", processedWord)
            .replace("\${to}", language)
            .replace("\${product}", product)

        request.entity = StringEntity(body, Charsets.UTF_8)

        var response: CloseableHttpResponse? = null
        return try {
            response = client.execute(request)
            val result = EntityUtils.toString(response.entity)
            if (TextUtils.isEmpty(result)) {
                System.err.println("No translation for $word in $language")
                return null
            }
            val matcher = TRANSLATION_REGEX.matcher(result)
            if (!matcher.find()) {
                System.err.println("No translation for $word in $language")
                return null
            }
            "\"${matcher.group(1).replace("{0}", "%1\$s").replace("{1}", "%2\$s")}\""
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: RuntimeException) {
            e.printStackTrace()
            null
        } finally {
            Utils.close(response)
        }
    }

    private fun toLanguage(languageLocale: String): String {
        return when (languageLocale) {
            "en" -> "en-us"
            "cs" -> "cs-cz"
            "ar" -> "ar-sa"
            "vi" -> "vi-vn"
            "ja" -> "ja-jp"
            "uk" -> "uk-ua"
            "pt-rBR" -> "pt-br"
            "pt-rPT" -> "pt-pt"
            "zh-rTW" -> "zh-tw"
            "zh-rCN" -> "zh-cn"
            else -> {
                val i = languageLocale.indexOf('-')
                val language = if (i >= 0) languageLocale.substring(0, i) else languageLocale
                "$language-$language"
            }
        }
    }
}
