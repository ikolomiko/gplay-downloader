package gplay.downloader

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
    if (args.size != 2) {
        println("usage: gplay-downloader <appids file> <output path>")
        System.exit(2)
    }

    val log = Logger(saveToFile = true, printToScreen = true, coloredOutput = true)

    // Create output dir
    val outputPath = if (args[1].endsWith('/')) args[1] else args[1] + "/"
    File(outputPath).mkdir()

    // Read app ids
    val appIds = readAppIds(args[0])

    log.status("=====Initialized=====")

    // Read auth configuration and login
    /// TODO replace placeholders below
    val configManager = ConfigManager(log, "authConfigPath", "proxyConfigPath")

    log.status("Initialized config manager")

    // Download each app
    val downloader = Downloader(log, outputPath, configManager)
    downloader.downloadAll(appIds)

    log.status("=====Finished=====")
}
