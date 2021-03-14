package com.github.beachman4.fivem.data

data class Resource(
    var fxVersion: String = "",
    var version: String = "",
    var games: MutableList<String> = mutableListOf(),
    var clientScripts: MutableList<String> = mutableListOf(),
    var actualClientScriptsLoaded: MutableList<String> = mutableListOf(),
    var serverScripts: MutableList<String> = mutableListOf(),
    var actualServerScriptsLoaded: MutableList<String> = mutableListOf(),
    var author: String = "",
    var description: String = "",
    var sharedScripts: MutableList<String> = mutableListOf(),
    var actualSharedScriptsLoaded: MutableList<String> = mutableListOf(),
    var exports: MutableList<String> = mutableListOf(),
    var serverExports: MutableList<String> = mutableListOf(),
    var uiPage: String = "",
    var dataFiles: MutableMap<String, MutableList<String>> = mutableMapOf(),
    var thisIsAMap: Boolean = false,
    var serverOnly: Boolean = false,
    var loadScreen: String = "",
    var files: MutableList<String> = mutableListOf<String>(),
    var actualFilesLoaded: MutableList<String> = mutableListOf(),
    var dependencies: MutableList<String> = mutableListOf<String>(),
    var provide: String = "",
    var disableLazyNatives: Boolean = false,
    var clrDisableTaskScheduler: Boolean = false,
    var directory: String = ""
) {
    //
}
