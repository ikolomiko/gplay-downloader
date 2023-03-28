package gplay.downloader.config

data class ProxyConfig(
        val hostname: String,
        val port: String,
        var user: String? = null,
        var password: String? = null,
)