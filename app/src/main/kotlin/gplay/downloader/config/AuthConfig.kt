package gplay.downloader.config

import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.AuthHelper
import gplay.downloader.AuroraAuthProvider
import gplay.downloader.SpoofProvider

interface IAuthConfig {
    fun login(): AuthData
}

class AuroraAuthConfig : IAuthConfig {
    override fun login(): AuthData {
        return AuroraAuthProvider().fetchAuthData()
    }
}

class RegularAuthConfig(val email: String, val aasToken: String) : IAuthConfig {
    override fun login(): AuthData {
        return AuthHelper.build(
                email,
                aasToken,
                SpoofProvider.getSpoofDeviceProperties(),
        )
    }
}
