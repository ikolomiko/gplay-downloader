package gplay.downloader

import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.File as ApkFile
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.AuthHelper
import com.aurora.gplayapi.helpers.PurchaseHelper
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class Downloader(
        val log: Logger,
        val outputPath: String,
        val configManager: ConfigManager,
        val singleApk: Boolean
) {
    data class Query(val files: List<ApkFile>, val isFree: Boolean)

    private fun getQuery(appId: String, authData: AuthData, log: Logger): Query {
        var files: List<ApkFile>
        try {
            val app = AppDetailsHelper(authData).getAppByPackageName(appId)
            if (!app.isFree) {
                return Query(emptyList(), false)
            }

            files =
                    PurchaseHelper(authData)
                            .purchase(app.packageName, app.versionCode, app.offerType)

            if (singleApk) {
                for (file in files) {
                    if (file.name.startsWith(appId)) {
                        files = listOf(file)
                        log.info("Chosen file name: " + file.name)
                        break
                    }
                }
            }
        } catch (e: Exception) {
            log.error("getQuery error at AppId $appId " + e.toString())
            files = emptyList()
        }

        return Query(files, true)
    }

    private fun downloadQuery(query: Query, appId: String) {
        var outputDir = outputPath

        // Group all APKs of the same app into a directory if the 'single APK' option was not set
        // This step is necessary because many of the split APKs share the same names
        if (!singleApk) {
            outputDir += "$appId/"
            File(outputDir).mkdirs()
        }

        for (file in query.files) {
            val outputFile = outputDir + file.name
            URL(file.url).openStream().use { Files.copy(it, Paths.get(outputFile)) }
        }
    }

    private fun applyProxyConfig(proxyConfig: ProxyConfig) {
        val (proxyHost, proxyPort, proxyUser, proxyPassword) = proxyConfig
        log.status("Using proxy ${proxyHost}:${proxyPort}")

        System.setProperty("http.proxyHost", proxyHost)
        System.setProperty("http.proxyPort", proxyPort)
        System.setProperty("http.proxyUser", proxyUser.orEmpty())
        System.setProperty("http.proxyPassword", proxyPassword.orEmpty())

        System.setProperty("https.proxyHost", proxyHost)
        System.setProperty("https.proxyPort", proxyPort)
        System.setProperty("https.proxyUser", proxyUser.orEmpty())
        System.setProperty("https.proxyPassword", proxyPassword.orEmpty())
    }

    public fun downloadAll(appIds: List<String>) {
        val len = appIds.size
        for ((index, id) in appIds.withIndex()) {
            log.status("Getting ${index+1}/$len AppId $id")

            // Check if the app is already downloaded
            if (File(outputPath + id + ".apk").exists()) {
                log.info("Already downloaded AppId $id")
                continue
            }

            // Get downloader config and apply proxy config if given
            log.status("Getting next downloader config")
            val (authConfig, proxyConfig) = configManager.getNextConfig()
            if (proxyConfig != null) {
                applyProxyConfig(proxyConfig)
            }

            // Build login credentials
            val authData = AuthHelper.build(authConfig.email, authConfig.aasToken)
            log.status("Logged in as ${authConfig.email}")

            // Try to find the app and download
            val query = getQuery(id, authData, log)
            if (!query.isFree) {
                log.warning("NOTFREE AppId $id")
            } else if (query.files.isNullOrEmpty()) {
                log.warning("NOTFOUND AppId $id")
            } else {
                try {
                    downloadQuery(query, id)
                    log.success("Downloaded AppId $id")
                } catch (e: Exception) {
                    log.error("DownloadError: Could not download AppId $id")
                }
            }
        }
    }
}
