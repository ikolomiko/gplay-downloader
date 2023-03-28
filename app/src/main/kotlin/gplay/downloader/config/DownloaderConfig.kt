package gplay.downloader.config

data class DownloaderConfig(val authConfig: IAuthConfig, var proxyConfig: ProxyConfig? = null)
