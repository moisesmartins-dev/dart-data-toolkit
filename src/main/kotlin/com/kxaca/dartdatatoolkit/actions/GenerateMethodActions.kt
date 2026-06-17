package com.kxaca.dartdatatoolkit.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.kxaca.dartdatatoolkit.parser.DartClassParser
import com.kxaca.dartdatatoolkit.generators.*

abstract class GenerateSingleMethodAction(private val methodName: String) : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

        if (!file.name.endsWith(".dart")) return

        val document = editor.document
        val dartClass = DartClassParser().parseClass(document.text, editor.caretModel.offset)

        if (dartClass == null) {
            Messages.showErrorDialog(project, "No Dart class found near the cursor.", "Error")
            return
        }

        if (dartClass.properties.isEmpty()) {
            Messages.showErrorDialog(project, "Class '${dartClass.name}' has no properties.", "Error")
            return
        }

        val code = when (methodName) {
            "copyWith" -> CopyWithGenerator().generate(dartClass)
            "toJson"   -> ToJsonGenerator().generate(dartClass)
            "fromJson" -> FromJsonGenerator().generate(dartClass)
            "toString" -> ToStringGenerator().generate(dartClass)
            else -> return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            document.insertString(dartClass.endOffset, code + "\n")
        }
    }

    // No update() override — actions are always visible in GenerateGroup.
    // Dart file validation is handled inside actionPerformed.
}

class GenerateCopyWithAction  : GenerateSingleMethodAction("copyWith")
class GenerateToJsonAction    : GenerateSingleMethodAction("toJson")
class GenerateFromJsonAction  : GenerateSingleMethodAction("fromJson")
class GenerateToStringAction  : GenerateSingleMethodAction("toString")
