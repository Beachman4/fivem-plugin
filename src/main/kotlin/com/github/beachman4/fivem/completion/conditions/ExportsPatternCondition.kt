package com.github.beachman4.fivem.completion.conditions

import com.github.beachman4.fivem.data.FivemVariables
import com.intellij.patterns.PatternCondition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl
import com.tang.intellij.lua.psi.impl.LuaNameExprImpl

class ExportsPatternCondition : PatternCondition<PsiElement>(null) {
    override fun accepts(t: PsiElement, context: ProcessingContext?): Boolean {

        if (FivemVariables.resourceManifestFiles.contains(t.containingFile.name)) {
            return false
        }

        val luaIndexExprImpl = PsiTreeUtil.getParentOfType(t, LuaIndexExprImpl::class.java) ?: return false

        val firstChild = luaIndexExprImpl.firstChild

        if (firstChild is LuaNameExprImpl) {
            return firstChild.text == "exports"
        }

        return false
    }
}
