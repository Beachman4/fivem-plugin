package com.github.beachman4.fivem.completion.providers

import com.github.beachman4.fivem.stub.FileToResourceIndex
import com.github.beachman4.fivem.stub.FivemResourceIndex
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import com.tang.intellij.lua.editor.completion.LuaLookupElement

class ExportsResourcesCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
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

        for (dependency in resource.dependencies) {
            var lookupElement = LuaLookupElement(dependency, false, null)

            result.addElement(lookupElement)
        }

        result.stopHere()
    }
}
