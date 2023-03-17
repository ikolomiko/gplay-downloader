package gplay.downloader

import java.io.File

data class AuthConfig(val email: String, val aasToken: String)

data class ProxyConfig(
        val hostname: String,
        val port: String,
        var user: String? = null,
        var password: String? = null,
)

data class DownloaderConfig(val authConfig: AuthConfig, var proxyConfig: ProxyConfig? = null)

class ConfigManager(val authConfigPath: String, val proxyConfigPath: String? = null) {
    private val useProxy: Boolean
    private var configIndex: Int
    private val downloaderConfigs: List<DownloaderConfig>

    /// TODO convert println statements to logger statements
    /// TODO actually make use of custom exception messages

    init {
        useProxy = !proxyConfigPath.isNullOrEmpty()
        configIndex = -1
        val authConfigs = readAuthConfigFile()
        val proxyConfigs = if (useProxy) readProxyConfigFile() else emptyList()

        if (authConfigs.size < 1) {
            println("Error: No auth configs found")
            System.exit(1)
        }

        if (useProxy && proxyConfigs.size < 1) {
            println("Error: No proxy configs found")
            System.exit(1)
        }

        downloaderConfigs = generateDownloaderConfigs(authConfigs, proxyConfigs)
    }

    public fun getNextConfig(): DownloaderConfig {
        configIndex = (configIndex + 1) % downloaderConfigs.size
        return downloaderConfigs[configIndex]
    }

    private fun readAuthConfigFile(): List<AuthConfig> {
        val list: MutableList<AuthConfig> = mutableListOf()
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

                val config = AuthConfig(tokens[0], tokens[1])
                list.add(config)
            }
        } catch (e: Exception) {
            println("Error: invalid auth config file")
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
                if (tokens.size != 2 || tokens.size != 4) {
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
            println("Error: invalid proxy config file")
            System.exit(1)
        }

        return list
    }

    private fun generateDownloaderConfigs(
            authConfigs: List<AuthConfig>,
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
