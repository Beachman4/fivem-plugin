package com.github.beachman4.fivem.data

data class FivemFileData(
    var resource: String = "",
    var functions: MutableList<String> = mutableListOf(),
    var functionParameters: MutableMap<String, MutableList<String>> = mutableMapOf(),
    var filePathFromResource: String = "",
    var eventHandlers: MutableList<String> = mutableListOf(),
    var triggeredEvents: MutableList<String> = mutableListOf()
) {
    //
}
