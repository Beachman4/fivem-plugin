package com.github.beachman4.fivem.utils

object StringUtil {
    fun removeQuotes(input: String): String {
        return input.replace("'", "").replace("\"", "")
    }
}
