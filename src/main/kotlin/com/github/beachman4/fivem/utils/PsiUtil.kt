package com.github.beachman4.fivem.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.psi.impl.LuaLiteralExprImpl
import com.tang.intellij.lua.psi.impl.LuaTableExprImpl
import com.tang.intellij.lua.psi.impl.LuaTableFieldImpl

object PsiUtil {

    fun <T : PsiElement?> getParentOfTypeRecursive(psiElement: PsiElement, aClass: Class<T>): T? {
        var parent: PsiElement? = psiElement.parent
        var found: T? = null

        while (found == null) {
            if (parent == null || parent.parent == null) {
                break
            }
            if (aClass.isInstance(parent)) {
                found = aClass.cast(parent)
            } else {
                parent = parent.parent
            }
        }

        return found
    }

    fun splitLuaTableIntoStrings(input: LuaTableExprImpl): MutableList<String> {

        val list = mutableListOf<String>()

        val children = PsiTreeUtil.findChildrenOfType(input, LuaTableFieldImpl::class.java)

        for (child in children) {
            val firstChild = child.firstChild ?: continue

            if (firstChild is LuaLiteralExprImpl) {
                list.add(StringUtil.removeQuotes(firstChild.firstChild.text))
            }
        }

        return list
    }
}
