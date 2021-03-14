package com.github.beachman4.fivem.completion.providers

import com.github.beachman4.fivem.stub.FivemFileDataIndex
import com.github.beachman4.fivem.stub.FivemResourceIndex
import com.github.beachman4.fivem.utils.StringUtil
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import com.tang.intellij.lua.editor.completion.LuaLookupElement
import com.tang.intellij.lua.psi.impl.LuaExprStatImpl
import com.tang.intellij.lua.psi.impl.LuaNameExprImpl
import com.tang.intellij.lua.psi.impl.LuaSingleArgImpl
import com.tang.intellij.lua.psi.impl.LuaTableExprImpl

class ResourceManifestIdFixedCompletionProvider : CompletionProvider<CompletionParameters>() {
    @Suppress("LongMethod")
    override fun addCompletions
    (parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
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

        val singleArg = PsiTreeUtil.findChildOfType(luaExprStatImpl, LuaSingleArgImpl::class.java)

        var isTable = false
        var value: PsiElement? = null

        if (singleArg!!.firstChild is LuaTableExprImpl) {
            isTable = true
            value = singleArg.firstChild
        } else {
            value = singleArg.firstChild
        }

        val text = StringUtil.removeQuotes(child!!.text)

        var list = mutableListOf<String>()
        var booleanList = mutableListOf("yes", "no")

        when (text) {
            "fx_version" -> {
                list = mutableListOf("adamant", "bodacious", "cerulean")
                list.remove(resource.fxVersion)
            }
            "resource_manifest_version" -> {
                list = mutableListOf(
                    "00000000-0000-0000-0000-000000000000",
                    "77731fab-63ca-442c-a67b-abc70f28dfa5",
                    "f15e72ec-3972-4fe4-9c7d-afc5394ae207",
                    "44febabe-d386-4d18-afbe-5e627f4af937",
                    "05cfa83c-a124-4cfa-a768-c24a5811d8f9"
                )
            }
            "game" -> {
                list = mutableListOf("gta5", "redm", "common")
                list.removeAll(resource.games)
            }
            "games" -> {
                list = mutableListOf("gta5", "redm", "common")
                list.removeAll(resource.games)
            }
            "this_is_a_map" -> list = booleanList
            "server_only" -> list = booleanList
            "disable_lazy_natives" -> list = booleanList
            "clr_disable_task_scheduler" -> list = booleanList
        }

        for (item in list) {
            val lookupElement = LuaLookupElement(item, false, null)

            result.addElement(lookupElement)
        }

        result.stopHere()
    }
}
