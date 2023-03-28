package gplay.downloader

import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier

class AuroraAuthProvider {
    companion object {
        const val URL_DISPENSER = "https://auroraoss.com/api/auth"
    }

    private val gson: Gson =
        GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC).create()

    private fun getAuthFromResponse(responseBytes: ByteArray): AuthData {
        val authData = gson.fromJson(String(responseBytes), AuthData::class.java)
        authData.locale = SpoofProvider.getSpoofLocale()
        authData.isAnonymous = true
        return authData
    }

    public fun fetchAuthData(): AuthData {
        var properties = SpoofProvider.getSpoofDeviceProperties()

        val playResponse = HttpClient.postAuth(URL_DISPENSER, gson.toJson(properties).toByteArray())

        return if (playResponse.isSuccessful) {
            getAuthFromResponse(playResponse.responseBytes)
        } else {
            when (playResponse.code) {
                404 -> throw Exception("Server unreachable")
                429 -> throw Exception("Oops, You are rate limited")
                else -> throw Exception(playResponse.errorString)
            }
        }
    }
}
