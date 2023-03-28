package gplay.downloader.config

import gplay.downloader.Logger
import gplay.downloader.SpoofProvider
import java.io.File

class ConfigManager(
        val log: Logger,
        val authConfigPath: String,
        val proxyConfigPath: String? = null,
        val useAuroraDispenserBackend: Boolean = false
) {
    private val useProxy: Boolean
    private var configIndex: Int
    private val downloaderConfigs: List<DownloaderConfig>

    init {
        useProxy = !proxyConfigPath.isNullOrEmpty()
        configIndex = -1

        val authConfigs =
                if (useAuroraDispenserBackend) {
                    generateSequence { AuroraAuthConfig() }.take(10).toList()
                } else {
                    readAuthConfigFile()
                }
        val proxyConfigs = if (useProxy) readProxyConfigFile() else emptyList()

        if (authConfigs.size < 1) {
            log.error("ConfigManager error: No auth configs found")
            log.status("=====Quitting=====")
            System.exit(1)
        }

        if (useProxy && proxyConfigs.size < 1) {
            log.error("ConfigManager error: No proxy configs found")
            log.status("=====Quitting=====")
            System.exit(1)
        }

        downloaderConfigs = generateDownloaderConfigs(authConfigs, proxyConfigs)
    }

    public fun getNextConfig(): DownloaderConfig {
        configIndex = (configIndex + 1) % downloaderConfigs.size

        // Swith to the next spoof device everytime this method
        // gets called (after every 3 apps)
        SpoofProvider.nextSpoofDeviceProperties()

        return downloaderConfigs[configIndex]
    }

    private fun readAuthConfigFile(): List<RegularAuthConfig> {
        val list: MutableList<RegularAuthConfig> = mutableListOf()
        try {
            // Check if the file exists
            val configFile: File = File(authConfigPath)
            if (!configFile.exists()) {
                throw Exception("Given auth config file does not exist")
            }

            // Auth config line format: <email> <aas token>
            for (line in configFile.readLines()) {
                // Check for empty line
                if (line.trim().isEmpty()) {
                    continue
                }

                // Check if the line consists of 2 words
                val tokens = line.trim().split(" ")
                if (tokens.size != 2) {
                    throw Exception("Invalid auth config file structure")
                }

                val config = RegularAuthConfig(tokens[0], tokens[1])
                list.add(config)
            }
        } catch (e: Exception) {
            log.error("ConfigManager error: invalid auth config file")
            val details = e.message.orEmpty()
            if (details.isNotEmpty()) {
                log.error(details)
            }
            log.status("=====Quitting=====")
            System.exit(1)
        }

        return list
    }

    private fun readProxyConfigFile(): List<ProxyConfig> {
        val list: MutableList<ProxyConfig> = mutableListOf()
        try {
            // Check if the file exists
            val configFile: File = File(proxyConfigPath)
            if (!configFile.exists()) {
                throw Exception("Given proxy config file does not exist")
            }

            // Proxy config line format:
            // <hostname> <password> <user (optional)> <password (optional)>
            for (line in configFile.readLines()) {
                // Check for empty line
                if (line.trim().isEmpty()) {
                    continue
                }

                // Check if the line consists of 2 or 4 words
                val tokens = line.trim().split(" ")
                if (tokens.size != 2 && tokens.size != 4) {
                    throw Exception("Invalid proxy config file structure")
                }

                val config = ProxyConfig(tokens[0], tokens[1])
                if (tokens.size == 4) {
                    config.user = tokens[2]
                    config.password = tokens[3]
                }
                list.add(config)
            }
        } catch (e: Exception) {
            log.error("ConfigManager error: invalid proxy config file")
            val details = e.message.orEmpty()
            if (details.isNotEmpty()) {
                log.error(details)
            }
            log.status("=====Quitting=====")
            System.exit(1)
        }

        return list
    }

    private fun generateDownloaderConfigs(
            authConfigs: List<IAuthConfig>,
            proxyConfigs: List<ProxyConfig>
    ): List<DownloaderConfig> {
        val list: MutableList<DownloaderConfig> = mutableListOf()
        var proxyIndex = 0

        // When using proxies, make sure that proxies are as much distributed to auth configs as possible
        // Further explanation ahead:
        // If (# of proxies) > (# of auths), each auth gets a distinct proxy. The remaining proxies are ignored.
        // If (# of proxies) = (# of auths), each auth gets a distinct proxy and there won't be any unused proxy.
        // If (# of proxies) < (# of auths), proxies are distributed to auths as sparse as possible. Some or all
        // proxies may be used by different auths. In all cases, each auth gets exactly one proxy.
        for (auth in authConfigs) {
            val dlConfig = DownloaderConfig(auth)
            if (useProxy) {
                dlConfig.proxyConfig = proxyConfigs[proxyIndex]
                proxyIndex = (proxyIndex + 1) % proxyConfigs.size
            }

            list.add(dlConfig)
        }

        return list
    }
}
