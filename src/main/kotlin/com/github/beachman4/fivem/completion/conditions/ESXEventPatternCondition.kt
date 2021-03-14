package com.github.beachman4.fivem.completion.conditions

import com.intellij.patterns.PatternCondition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl
import com.tang.intellij.lua.psi.impl.LuaListArgsImpl

class ESXEventPatternCondition(debugMethodName: String?) : PatternCondition<PsiElement>(debugMethodName) {
    override fun accepts(t: PsiElement, context: ProcessingContext?): Boolean {

        var parent = PsiTreeUtil.getParentOfType(t.parent, LuaListArgsImpl::class.java) ?: return false

        var previousSibling = parent.prevSibling

        if (previousSibling is LuaIndexExprImpl) {
            var children = PsiTreeUtil.findChildrenOfType(previousSibling, PsiElement::class.java)

            for (child in children) {
                if (child.text == "triggerEvent") {
                    return true
                }
            }
        }

        return false
    }
}
