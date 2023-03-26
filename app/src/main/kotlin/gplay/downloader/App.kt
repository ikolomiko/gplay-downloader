package gplay.downloader

import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParserException

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
    // Create argument parser
    val argParser = ArgumentParsers.newArgumentParser("gplay-downloader")
    argParser
            .addArgument("-a", "--app-ids")
            .dest("appids_path")
            .required(true)
            .help("path to the file containing app ids")
    argParser
            .addArgument("-c", "--auth-config")
            .dest("authconfig_path")
            .required(true)
            .help("path to the auth config file")
    argParser
            .addArgument("-o", "--output")
            .dest("output_path")
            .required(true)
            .help("the path where the apps will be downloaded to")
    argParser
            .addArgument("-p", "--proxy-config")
            .dest("proxyconfig_path")
            .required(false)
            .help("path to the proxy config file (optional)")
    argParser
            .addArgument("-s", "--single-apk")
            .action(Arguments.storeTrue())
            .required(false)
            .help("download only the main APK file")

    var appIdsPath: String = ""
    var authConfigPath: String = ""
    var outputPath: String = ""
    var proxyConfigPath: String? = null
    var singleApk: Boolean = false

    // Try to assign values to variables from parsed arguments
    try {
        val namespace = argParser.parseArgs(args)
        appIdsPath = namespace.getString("appids_path")
        authConfigPath = namespace.getString("authconfig_path")
        outputPath = namespace.getString("output_path")
        proxyConfigPath = namespace.getString("proxyconfig_path")
        singleApk = namespace.getBoolean("single_apk")
    } catch (e: ArgumentParserException) {
        argParser.handleError(e)
        System.exit(2)
    }

    try {
        // Check if the given files exist
        if (!File(appIdsPath).isFile()) throw FileNotFoundException(appIdsPath)
        if (!File(authConfigPath).isFile()) throw FileNotFoundException(authConfigPath)
        if (proxyConfigPath != null && !File(proxyConfigPath).isFile())
                throw FileNotFoundException(proxyConfigPath)

        // Create output directory
        outputPath = if (outputPath.endsWith('/')) outputPath else outputPath + "/"
        File(outputPath).mkdirs()
    } catch (e: FileNotFoundException) {
        println("File \"${e.message}\" not found")
        System.exit(1)
    } catch (e: Exception) {
        e.printStackTrace()
        System.exit(1)
    }

    // Initialize the logger
    val log = Logger(saveToFile = true, printToScreen = true, coloredOutput = true)

    // Read app ids
    val appIds = readAppIds(appIdsPath)

    log.status("=====Initialized=====")

    // Read auth configuration and login
    val configManager = ConfigManager(log, authConfigPath, proxyConfigPath)

    log.status("Initialized config manager")

    // Download each app
    val downloader = Downloader(log, outputPath, configManager, singleApk)
    downloader.downloadAll(appIds)

    log.status("=====Finished=====")
}
