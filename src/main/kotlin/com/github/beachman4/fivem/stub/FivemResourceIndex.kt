package com.github.beachman4.fivem.stub

import com.beust.klaxon.Klaxon
import com.github.beachman4.fivem.data.Resource
import com.github.beachman4.fivem.utils.PsiUtil
import com.github.beachman4.fivem.utils.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.* // ktlint-disable no-wildcard-imports
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.psi.impl.* // ktlint-disable no-wildcard-imports
import java.io.DataInput
import java.io.DataOutput
import java.nio.file.FileSystems
import java.nio.file.Paths

class FivemResourceIndex : FileBasedIndexExtension<String, Resource>() {

    companion object {
        val KEY = ID.create<String, Resource>("com.beachman4.fivem.fivem_resource")
    }
    override fun getValueExternalizer() = object : DataExternalizer<Resource> {
        override fun save(out: DataOutput, value: Resource?) {
            out.writeUTF(Klaxon().toJsonString(value))
        }

        override fun read(input: DataInput): Resource {
            return Klaxon().parse<Resource>(input.readUTF())!!
        }
    }

    override fun getName(): ID<String, Resource> {
        return KEY
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    @Suppress("LongMethod", "ComplexMethod")
    override fun getIndexer(): DataIndexer<String, Resource, FileContent> {
        return DataIndexer { fileContent: FileContent ->
            val map = mutableMapOf<String, Resource>()

            val resource = Resource()

            val directory = fileContent.file.parent

            val children = PsiTreeUtil.findChildrenOfType(fileContent.psiFile, LuaExprStatImpl::class.java)

            for (child in children) {
                val name = PsiTreeUtil.findChildOfType(child, LuaNameExprImpl::class.java) ?: continue
                val singleArg = PsiTreeUtil.findChildOfType(child, LuaSingleArgImpl::class.java) ?: continue
                var isTable = false
                var value: PsiElement? = null

                if (singleArg!!.firstChild is LuaTableExprImpl) {
                    isTable = true
                    value = singleArg.firstChild
                } else {
                    value = singleArg.firstChild
                }

                val text = StringUtil.removeQuotes(name!!.text)

                when (text) {
                    "fx_version" -> resource.fxVersion = StringUtil.removeQuotes(value.text)
                    "game" -> resource.games.add(StringUtil.removeQuotes(value.text))
                    "games" -> resource.games.addAll(convertLuaTableToList(value))
                    "description" -> resource.description = StringUtil.removeQuotes(value.text)
                    "author" -> resource.author = StringUtil.removeQuotes(value.text)
                    "version" -> resource.version = StringUtil.removeQuotes(value.text)
                    "server_scripts" -> {
                        val scripts = convertLuaTableToList(value)

                        resource.serverScripts.addAll(scripts)

                        for (script in scripts) {
                            if (script.contains("**") || script.contains("*")) {
                                val matchedFiles = convertFileWithWildcardToListOfFiles(script, directory)

                                resource.actualServerScriptsLoaded.addAll(matchedFiles)
                            } else {
                                resource.actualServerScriptsLoaded.add(StringUtil.removeQuotes(script))
                            }
                        }
                    }
                    "server_script" -> {
                        resource.serverScripts.add(StringUtil.removeQuotes(value.text))

                        if (value.text.contains("**") || value.text.contains("*")) {
                            val matchedFiles = convertFileWithWildcardToListOfFiles(value.text, directory)

                            resource.actualServerScriptsLoaded.addAll(matchedFiles)
                        } else {
                            resource.actualServerScriptsLoaded.add(StringUtil.removeQuotes(value.text))
                        }
                    }
                    "shared_scripts" -> {
                        val scripts = convertLuaTableToList(value)

                        resource.sharedScripts.addAll(scripts)

                        for (script in scripts) {
                            if (script.contains("**") || script.contains("*")) {
                                val matchedFiles = convertFileWithWildcardToListOfFiles(script, directory)

                                resource.actualSharedScriptsLoaded.addAll(matchedFiles)
                            } else {
                                resource.actualSharedScriptsLoaded.add(StringUtil.removeQuotes(script))
                            }
                        }
                    }
                    "shared_script" -> {
                        resource.sharedScripts.add(StringUtil.removeQuotes(value.text))

                        if (value.text.contains("**") || value.text.contains("*")) {
                            val matchedFiles = convertFileWithWildcardToListOfFiles(value.text, directory)

                            resource.actualSharedScriptsLoaded.addAll(matchedFiles)
                        } else {
                            resource.actualSharedScriptsLoaded.add(StringUtil.removeQuotes(value.text))
                        }
                    }
                    "client_scripts" -> {
                        val scripts = convertLuaTableToList(value)

                        resource.clientScripts.addAll(scripts)

                        for (script in scripts) {
                            if (script.contains("**") || script.contains("*")) {
                                val matchedFiles = convertFileWithWildcardToListOfFiles(script, directory)

                                resource.actualClientScriptsLoaded.addAll(matchedFiles)
                            } else {
                                resource.actualClientScriptsLoaded.add(StringUtil.removeQuotes(script))
                            }
                        }
                    }
                    "client_script" -> {
                        resource.clientScripts.add(StringUtil.removeQuotes(value.text))

                        if (value.text.contains("**") || value.text.contains("*")) {
                            val matchedFiles = convertFileWithWildcardToListOfFiles(value.text, directory)

                            resource.actualClientScriptsLoaded.addAll(matchedFiles)
                        } else {
                            resource.actualClientScriptsLoaded.add(StringUtil.removeQuotes(value.text))
                        }
                    }
                    "exports" -> resource.exports.addAll(convertLuaTableToList(value))
                    "export" -> resource.exports.add(StringUtil.removeQuotes(value.text))
                    "server_exports" -> resource.serverExports.addAll(convertLuaTableToList(value))
                    "server_export" -> resource.serverExports.add(StringUtil.removeQuotes(value.text))
                    "dependencies" -> resource.dependencies.addAll(convertLuaTableToList(value))
                    "dependency" -> resource.dependencies.add(StringUtil.removeQuotes(value.text))
                    "ui_page" -> resource.uiPage = StringUtil.removeQuotes(value.text)
                    "file" -> {
                        resource.files.add(StringUtil.removeQuotes(value.text))

                        if (value.text.contains("**") || value.text.contains("*")) {
                            val matchedFiles = convertFileWithWildcardToListOfFiles(value.text, directory)

                            resource.actualFilesLoaded.addAll(matchedFiles)
                        } else {
                            resource.actualFilesLoaded.add(StringUtil.removeQuotes(value.text))
                        }
                    }
                    "files" -> {
                        val scripts = convertLuaTableToList(value)

                        resource.files.addAll(scripts)

                        for (script in scripts) {
                            if (script.contains("**") || script.contains("*")) {
                                val matchedFiles = convertFileWithWildcardToListOfFiles(script, directory)

                                resource.actualFilesLoaded.addAll(matchedFiles)
                            } else {
                                resource.actualFilesLoaded.add(StringUtil.removeQuotes(script))
                            }
                        }
                    }
                    "loadscreen" -> resource.loadScreen = StringUtil.removeQuotes(value.text)
                    "this_is_a_map" -> resource.thisIsAMap = resourceBoolToActualBool(value.text)
                    "server_only" -> resource.serverOnly = resourceBoolToActualBool(value.text)
                    "provide" -> resource.provide = StringUtil.removeQuotes(value.text)
                    "disable_lazy_natives" -> resource.disableLazyNatives = resourceBoolToActualBool(value.text)
                    @Suppress("MaxLineLength")
                    "clr_disable_task_scheduler" -> resource.clrDisableTaskScheduler = resourceBoolToActualBool(value.text)
                    "data_file" -> {
                        val dataFileType = StringUtil.removeQuotes(value.text)

                        val fileLocation = PsiTreeUtil.getChildOfType(child.firstChild, LuaSingleArgImpl::class.java)

                        if (!resource.dataFiles.containsKey(dataFileType)) {
                            resource.dataFiles[dataFileType] = mutableListOf()
                        }

                        resource.dataFiles[dataFileType]!!.add(StringUtil.removeQuotes(fileLocation!!.text))
                    }
                }
            }

            val directoryName = directory.name

            resource.directory = directory.presentableUrl

            map[directoryName] = resource

            return@DataIndexer map
        }
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        val validNames = arrayListOf<String>("__resource.lua", "fxmanifest.lua")
        return FileBasedIndex.InputFilter { file: VirtualFile ->
            file.fileType === LuaFileType.INSTANCE && validNames.contains(file.name)
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    private fun convertLuaTableToList(value: PsiElement): MutableList<String> {
        return PsiUtil.splitLuaTableIntoStrings(value as LuaTableExprImpl)
    }

    private fun resourceBoolToActualBool(text: String): Boolean {
        return text == "yes"
    }

    private fun convertFileWithWildcardToListOfFiles(file: String, directory: VirtualFile): MutableList<String> {
        val directoryUrl = directory.presentableUrl
        val test = Paths.get("$directoryUrl".replace("\\", "/")).toAbsolutePath().normalize()
        val url = directoryUrl.replace("\\", "/").replace(
            "[",
            "\\[",
        ).replace("]", "\\]")
        val matcher = FileSystems.getDefault().getPathMatcher("glob:$url/$file")

        val files = recursiveFileSearch(directory)

        val matchedFiles = mutableListOf<String>()

        for (item in files) {
            val path = Paths.get(item.presentableUrl).toAbsolutePath().normalize()

            if (matcher.matches(path)) {
                matchedFiles.add(
                    item.presentableUrl.replace(
                        directory.presentableUrl,
                        ""
                    ).replace("\\", "/").removePrefix("/")
                )
            }
        }

        return matchedFiles
    }

    private fun recursiveFileSearch(directory: VirtualFile): MutableList<VirtualFile> {
        val files = mutableListOf<VirtualFile>()

        for (child in directory.children) {
            if (child.isDirectory) {
                files.addAll(recursiveFileSearch(child))
            } else {
                files.add(child)
            }
        }

        return files
    }
}
