package com.github.beachman4.fivem.stub

import com.beust.klaxon.Klaxon
import com.github.beachman4.fivem.data.FivemFileData
import com.github.beachman4.fivem.data.FivemVariables
import com.github.beachman4.fivem.utils.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.util.indexing.* // ktlint-disable no-wildcard-imports
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.psi.LuaTypes
import com.tang.intellij.lua.psi.impl.* // ktlint-disable no-wildcard-imports
import java.io.DataInput
import java.io.DataOutput

class FivemFileDataIndex : FileBasedIndexExtension<String, FivemFileData>() {

    companion object {
        val KEY = ID.create<String, FivemFileData>("com.beachman4.fivem.fivem_file")
    }

    override fun getValueExternalizer() = object : DataExternalizer<FivemFileData> {
        override fun save(out: DataOutput, value: FivemFileData?) {
            out.writeUTF(Klaxon().toJsonString(value))
        }

        override fun read(input: DataInput): FivemFileData {
            return Klaxon().parse<FivemFileData>(input.readUTF())!!
        }
    }

    override fun getName(): ID<String, FivemFileData> {
        return KEY
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    @Suppress("LongMethod", "ComplexMethod")
    override fun getIndexer(): DataIndexer<String, FivemFileData, FileContent> {
        return DataIndexer { fileContent: FileContent ->
            val map = mutableMapOf<String, FivemFileData>()

            val fileData = FivemFileData()
            val psiFile = fileContent.psiFile

            if (fileContent.file.parent == null) {
                return@DataIndexer map
            }

            var file = fileContent.file.parent
            var previous = fileContent.file
            var found = false

            var path = psiFile.name

            var i = 0
            @Suppress("MagicNumber")
            val max = 20

            while (!found) {
                i++

                if (i > max) {
                    file = null
                    break
                }

                if (file == null) {
                    break
                }

                var children = file.children

                for (child in children) {
                    if (child.name == "fxmanifest.lua" || child.name == "__resource.lua") {
                        found = true
                    }
                }

                if (!found) {
                    if ((file.name.startsWith("[") && file.name.endsWith("]")) || file.name.equals("resources")) {
                        file = previous
                        found = true
                    } else {
                        path = "${file.name}/$path"
                        previous = file
                        file = file.parent
                    }
                }
            }

            if (file != null) {

                val resourceFiles = listOf("__resource.lua", "fxmanifest.lua")

                if (psiFile.fileType === LuaFileType.INSTANCE && !resourceFiles.contains(psiFile.name)) {
                    val functionDeclarations = PsiTreeUtil.findChildrenOfType(psiFile, LuaFuncDefImpl::class.java)

                    for (child in functionDeclarations) {
                        var functionName: PsiElement? = null

                        var firstChild: PsiElement? = child.firstChild

                        loop@ while (firstChild != null) {
                            if (firstChild.nextSibling.elementType == LuaTypes.ID) {
                                functionName = firstChild.nextSibling
                                break@loop
                            } else if (firstChild.nextSibling.text === "local") {
                                firstChild = null
                            } else {
                                firstChild = firstChild.nextSibling
                            }
                        }

                        if (functionName === null) {
                            continue
                        }

                        fileData.functions.add(functionName.text)

                        val parameters = PsiTreeUtil.findChildrenOfType(child, LuaParamNameDefImpl::class.java)

                        if (parameters.isNotEmpty()) {
                            fileData.functionParameters[functionName.text] = mutableListOf()
                        }

                        for (parameter in parameters) {
                            fileData.functionParameters[functionName.text]!!.add(parameter.text)
                        }
                    }

                    val luaExprStatImpls = PsiTreeUtil.findChildrenOfType(psiFile, LuaExprStatImpl::class.java)

                    for (child in luaExprStatImpls) {
                        val name = PsiTreeUtil.findChildOfType(child, LuaNameExprImpl::class.java) ?: continue

                        val luaListArgs = PsiTreeUtil.findChildOfType(child, LuaListArgsImpl::class.java) ?: continue

                        val firstLiteral = PsiTreeUtil.findChildOfType(
                            luaListArgs,
                            LuaLiteralExprImpl::class.java
                        ) ?: continue

                        val removedQuotes = StringUtil.removeQuotes(firstLiteral.text)

                        if (name.text == "AddEventHandler") {
                            if (!fileData.eventHandlers.contains(removedQuotes)) {
                                fileData.eventHandlers.add(removedQuotes)
                            }
                        } else if (FivemVariables.triggeredEventsKeys.contains(name.text)) {
                            if (!fileData.triggeredEvents.contains(removedQuotes)) {
                                fileData.triggeredEvents.add(removedQuotes)
                            }
                        }
                    }
                }

                fileData.filePathFromResource = path
                fileData.resource = file.name

                map["fileData"] = fileData
            }

            return@DataIndexer map
        }
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file ->
            val extension = file.extension
            val validTypes = listOf(
                "lua"
            )

            return@InputFilter validTypes.contains(extension)
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
}
