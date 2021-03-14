package com.github.beachman4.fivem.completion.conditions

import com.intellij.patterns.PatternCondition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl
import com.tang.intellij.lua.psi.impl.LuaNameExprImpl

class ExportsFunctionPatternCondition : PatternCondition<PsiElement>(null) {
    override fun accepts(t: PsiElement, context: ProcessingContext?): Boolean {
        val firstLuaIndexExprImpl = PsiTreeUtil.getParentOfType(t, LuaIndexExprImpl::class.java) ?: return false

        val firstChild = firstLuaIndexExprImpl.firstChild

        if (firstChild is LuaIndexExprImpl) {
            val name = firstChild.firstChild

            if (name is LuaNameExprImpl) {
                return name.text == "exports"
            }
        }

        return false
    }
}
