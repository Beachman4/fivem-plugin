package com.github.beachman4.fivem.completion.providers

import com.github.beachman4.fivem.stub.EventsStubIndex
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import com.tang.intellij.lua.editor.completion.LuaLookupElement

class EventCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions
    (parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val keys = FileBasedIndex.getInstance().getAllKeys(EventsStubIndex.KEY, position.project)

        for (key in keys) {
            val lookupElement = LuaLookupElement(key, false, null)

            result.addElement(lookupElement)
        }

        result.stopHere()
    }
}
