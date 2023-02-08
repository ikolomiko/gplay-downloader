package gplay.downloader

import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.AuthHelper
import com.aurora.gplayapi.helpers.PurchaseHelper
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

data class DownloadQuery(val url: String, val appId: String, val isFree: Boolean)

data class AuthConfig(val email: String, val aasToken: String)

fun getDownloadQuery(appId: String, authData: AuthData): DownloadQuery? {
    var url: String = ""
    try {
        val app = AppDetailsHelper(authData).getAppByPackageName(appId)
        if (!app.isFree) {
            return DownloadQuery("", appId, false)
        }

        val files =
                PurchaseHelper(authData).purchase(app.packageName, app.versionCode, app.offerType)

        for (file in files) {
            if (file.name.startsWith(appId)) {
                url = file.url
                println(file.name)
            }
        }
    } catch (e: Exception) {
        println("ERROR " + e.toString())
        return null
    }

    return when (url) {
        "" -> null
        else -> DownloadQuery(url, appId, true)
    }
}

fun readAuthConfig(): AuthConfig {
    try {
        // Read first 2 lines of authconfig.txt
        val bufferedReader: BufferedReader = File("authconfig.txt").bufferedReader()
        val lines: List<String> =
                bufferedReader.useLines { text: Sequence<String> -> text.take(2).toList() }

        // Check if files are empty or not
        if (lines[0].isNullOrEmpty() || lines[1].isNullOrEmpty()) {
            println("Error: invalid authconfig.txt file")
            System.exit(1)
        } else {
            return AuthConfig(lines[0], lines[1])
        }
    } catch (e: Exception) {
        println("Error: invalid authconfig.txt file")
        System.exit(1)
    }

    // This line will never execute
    return AuthConfig("", "")
}

fun readAppIds(path: String): List<String> {
    val ls: MutableList<String> = mutableListOf()
    val inputStream: InputStream = File(path).inputStream()

    // Create a list of app ids which is free of duplicates
    inputStream.bufferedReader().forEachLine { line ->
        val id = line.trim()
        if (!ls.contains(id)) ls.add(id)
    }

    return ls
}

fun downloadFrom(query: DownloadQuery, outputPath: String) {
    val path = outputPath + query.appId + ".apk"
    URL(query.url).openStream().use { Files.copy(it, Paths.get(path)) }
}

fun log(text: String) {
    File("log.txt").appendText(text + "\n")
}

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("usage: gplay-downloader <appids file> <output path>")
        return
    }

    // Create output dir
    val outputPath = if (args[1].endsWith('/')) args[1] else args[1] + "/"
    File(outputPath).mkdir()

    // Read app ids
    val appids = readAppIds(args[0])

    // Read auth configuration and login
    val (email, aasToken) = readAuthConfig()
    val authData = AuthHelper.build(email, aasToken)

    // Download each app
    val len = appids.size
    appids.forEachIndexed { index, id ->
        println("Getting $index / $len : $id")
        if (File(outputPath + id + ".apk").exists()) {
            println("Already downloaded")
            return@forEachIndexed
        }

        val query = getDownloadQuery(id, authData)
        if (query == null) {
            log("NOTFOUND $id")
            println("Could not found $id")
        } else if (!query.isFree) {
            log("NOTFREE $id")
            println("$id is not free")
        } else {
            downloadFrom(query, outputPath)
        }
    }

    println("Finished")
}
