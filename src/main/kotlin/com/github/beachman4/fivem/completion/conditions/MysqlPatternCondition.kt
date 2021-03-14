package com.github.beachman4.fivem.completion.conditions

import com.github.beachman4.fivem.utils.PsiUtil
import com.intellij.patterns.PatternCondition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.psi.impl.LuaExprStatImpl
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl

class MysqlPatternCondition(debugMethodName: String?) : PatternCondition<PsiElement>(debugMethodName) {

    val mysqlTypes = listOf<String>(
        "MySQL.Async.insert",
        "MySQL.Async.execute",
        "MySQL.Async.fetchScalar",
        "MySQL.Async.fetchAll",
        "MySQL.Sync.insert",
        "MySQL.Sync.execute",
        "MySQL.Sync.fetchScalar",
        "MySQL.Sync.fetchAll"
    )

    override fun accepts(t: PsiElement, context: ProcessingContext?): Boolean {
        var luaExpr = PsiUtil.getParentOfTypeRecursive(t, LuaExprStatImpl::class.java) ?: return false

        var child = PsiTreeUtil.findChildOfType(luaExpr, LuaIndexExprImpl::class.java) ?: return false

        return mysqlTypes.contains(child.text)
    }
}
