package gplay.downloader

import com.aurora.gplayapi.helpers.AuthHelper
import java.io.BufferedReader
import java.io.File
import java.io.InputStream



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

fun main(args: Array<String>) {
    val log = Logger(saveToFile = true, printToScreen = true, coloredOutput = true)

    if (args.size != 2) {
        println("usage: gplay-downloader <appids file> <output path>")
        return
    }

    // Create output dir
    val outputPath = if (args[1].endsWith('/')) args[1] else args[1] + "/"
    File(outputPath).mkdir()

    // Read app ids
    val appIds = readAppIds(args[0])

    log.status("=====Initialized=====")

    // Read auth configuration and login
    val (email, aasToken) = readAuthConfig()
    val authData = AuthHelper.build(email, aasToken)

    log.status("Logged in")

    // Download each app
    val downloader = Downloader(log, outputPath, authData)
    downloader.downloadAll(appIds)

    log.status("=====Finished=====")
}
