package gplay.downloader

import java.net.JarURLConnection
import java.util.Locale
import java.util.Properties

object SpoofProvider {
    private var deviceIndex = 0
    private var deviceSpoofProperties = Properties()
    private var deviceFilenames: List<String> = emptyList()

    init {
        readDeviceFilenames()
        laodSpoofDeviceProperties()
    }

    private fun filenameValid(name: String): Boolean {
        return name.startsWith("device-props") && name.endsWith(".properties")
    }

    private fun readDeviceFilenames() {
        var filenames = emptyList<String>()
        val handle = javaClass.classLoader.getResources("device-props/")
        if (handle.hasMoreElements()) {
            val url = handle.nextElement()
            val urlcon = url.openConnection() as JarURLConnection

            val entries = urlcon.jarFile.entries()
            filenames = entries.toList().map { it.name }.filter(::filenameValid)
        }
        deviceFilenames = filenames
    }

    private fun laodSpoofDeviceProperties() {
        // Load the device properties at current index
        val filename = deviceFilenames[deviceIndex]
        val bufferedStream =
            ClassLoader.getSystemClassLoader().getResourceAsStream(filename).buffered()

        deviceSpoofProperties.load(bufferedStream)
    }

    fun nextSpoofDeviceProperties(): Properties {
        // Increment the file index, load the new property file and return it
        val length = deviceFilenames.size
        deviceIndex = (deviceIndex + 1) % length
        laodSpoofDeviceProperties()

        return deviceSpoofProperties
    }

    fun getSpoofLocale(): Locale {
        return Locale.getDefault()
    }

    fun getSpoofDeviceProperties(): Properties {
        return deviceSpoofProperties
    }
}
