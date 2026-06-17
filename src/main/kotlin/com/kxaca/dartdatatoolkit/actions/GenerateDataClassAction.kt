package com.kxaca.dartdatatoolkit.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.kxaca.dartdatatoolkit.parser.DartClassParser
import com.kxaca.dartdatatoolkit.generators.*

class GenerateDataClassAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

        if (!file.name.endsWith(".dart")) {
            showError(project, "This file is not a Dart file (.dart)")
            return
        }

        val document = editor.document
        val content = document.text
        val cursorOffset = editor.caretModel.offset

        val dartClass = DartClassParser().parseClass(content, cursorOffset)

        if (dartClass == null) {
            showError(project, "No Dart class found near the cursor.\nPlace the cursor inside the class.")
            return
        }

        if (dartClass.properties.isEmpty()) {
            showError(project, "Class '${dartClass.name}' has no properties.\nAdd properties before generating methods.")
            return
        }

        val dialog = ChooseMethodsDialog()
        if (!dialog.showAndGet()) return

        val selectedMethods = dialog.getSelectedMethods()

        if (selectedMethods.isEmpty()) {
            showError(project, "No methods were selected.")
            return
        }

        val generatedCode = generateMethods(dartClass, selectedMethods)
        insertCode(project, document, dartClass, generatedCode)

        Messages.showInfoMessage(
            project,
            "Methods generated successfully for class '${dartClass.name}':\n" +
            selectedMethods.joinToString(", ") { "$it()" },
            "Success"
        )
    }

    private fun generateMethods(dartClass: com.kxaca.dartdatatoolkit.parser.DartClass,
                                methods: Set<String>): String {
        val result = StringBuilder()
        if ("copyWith" in methods) result.append(CopyWithGenerator().generate(dartClass))
        if ("toJson"   in methods) result.append(ToJsonGenerator().generate(dartClass))
        if ("fromJson" in methods) result.append(FromJsonGenerator().generate(dartClass))
        if ("toString" in methods) result.append(ToStringGenerator().generate(dartClass))
        return result.toString()
    }

    private fun insertCode(project: Project, document: com.intellij.openapi.editor.Document,
                           dartClass: com.kxaca.dartdatatoolkit.parser.DartClass, code: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            document.insertString(dartClass.endOffset, code + "\n")
        }
    }

    private fun showError(project: Project, message: String) {
        Messages.showErrorDialog(project, message, "Generate Methods Error")
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabled = file?.name?.endsWith(".dart") == true
        e.presentation.isVisible = true
    }
}
