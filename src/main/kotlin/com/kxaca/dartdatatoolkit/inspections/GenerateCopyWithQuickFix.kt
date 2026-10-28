package com.kxaca.dartdatatoolkit.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

// Regenerates the copyWith of the given class in place. The class is looked up again by name when the fix
// runs, because the document may have changed since the inspection ran.
class GenerateCopyWithQuickFix(private val className: String) : LocalQuickFix {

    override fun getFamilyName(): String = "Generate copyWith"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val file = descriptor.psiElement?.containingFile ?: return
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(file) ?: return

        val replacement = CopyWithAnalysis.buildReplacement(document.text, className) ?: return

        documentManager.doPostponedOperationsAndUnblockDocument(document)
        document.replaceString(replacement.start, replacement.end, replacement.text)
        documentManager.commitDocument(document)
    }
}
