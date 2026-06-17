package com.kxaca.dartdatatoolkit.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.kxaca.dartdatatoolkit.generators.JsonToDartGenerator

class JsonToDartAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor  = e.getData(CommonDataKeys.EDITOR) ?: return

        val dialog = JsonToDartDialog()
        if (!dialog.showAndGet()) return

        val json = dialog.getJsonText()
        if (json.isEmpty()) {
            Messages.showErrorDialog(project, "No JSON was provided.", "Error")
            return
        }

        val code = try {
            JsonToDartGenerator().generateClass(
                className = dialog.getClassName(),
                jsonString = json,
                methods   = dialog.getSelectedMethods()
            )
        } catch (ex: Exception) {
            Messages.showErrorDialog(project, "Invalid JSON: ${ex.message}", "Error")
            return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            val doc    = editor.document
            val offset = editor.caretModel.offset
            doc.insertString(offset, "\n$code\n")
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = true
        e.presentation.isEnabled = e.getData(CommonDataKeys.EDITOR) != null
    }
}
