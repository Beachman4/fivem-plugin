package com.github.beachman4.fivem.completion.providers

import com.github.beachman4.fivem.stub.FivemFileDataIndex
import com.github.beachman4.fivem.stub.FivemResourceIndex
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import com.tang.intellij.lua.editor.completion.LuaLookupElement
import com.tang.intellij.lua.psi.impl.LuaExprStatImpl
import com.tang.intellij.lua.psi.impl.LuaNameExprImpl

class ResourceManifestDependencyCompletionProvider : CompletionProvider<CompletionParameters>() {
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
        val luaExprStatImpl = PsiTreeUtil.getParentOfType(position, LuaExprStatImpl::class.java) ?: return

        val child = PsiTreeUtil.findChildOfType(luaExprStatImpl, LuaNameExprImpl::class.java) ?: return

        val keys = FileBasedIndex.getInstance().getAllKeys(FivemResourceIndex.KEY, position.project)

        keys.removeAll(resource.dependencies)

        for (key in keys) {
            val lookupElement = LuaLookupElement(key, false, null)

            result.addElement(lookupElement)
        }

        result.stopHere()
    }
}
