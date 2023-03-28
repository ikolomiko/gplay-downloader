package gplay.downloader

import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.File as ApkFile
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.PurchaseHelper
import gplay.downloader.config.ConfigManager
import gplay.downloader.config.ProxyConfig
import gplay.downloader.config.DownloaderConfig
import java.io.File

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
            e.printStackTrace()
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
            HttpClient.downloadFile(file.url, outputFile)
        }
    }

    private fun applyProxyConfig(proxyConfig: ProxyConfig) {
        val (proxyHost, proxyPort, proxyUser, proxyPassword) = proxyConfig
        log.status("Using proxy $proxyHost:$proxyPort")

        HttpClient.setProxy(proxyHost, proxyPort, proxyUser.orEmpty(), proxyPassword.orEmpty())
    }

    public fun downloadAll(appIds: List<String>) {
        val len = appIds.size
        var dlConfig: DownloaderConfig? = null
        var authData: AuthData? = null

        for ((index, id) in appIds.withIndex()) {
            log.status("Getting ${index+1}/$len AppId $id")

            val outputFile =
                    if (singleApk) {
                        outputPath + id + ".apk"
                    } else {
                        outputPath + id + "/" + id + ".apk"
                    }

            // Check if the app is already downloaded
            if (File(outputFile).exists()) {
                log.info("Already downloaded AppId $id")
                continue
            }

            // Get the next downloader config every 3 apps
            if (index % 3 == 0) {
                log.status("Getting next downloader config")
                dlConfig = configManager.getNextConfig()
            }
            val (authConfig, proxyConfig) = dlConfig!!
            if (proxyConfig != null) {
                applyProxyConfig(proxyConfig)
            }

            // Build login credentials every 3 apps
            if (index % 3 == 0) {
                authData = authConfig.login()
                log.status("Logged in as ${authData.email}")
            }

            // Try to find the app and download
            val query = getQuery(id, authData!!, log)
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
                    e.printStackTrace()
                }
            }
        }
    }
}
