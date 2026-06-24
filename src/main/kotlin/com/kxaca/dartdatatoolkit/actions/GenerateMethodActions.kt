package com.kxaca.dartdatatoolkit.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.kxaca.dartdatatoolkit.parser.DartClassParser
import com.kxaca.dartdatatoolkit.generators.*

internal fun methodSignaturePattern(methodName: String): Regex? = when (methodName) {
    "toJson"   -> Regex("""\btoJson\s*\(\s*\)""")
    "fromJson" -> Regex("""factory\s+\w+\.fromJson\s*\(""")
    "toString" -> Regex("""String\s+toString\s*\(\s*\)""")
    "copyWith" -> Regex("""\bcopyWith\s*\(""")
    else       -> null
}

internal fun methodExistsInClassBody(classBody: String, methodName: String): Boolean {
    val pattern = methodSignaturePattern(methodName) ?: return false
    return pattern.containsMatchIn(classBody)
}

// Returns (startInclusive, endExclusive) in absolute document offsets for the existing method.
internal fun findMethodBounds(
    fullText: String,
    classStartOffset: Int,
    classEndOffset: Int,
    methodName: String
): Pair<Int, Int>? {
    val classBody = fullText.substring(classStartOffset, classEndOffset)
    val sigMatch = methodSignaturePattern(methodName)?.find(classBody) ?: return null

    // Walk back to the start of the method's first declaration line
    var lineStart = sigMatch.range.first
    while (lineStart > 0 && classBody[lineStart - 1] != '\n') lineStart--

    // If the line above is @override, include it
    var methodStart = lineStart
    if (lineStart > 0) {
        var prevLineStart = lineStart - 1
        while (prevLineStart > 0 && classBody[prevLineStart - 1] != '\n') prevLineStart--
        if (classBody.substring(prevLineStart, lineStart - 1).trim() == "@override") {
            methodStart = prevLineStart
        }
    }

    // Scan forward past the parameter list to find the method body opening brace
    var scanPos = sigMatch.range.last + 1
    var parenDepth = if (sigMatch.value.endsWith("(")) 1 else 0
    var methodBodyBrace = -1
    while (scanPos < classBody.length) {
        val ch = classBody[scanPos]
        if (parenDepth > 0) {
            when (ch) {
                '(' -> parenDepth++
                ')' -> parenDepth--
            }
        } else if (ch == '{') {
            methodBodyBrace = scanPos
            break
        }
        scanPos++
    }
    if (methodBodyBrace == -1) return null

    // Find the matching closing brace
    var depth = 1
    var i = methodBodyBrace + 1
    while (i < classBody.length && depth > 0) {
        when (classBody[i]) {
            '{' -> depth++
            '}' -> depth--
        }
        i++
    }
    if (depth != 0) return null

    if (i < classBody.length && classBody[i] == '\n') i++

    return Pair(classStartOffset + methodStart, classStartOffset + i)
}

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

        val classBody = document.text.substring(dartClass.startOffset, dartClass.endOffset)
        val existingBounds = if (methodExistsInClassBody(classBody, methodName)) {
            findMethodBounds(document.text, dartClass.startOffset, dartClass.endOffset, methodName)
        } else null

        val code = when (methodName) {
            "copyWith" -> CopyWithGenerator().generate(dartClass)
            "toJson"   -> ToJsonGenerator().generate(dartClass)
            "fromJson" -> FromJsonGenerator().generate(dartClass)
            "toString" -> ToStringGenerator().generate(dartClass)
            else -> return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            if (existingBounds != null) {
                val (start, end) = existingBounds
                document.deleteString(start, end)
                document.insertString(dartClass.endOffset - (end - start), code + "\n")
            } else {
                document.insertString(dartClass.endOffset, code + "\n")
            }
        }
    }

    // No update() override — actions are always visible in GenerateGroup.
    // Dart file validation is handled inside actionPerformed.
}

class GenerateCopyWithAction  : GenerateSingleMethodAction("copyWith")
class GenerateToJsonAction    : GenerateSingleMethodAction("toJson")
class GenerateFromJsonAction  : GenerateSingleMethodAction("fromJson")
class GenerateToStringAction  : GenerateSingleMethodAction("toString")
