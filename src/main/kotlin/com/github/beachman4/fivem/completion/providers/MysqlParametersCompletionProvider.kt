package com.github.beachman4.fivem.completion.providers

import com.github.beachman4.fivem.utils.PsiUtil
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.editor.completion.LuaLookupElement
import com.tang.intellij.lua.psi.impl.LuaListArgsImpl
import com.tang.intellij.lua.psi.impl.LuaLiteralExprImpl
import com.tang.intellij.lua.psi.impl.LuaTableExprImpl
import com.tang.intellij.lua.psi.impl.LuaTableFieldImpl

class MysqlParametersCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions
    (parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {

        val listOfParameters = mutableListOf<String>()

        val position = parameters.position

        val luaListArgsImpl = PsiUtil.getParentOfTypeRecursive(position, LuaListArgsImpl::class.java) ?: return

        val mysqlQuery = PsiTreeUtil.getChildOfType(luaListArgsImpl, LuaLiteralExprImpl::class.java) ?: return

        val mysqlString = mysqlQuery.text.removeSurrounding("\"", "\"").removeSurrounding("'", "'")

        val split = mysqlString.split(" ")

        for (item in split) {
            if (item.contains("@")) {
                listOfParameters.add(item.replace(",", "").replace("(", "").replace("`", "").replace(")", ""))
            }
        }

        val parent = PsiUtil.getParentOfTypeRecursive(position, LuaTableExprImpl::class.java)

        val children = PsiTreeUtil.findChildrenOfType(parent, LuaTableFieldImpl::class.java)

        val listOfParametersAlreadyUsed = mutableListOf<String>()

        for (child in children) {
            var foundParameter: PsiElement? = null
            var previous: PsiElement? = null
            var reassignableChild = child.firstChild

            while (foundParameter == null) {
                if (reassignableChild.text == "[") {
                    previous = reassignableChild
                    reassignableChild = reassignableChild.nextSibling
                } else if (reassignableChild.text == "]") {
                    foundParameter = previous
                } else {
                    previous = reassignableChild
                    reassignableChild = reassignableChild.nextSibling
                }
            }
            val foundParameterStripped = foundParameter.text.removeSurrounding("\"", "\"").removeSurrounding("'", "'")
            if (foundParameterStripped.startsWith("@")) {
                listOfParametersAlreadyUsed.add(foundParameterStripped)
            }
        }

        for (item in listOfParameters) {
            if (!listOfParametersAlreadyUsed.contains(item)) {
                val lookupElement = LuaLookupElement(item, false, null)

                result.addElement(lookupElement)
            }
        }

        result.stopHere()
    }
}
