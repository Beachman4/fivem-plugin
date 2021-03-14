package com.github.beachman4.fivem.completion.providers

import com.github.beachman4.fivem.data.Resource
import com.github.beachman4.fivem.stub.FivemFileDataIndex
import com.github.beachman4.fivem.stub.FivemResourceIndex
import com.github.beachman4.fivem.utils.StringUtil
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
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl
import com.tang.intellij.lua.psi.impl.LuaLiteralExprImpl
import java.nio.file.Paths

class ExportsFunctionsCompletionProvider : CompletionProvider<CompletionParameters>() {
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
        val fileDataClass = fileData["fileData"]
        val resource = FileBasedIndex.getInstance().getValues(
            FivemResourceIndex.KEY,
            fileDataClass!!.resource,
            GlobalSearchScope.projectScope(position.project)
        )[0]

        val firstLuaIndexExprImpl = PsiTreeUtil.getParentOfType(position, LuaIndexExprImpl::class.java) ?: return

        val firstChild = firstLuaIndexExprImpl.firstChild

        if (firstChild is LuaIndexExprImpl) {
            val literal = PsiTreeUtil.getChildOfType(firstChild, LuaLiteralExprImpl::class.java) ?: return

            val exportResource = StringUtil.removeQuotes(literal.text)

            val functions = getExportedFunctionsFromResource(
                resource,
                exportResource,
                fileDataClass!!.filePathFromResource,
                position.project
            )

            for (function in functions) {
                val lookupElement = LuaLookupElement(function, false, null)

                result.addElement(lookupElement)
            }

            result.stopHere()
        }
    }

    @Suppress("NestedBlockDepth")
    private fun getExportedFunctionsFromResource(
        currentResource: Resource,
        resourceName: String,
        currentFilePath: String,
        project: Project
    ): MutableList<String> {

        val resource = FileBasedIndex.getInstance().getValues(
            FivemResourceIndex.KEY,
            resourceName,
            GlobalSearchScope.projectScope(project)
        )[0]

        var files: List<String> = listOf()

        var clientExports = true

        when (true) {
            @Suppress("MaxLineLength")
            currentResource.actualClientScriptsLoaded.contains(currentFilePath) -> files = resource.actualClientScriptsLoaded
            currentResource.actualServerScriptsLoaded.contains(currentFilePath) -> {
                files = resource.actualServerScriptsLoaded
                clientExports = false
            }
        }

        val functions = mutableListOf<String>()
        for (file in files) {
            val path = Paths.get("${resource.directory}\\$file")

            val virtualFile = LocalFileSystem.getInstance().findFileByNioFile(path) ?: continue

            val fileData = FileBasedIndex.getInstance().getFileData(
                FivemFileDataIndex.KEY,
                virtualFile,
                project
            ).get("fileData") ?: continue

            for (function in fileData.functions) {
                if (clientExports) {
                    if (resource.exports.contains(function)) {
                        functions.add(function)
                    }
                } else {
                    if (resource.serverExports.contains(function)) {
                        functions.add(function)
                    }
                }
            }
        }

        return functions
    }
}
