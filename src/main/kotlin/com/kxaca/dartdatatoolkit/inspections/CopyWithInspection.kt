package com.kxaca.dartdatatoolkit.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

// Warns when a class's copyWith has no parameter for one or more of the class fields.
// Works on the file text (like the rest of the plugin), so it needs no Dart PSI classes.
class CopyWithInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (!file.name.endsWith(".dart")) return null

        val findings = CopyWithAnalysis.findOutdatedCopyWith(file.text)
        if (findings.isEmpty()) return null

        return findings.map { finding ->
            manager.createProblemDescriptor(
                file,
                TextRange(finding.nameStart, finding.nameEnd),
                "copyWith() is missing parameters for: ${finding.missing.joinToString(", ")}",
                ProblemHighlightType.WARNING,
                isOnTheFly,
                GenerateCopyWithQuickFix(finding.dartClass.name)
            )
        }.toTypedArray()
    }
}
