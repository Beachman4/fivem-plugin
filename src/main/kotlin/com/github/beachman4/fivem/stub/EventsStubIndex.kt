package com.github.beachman4.fivem.stub

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.* // ktlint-disable no-wildcard-imports
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.intellij.util.io.VoidDataExternalizer
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.psi.LuaNameExpr
import com.tang.intellij.lua.psi.impl.LuaCallExprImpl
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl
import com.tang.intellij.lua.psi.impl.LuaListArgsImpl
import com.tang.intellij.lua.psi.impl.LuaLiteralExprImpl

class EventsStubIndex : FileBasedIndexExtension<String, Void>() {

    companion object {
        val KEY = ID.create<String, Void>("com.beachman4.fivem.event_keys")
    }

    override fun getValueExternalizer(): DataExternalizer<Void> {
        return VoidDataExternalizer.INSTANCE
    }

    override fun getName(): ID<String, Void> {
        return KEY
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, Void?, FileContent> {
        return DataIndexer { fileContent: FileContent ->
            val map = mutableMapOf<String, Void?>()

            val psiFile = fileContent.psiFile

            getFunctionsForEvents(psiFile, map)
            val children = PsiTreeUtil.findChildrenOfType(psiFile, LuaIndexExprImpl::class.java)

            for (child in children) {
                for (item in PsiTreeUtil.getChildrenOfTypeAsList(child, PsiElement::class.java)) {
                    if (item.text == "triggerEvent") {
                        val sibling = child.nextSibling

                        if (sibling is LuaListArgsImpl) {
                            val event = PsiTreeUtil.findChildOfType(sibling, LuaLiteralExprImpl::class.java) ?: continue

                            val text = event.text.removeSurrounding("\"", "\"").removeSurrounding("'", "'")

                            if (text.isEmpty()) {
                                continue
                            }

                            map[text] = null
                        }
                    }
                }
            }

            return@DataIndexer map
        }
    }

    private fun getFunctionsForEvents(psiFile: PsiFile, map: MutableMap<String, Void?>) {
        val children = PsiTreeUtil.findChildrenOfType(psiFile, LuaNameExpr::class.java)

        for (child in children) {

            val validTypes = listOf<String>(
                "AddEventHandler",
                "TriggerEvent",
                "TriggerClientEvent",
                "TriggerServerEvent",
                "RegisterNetEvent"
            )

            if (!validTypes.contains(child.name)) {
                continue
            }

            val parent = PsiTreeUtil.getParentOfType(child, LuaCallExprImpl::class.java) ?: continue
            val luaListArgs = PsiTreeUtil.getChildrenOfTypeAsList(parent, LuaListArgsImpl::class.java)[0] ?: continue

            val luaLiteralExp = PsiTreeUtil.getChildOfType(luaListArgs, LuaLiteralExprImpl::class.java) ?: continue

            val foundChild = PsiTreeUtil.getChildOfType(luaLiteralExp, PsiElement::class.java)

            if (foundChild != null) {
                val text = foundChild.text.removeSurrounding("\"", "\"").removeSurrounding("'", "'")

                if (!text.isEmpty()) {
                    map[text] = null
                }
            }
        }
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        val invalidNames = arrayListOf<String>("__resource.lua", "fxmanifest.lua")
        return FileBasedIndex.InputFilter { file: VirtualFile ->
            file.fileType === LuaFileType.INSTANCE && !invalidNames.contains(file.name)
        }
    }
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
}
