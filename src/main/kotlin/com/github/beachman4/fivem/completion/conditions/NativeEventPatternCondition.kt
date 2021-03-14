package com.github.beachman4.fivem.completion.conditions

import com.intellij.patterns.PatternCondition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.psi.impl.LuaCallExprImpl
import com.tang.intellij.lua.psi.impl.LuaListArgsImpl
import com.tang.intellij.lua.psi.impl.LuaLiteralExprImpl
import com.tang.intellij.lua.psi.impl.LuaNameExprImpl

class NativeEventPatternCondition(debugMethodName: String?) : PatternCondition<PsiElement>(debugMethodName) {
    override fun accepts(t: PsiElement, context: ProcessingContext?): Boolean {
        var luaLiteralExprImpl: LuaLiteralExprImpl? = PsiTreeUtil.getParentOfType(t, LuaLiteralExprImpl::class.java)
            ?: return false

        var luaListArgs = luaLiteralExprImpl!!.parent

        if (luaListArgs !is LuaListArgsImpl) {
            return false
        }

        val luaCallExpr = luaListArgs.parent

        if (luaCallExpr !is LuaCallExprImpl) {
            return false
        }

        val luaNameExpr = luaCallExpr.firstChild

        if (luaNameExpr is LuaNameExprImpl) {
            val validFunctions = listOf<String>(
                "AddEventHandler",
                "TriggerEvent",
                "TriggerClientEvent",
                "TriggerServerEvent",
                "RegisterNetEvent"
            )

            return validFunctions.contains(luaNameExpr.name)
        }

        return false
    }
}
