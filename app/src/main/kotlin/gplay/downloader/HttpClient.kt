package gplay.downloader.aurora

import com.aurora.gplayapi.data.models.PlayResponse
import com.aurora.gplayapi.network.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayInputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.nio.file.Files
import java.nio.file.Paths

object HttpClient {
    val client = OkHttpClient

    fun setProxy(proxyHost: String, proxyPort: String, proxyUser: String, proxyPassword: String) {
        val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort.toInt()))
        client.setProxy(proxy, proxyUser, proxyPassword)
    }

    fun downloadFile(url: String, outputFile: String) {
        val response = client.get(url, emptyMap())
        if (response.code != 200 && response.code != 201) {
            throw Exception("DownloadError: http code $response.code")
        }
        ByteArrayInputStream(response.responseBytes).use { Files.copy(it, Paths.get(outputFile)) }
    }

    fun postAuth(url: String, body: ByteArray): PlayResponse {
        val versionName = System.getenv("AURORA_VERSION_NAME") ?: "4.1.1"
        val versionCode = System.getenv("AURORA_VERSION_CODE") ?: "41"

        val requestBody = body.toRequestBody("application/json".toMediaType(), 0, body.size)
        val response = client.post(url, mapOf("User-Agent" to "com.aurora.store-$versionName-$versionCode"), requestBody)

        return response
    }
}
