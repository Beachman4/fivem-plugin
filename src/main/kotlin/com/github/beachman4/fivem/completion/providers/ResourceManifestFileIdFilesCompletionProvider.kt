package com.github.beachman4.fivem.completion.providers

import com.github.beachman4.fivem.stub.FileToResourceIndex
import com.github.beachman4.fivem.stub.FivemFileDataIndex
import com.github.beachman4.fivem.stub.FivemResourceIndex
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import com.tang.intellij.lua.editor.completion.LuaLookupElement
import com.tang.intellij.lua.psi.impl.LuaExprStatImpl
import com.tang.intellij.lua.psi.impl.LuaNameExprImpl

class ResourceManifestFileIdFilesCompletionProvider : CompletionProvider<CompletionParameters>() {
    @Suppress("ComplexMethod", "LongMethod")
    override fun addCompletions
    (parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        var position = parameters.position
        val fileData = FileBasedIndex.getInstance().getFileData(
            FileToResourceIndex.KEY,
            parameters.originalFile.virtualFile,
            position.project
        )
        val resourceName = fileData["resource"] ?: return
        val resource = FileBasedIndex.getInstance().getValues(
            FivemResourceIndex.KEY,
            resourceName,
            GlobalSearchScope.projectScope(position.project)
        )[0]
        val filesFromResources = mutableListOf<String>()
        val luaExprStatImpl = PsiTreeUtil.getParentOfType(position, LuaExprStatImpl::class.java) ?: return

        val child = PsiTreeUtil.findChildOfType(luaExprStatImpl, LuaNameExprImpl::class.java) ?: return

        FileBasedIndex.getInstance().getFilesWithKey(
            FivemFileDataIndex.KEY,
            setOf("fileData"),
            Processor { t ->
                val localFileData = FileBasedIndex.getInstance().getFileData(
                    FivemFileDataIndex.KEY,
                    t,
                    position.project
                )

                if (localFileData["fileData"]!!.resource.equals(resourceName)) {
                    filesFromResources.add(
                        t.presentableUrl.replace(
                            "\\",
                            "/"
                        ).replace(resource.directory.replace("\\", "/"), "").removePrefix("/")
                    )
                }

                return@Processor true
            },
            GlobalSearchScope.projectScope(position.project)
        )

        filesFromResources.removeAll(listOf("fxmanifest.lua", "__resource.lua"))

        when (child.text) {
            "client_scripts" -> filesFromResources.removeAll(resource.clientScripts)
            "client_script" -> filesFromResources.removeAll(resource.clientScripts)
            "server_scripts" -> filesFromResources.removeAll(resource.serverScripts)
            "server_script" -> filesFromResources.removeAll(resource.serverScripts)
            "shared_scripts" -> filesFromResources.removeAll(resource.sharedScripts)
            "shared_script" -> filesFromResources.removeAll(resource.sharedScripts)
            "files" -> filesFromResources.removeAll(resource.files)
            "file" -> filesFromResources.removeAll(resource.files)
            "loadscreen" -> {
                for (file in filesFromResources) {
                    if (!file.endsWith(".html")) {
                        filesFromResources.remove(file)
                    }
                }

                filesFromResources.remove(resource.loadScreen)
            }
            "ui_page" -> {
                for (file in filesFromResources) {
                    if (!file.endsWith(".html")) {
                        filesFromResources.remove(file)
                    }
                }

                filesFromResources.remove(resource.uiPage)
            }
        }

        for (file in filesFromResources) {
            val lookupElement = LuaLookupElement(file, false, null)

            result.addElement(lookupElement)
        }

        result.stopHere()
    }
}
