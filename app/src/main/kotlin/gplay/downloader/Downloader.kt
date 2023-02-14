package gplay.downloader

import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.PurchaseHelper
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class Downloader(
        val log: Logger,
        val outputPath: String,
        val authData: AuthData,
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

    public fun downloadAll(appIds: List<String>) {
        val len = appIds.size
        appIds.forEachIndexed { index, id ->
            log.status("Getting ${index+1}/$len AppId $id")

            if (File(outputPath + id + ".apk").exists()) {
                log.info("Already downloaded AppId $id")
                return@forEachIndexed
            }

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
