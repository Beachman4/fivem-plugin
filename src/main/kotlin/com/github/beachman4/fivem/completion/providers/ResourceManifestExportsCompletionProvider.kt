package com.github.beachman4.fivem.completion.providers

import com.github.beachman4.fivem.data.Resource
import com.github.beachman4.fivem.stub.FivemFileDataIndex
import com.github.beachman4.fivem.stub.FivemResourceIndex
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import com.tang.intellij.lua.editor.completion.LuaLookupElement
import com.tang.intellij.lua.psi.impl.LuaExprStatImpl
import com.tang.intellij.lua.psi.impl.LuaNameExprImpl
import java.nio.file.Paths

class ResourceManifestExportsCompletionProvider : CompletionProvider<CompletionParameters>() {
    @Suppress("LongMethod")
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        var position = parameters.position
        val fileData = FileBasedIndex.getInstance().getFileData(
            FivemFileDataIndex.KEY,
            parameters.originalFile.virtualFile,
            position.project
        )
        val fileDataClass = fileData["fileData"]!!
        val resource = FileBasedIndex.getInstance().getValues(
            FivemResourceIndex.KEY,
            fileDataClass.resource,
            GlobalSearchScope.projectScope(position.project)
        )[0]
        val luaExprStatImpl = PsiTreeUtil.getParentOfType(position, LuaExprStatImpl::class.java) ?: return

        val child = PsiTreeUtil.findChildOfType(luaExprStatImpl, LuaNameExprImpl::class.java) ?: return

        var functions = mutableListOf<String>()

        when (child.text) {
            "export" -> {
                functions.addAll(
                    getFunctionsFromTypeOfFiles(
                        resource,
                        position.project,
                        resource.actualClientScriptsLoaded
                    )
                )
                functions.removeAll(resource.exports)
            }
            "exports" -> {
                functions.addAll(
                    getFunctionsFromTypeOfFiles(
                        resource,
                        position.project,
                        resource.actualClientScriptsLoaded
                    )
                )
                functions.removeAll(resource.exports)
            }
            "server_exports" -> {
                functions.addAll(
                    getFunctionsFromTypeOfFiles(
                        resource,
                        position.project,
                        resource.actualServerScriptsLoaded
                    )
                )
                functions.removeAll(resource.serverExports)
            }
            "server_export" -> {
                functions.addAll(
                    getFunctionsFromTypeOfFiles(
                        resource,
                        position.project,
                        resource.actualServerScriptsLoaded
                    )
                )

                functions.removeAll(resource.serverExports)
            }
        }

        for (function in functions) {
            val lookupElement = LuaLookupElement(function, false, null)

            result.addElement(lookupElement)
        }

        result.stopHere()
    }

    private fun getFunctionsFromTypeOfFiles(
        resource: Resource,
        project: Project,
        files: MutableList<String>
    ): MutableList<String> {
        val functions = mutableListOf<String>()
        for (file in files) {
            val path = Paths.get("${resource.directory}\\$file")

            val virtualFile = LocalFileSystem.getInstance().findFileByNioFile(path) ?: continue

            val fileData = FileBasedIndex.getInstance().getFileData(
                FivemFileDataIndex.KEY,
                virtualFile,
                project
            ).get("fileData") ?: continue

            functions.addAll(fileData.functions)
        }

        return functions
    }
}
