package com.github.beachman4.fivem.usages

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.lang.LuaParserDefinition
import com.tang.intellij.lua.lexer.LuaLexerAdapter
import com.tang.intellij.lua.psi.impl.LuaCallExprImpl
import com.tang.intellij.lua.psi.impl.LuaNameExprImpl

class EventsFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner? {
        return DefaultWordsScanner(
            LuaLexerAdapter(),
            TokenSet.create(),
            LuaParserDefinition.COMMENTS,
            LuaParserDefinition.STRINGS
        )
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return getDescriptiveName(element)
    }

    override fun getDescriptiveName(element: PsiElement): String {
        if (element is PsiNamedElement) {
            val name = element.name
            if (name != null) {
                return name
            }
        }
        return ""
    }

    override fun getType(element: PsiElement): String {
        return "Event"
    }

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        val parent = PsiTreeUtil.getParentOfType(psiElement, LuaCallExprImpl::class.java) ?: return false

        val firstChild = parent.firstChild

        if (firstChild is LuaNameExprImpl) {
            val validFunctions = listOf<String>(
                "AddEventHandler",
                "TriggerEvent",
                "TriggerClientEvent",
                "TriggerServerEvent",
                "RegisterNetEvent"
            )

            return validFunctions.contains(firstChild.text)
        }

        return false
    }
}
