package gplay.downloader

import com.aurora.gplayapi.data.models.AuthData
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
) {
    data class Query(val url: String, val appId: String, val isFree: Boolean)

    private fun getQuery(appId: String, authData: AuthData, log: Logger): Query? {
        var url: String = ""
        try {
            val app = AppDetailsHelper(authData).getAppByPackageName(appId)
            if (!app.isFree) {
                return Query("", appId, false)
            }

            val files =
                    PurchaseHelper(authData)
                            .purchase(app.packageName, app.versionCode, app.offerType)

            for (file in files) {
                if (file.name.startsWith(appId)) {
                    url = file.url
                    println("Chosen file name: " + file.name)
                    break
                }
            }
        } catch (e: Exception) {
            log.dqError("getQuery error at AppId $appId " + e.toString())
            return null
        }

        return when (url) {
            "" -> null
            else -> Query(url, appId, true)
        }
    }

    private fun downloadQuery(query: Query) {
        val path = outputPath + query.appId + ".apk"
        URL(query.url).openStream().use { Files.copy(it, Paths.get(path)) }
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
            if (query == null) {
                log.warning("NOTFOUND AppId $id")
            } else if (!query.isFree) {
                log.warning("NOTFREE AppId $id")
            } else {
                try {
                    downloadQuery(query)
                    log.success("Downloaded AppId $id")
                } catch (e: Exception) {
                    log.dlError("Could not download AppId $id")
                }
            }
        }
    }
}
