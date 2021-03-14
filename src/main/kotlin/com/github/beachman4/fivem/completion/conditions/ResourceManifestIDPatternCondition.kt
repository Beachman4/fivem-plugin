package com.github.beachman4.fivem.completion.conditions

import com.github.beachman4.fivem.utils.StringUtil
import com.intellij.patterns.PatternCondition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.psi.impl.LuaExprStatImpl
import com.tang.intellij.lua.psi.impl.LuaNameExprImpl

class ResourceManifestIDPatternCondition(private val ids: List<String>) : PatternCondition<PsiElement>(null) {
    override fun accepts(t: PsiElement, context: ProcessingContext?): Boolean {
        val validFiles = listOf(
            "__resource.lua",
            "fxmanifest.lua"
        )

        if (!validFiles.contains(t.containingFile.name)) {
            return false
        }

        val luaExprStatImpl = PsiTreeUtil.getParentOfType(t, LuaExprStatImpl::class.java) ?: return false

        val child = PsiTreeUtil.findChildOfType(luaExprStatImpl, LuaNameExprImpl::class.java) ?: return false

        return ids.contains(StringUtil.removeQuotes(child.text))
    }
}
