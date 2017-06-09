package com.bluemedora.example.sqlserver

import com.bluemedora.exuno.common.ExUnoTimer
import com.bluemedora.exuno.logging.ExUnoLogger

fun <T> time(rawId: String, body: () -> T): T {
    ExUnoLogger.debug(rawId)
    val id = rawId.stripNewLines().limitLengthTo(100)
    ExUnoTimer.captureTime(id)
    try {
        return body()
    } finally {
        ExUnoTimer.logSecondsSince(id)
    }
}

private fun String.stripNewLines() = this.replace(Regex("[\n\r]+"), " ").replace(Regex("\\s+"), " ")
private fun String.limitLengthTo(length: Int) = if (this.length <= length) {
    this
} else {
    this.take(length - 5) + "[...]"
}