package gplay.downloader

import java.io.File

data class AuthConfig(val email: String, val aasToken: String)

data class ProxyConfig(
        val user: String,
        val password: String,
        val hostname: String,
        val port: String
)

data class DownloaderConfig(val authConfig: AuthConfig, val proxyConfig: ProxyConfig?)

class ConfigManager(val authConfigPath: String, val proxyConfigPath: String? = null) {
    private val authConfigs: List<AuthConfig> = listOf()
    private val proxyConfigs: List<ProxyConfig> = listOf()
    private val useProxy: Boolean = !proxyConfigPath.isNullOrEmpty()

    private fun readAuthConfig(): List<AuthConfig> {
        val list: MutableList<AuthConfig> = mutableListOf()
        try {
            // Check if the file exists
            val configFile: File = File(authConfigPath)
            if (!configFile.exists()) {
                throw Exception("Given authconfig file does not exist")
            }

            for (line in configFile.readLines()) {
                // Check for empty line
                if (line.trim().isEmpty()){
                    continue
                }

                // Check if the line consists of 2 words
                val tokens = line.trim().split(" ")
                if (tokens.size != 2) {
                    throw Exception("Invalid authconfig structure")
                }

                val config = AuthConfig(tokens[0], tokens[1])
                list.add(config)
            }
        } catch (e: Exception) {
            println("Error: invalid authconfig.txt file")
            System.exit(1)
        }

        return list
    }
}
