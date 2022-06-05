package com.github.beachman4.fivem.completion

import com.github.beachman4.fivem.completion.conditions.* // ktlint-disable no-wildcard-imports
import com.github.beachman4.fivem.completion.providers.* // ktlint-disable no-wildcard-imports
import com.intellij.codeInsight.completion.* // ktlint-disable no-wildcard-imports
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.patterns.PlatformPatterns.psiElement
import com.tang.intellij.lua.editor.completion.LuaLookupElement
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.psi.LuaLiteralExpr
import com.tang.intellij.lua.psi.LuaTypes

class FivemCodeCompletionContributor : CompletionContributor() {

    val keywords = listOf(
        "fx_version",
        "game",
        "description",
        "author",
        "version",
        "server_script",
        "shared_script",
        "client_script",
        "export",
        "server_export",
        "dependency",
        "ui_page",
        "file",
        "loadscreen",
        "this_is_a_map",
        "server_only",
        "provide",
        "disable_lazy_natives",
        "clr_disable_task_scheduler",
        "data_file"
    )

    val keywordsTable = listOf(
        "games",
        "server_scripts",
        "shared_scripts",
        "client_scripts",
        "exports",
        "server_exports",
        "dependencies",
        "files",
    )

    val validIdsFixed = listOf(
        "fx_version",
        "game",
        "games",
        "this_is_a_map",
        "server_only",
        "disable_lazy_natives",
        "clr_disable_task_scheduler",
        "resource_manifest_version"
    )

    val validIdsFile = listOf(
        "server_scripts",
        "server_script",
        "client_scripts",
        "client_script",
        "shared_scripts",
        "shared_script",
        "files",
        "file",
        "loadscreen",
        "ui_page"
    )

    val validDependency = listOf(
        "dependency",
        "dependencies"
    )

    val validExports = listOf(
        "exports",
        "export",
        "server_exports",
        "server_export"
    )

    init {
        extend(
            CompletionType.BASIC,
            psiElement(LuaTypes.STRING)
                .with(NativeEventPatternCondition(null)),
            EventCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            psiElement(LuaTypes.STRING)
                .with(ESXEventPatternCondition(null)),
            EventCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            psiElement(LuaTypes.STRING)
                .with(T9GEventPatternCondition(null)),
            EventCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            psiElement(LuaTypes.STRING)
                .with(MysqlPatternCondition(null)),
            MysqlParametersCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            psiElement(LuaTypes.STRING)
                .with(ResourceManifestIDPatternCondition(validIdsFile)),
            ResourceManifestFileIdFilesCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            psiElement(LuaTypes.STRING)
                .with(ResourceManifestIDPatternCondition(validIdsFixed)),
            ResourceManifestIdFixedCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            psiElement(LuaTypes.STRING)
                .with(ResourceManifestIDPatternCondition(validDependency)),
            ResourceManifestDependencyCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            psiElement(LuaTypes.STRING)
                .with(ResourceManifestIDPatternCondition(validExports)),
            ResourceManifestExportsCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            psiElement(LuaTypes.STRING)
                .with(ExportsPatternCondition()),
            ExportsResourcesCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            psiElement().with(ExportsFunctionPatternCondition()),
            ExportsFunctionsCompletionProvider()
        )
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val position = parameters.position
        val file = position.containingFile
        super.fillCompletionVariants(parameters, result)

        val resourceFiles = listOf("__resource.lua", "fxmanifest.lua")

        val luaLiteralExpr = position.parent

        @Suppress("MaxLineLength", "ComplexCondition")
        if (file.fileType === LuaFileType.INSTANCE && resourceFiles.contains(file.name) && !result.isStopped && luaLiteralExpr !is LuaLiteralExpr) {
            for (item in keywords) {
                val lookupElement = LuaLookupElement(item, false, null)

                val baseHandler = lookupElement.handler

                lookupElement.handler = InsertHandler<LookupElement> { insertionContext, localLookupElement ->
                    baseHandler.handleInsert(insertionContext, localLookupElement)
                    val document = insertionContext.document

                    document.insertString(insertionContext.tailOffset, " \"\"")

                    insertionContext.editor.caretModel.moveToOffset(insertionContext.tailOffset - 1)
                }

                result.addElement(lookupElement)
            }

            for (item in keywordsTable) {
                val lookupElement = LuaLookupElement(item, false, null)

                val baseHandler = lookupElement.handler

                lookupElement.handler = InsertHandler<LookupElement> { insertionContext, localLookupElement ->
                    baseHandler.handleInsert(insertionContext, localLookupElement)
                    val document = insertionContext.document

                    document.insertString(insertionContext.tailOffset, " {\n\t\n}")

                    insertionContext.editor.caretModel.moveToOffset(insertionContext.tailOffset - 2)
                }

                result.addElement(lookupElement)
            }

            result.stopHere()
        }
    }
}
