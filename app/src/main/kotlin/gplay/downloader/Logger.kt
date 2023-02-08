package gplay.downloader

import java.io.File
import java.time.LocalDateTime

class Logger(
        val saveToFile: Boolean = true,
        val printToScreen: Boolean = true,
        val coloredOutput: Boolean = true,
) {
    enum class LogLevel {
        INFO,
        STATUS,
        SUCCESS,
        WARNING,
        DQERROR,
        DLERROR,
    }

    private val ColorCode: Array<String> =
            arrayOf(
                    "\u001b[37m", // white - info
                    "\u001b[36m", // cyan - status
                    "\u001b[32m", // green - success
                    "\u001b[33m", // yellow - warning
                    "\u001b[35m", // purple - dqerror
                    "\u001b[31m", // red - dlerror
            )

    fun color(text: String, level: LogLevel = LogLevel.INFO): String {
        val index = level.ordinal
        return "${ColorCode[index]}${text}\u001b[0m"
    }

    public fun log(text: String, level: LogLevel = LogLevel.INFO) {
        val formatted = "${LocalDateTime.now()}\t${level.name}\t$text\n"

        if (saveToFile) {
            File("log.txt").appendText(formatted)
        }

        if (printToScreen) {
            if (coloredOutput) {
                println(color(text, level))
            } else {
                println(text)
            }
        }
    }

    public fun info(text: String) = log(text, LogLevel.INFO)
    public fun status(text: String) = log(text, LogLevel.STATUS)
    public fun success(text: String) = log(text, LogLevel.SUCCESS)
    public fun warning(text: String) = log(text, LogLevel.WARNING)
    public fun dqError(text: String) = log(text, LogLevel.DQERROR)
    public fun dlError(text: String) = log(text, LogLevel.DLERROR)
}
