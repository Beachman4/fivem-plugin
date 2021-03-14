package com.github.beachman4.fivem.completion

import com.github.beachman4.fivem.completion.conditions.ExportsFunctionPatternCondition
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.* // ktlint-disable no-wildcard-imports
import com.intellij.util.ProcessingContext

class FivemReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            psiElement().with(ExportsFunctionPatternCondition()),
            ExportReferenceFunctionProvider()
        )
    }

    internal inner class ExportReferenceFunctionProvider : PsiReferenceProvider() {
        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
            element

            return PsiReference.EMPTY_ARRAY
        }
    }
}
