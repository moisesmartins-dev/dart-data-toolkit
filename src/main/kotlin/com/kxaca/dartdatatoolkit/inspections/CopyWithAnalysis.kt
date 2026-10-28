package com.kxaca.dartdatatoolkit.inspections

import com.kxaca.dartdatatoolkit.actions.findMethodBounds
import com.kxaca.dartdatatoolkit.actions.methodSignaturePattern
import com.kxaca.dartdatatoolkit.generators.CopyWithGenerator
import com.kxaca.dartdatatoolkit.parser.DartClass
import com.kxaca.dartdatatoolkit.parser.DartClassParser

// A copyWith declaration that does not cover every copyable field of its class.
// nameStart/nameEnd are absolute offsets of the "copyWith" identifier in the analysed text.
data class CopyWithFinding(
    val dartClass: DartClass,
    val nameStart: Int,
    val nameEnd: Int,
    val missing: List<String>
)

// A text edit that replaces [start, end) with [text].
data class CopyWithReplacement(val start: Int, val end: Int, val text: String)

object CopyWithAnalysis {

    private val identifier = Regex("""[A-Za-z_]\w*""")
    private val functionTypeParam = Regex("""\bFunction\s*(?:<[^>]*>)?\s*\(""")

    fun findOutdatedCopyWith(text: String): List<CopyWithFinding> =
        DartClassParser().parseAllClasses(text).mapNotNull { analyzeClass(text, it) }

    // Builds the edit that regenerates the copyWith of [className] in place, or null when it cannot be located.
    fun buildReplacement(text: String, className: String): CopyWithReplacement? {
        val dartClass = DartClassParser().parseAllClasses(text).firstOrNull { it.name == className } ?: return null
        val (start, end) = findMethodBounds(text, dartClass.startOffset, dartClass.endOffset, "copyWith") ?: return null

        val code = CopyWithGenerator().generate(dartClass)
        if (code.isEmpty()) return null

        // generate() starts with a blank line; the replaced range already begins at the method's own line
        return CopyWithReplacement(start, end, code.trimStart('\n') + "\n")
    }

    private fun analyzeClass(text: String, dartClass: DartClass): CopyWithFinding? {
        val copyable = dartClass.copyableProperties
        if (copyable.isEmpty()) return null

        val classBody = text.substring(dartClass.startOffset, dartClass.endOffset)
        val signature = methodSignaturePattern("copyWith")?.find(classBody) ?: return null

        // Only a declaration at class level counts; a call such as x.copyWith(...) inside another method does not
        if (braceDepthAt(classBody, signature.range.first) != 1) return null

        val params = extractParamNames(classBody, signature.range.last) ?: return null
        val missing = copyable.map { it.name }.filter { it !in params }
        if (missing.isEmpty()) return null

        val nameStart = dartClass.startOffset + signature.range.first
        return CopyWithFinding(dartClass, nameStart, nameStart + "copyWith".length, missing)
    }

    private fun braceDepthAt(text: String, index: Int): Int {
        var depth = 0
        for (i in 0 until index) {
            when (text[i]) {
                '{' -> depth++
                '}' -> depth--
            }
        }
        return depth
    }

    // [openParen] is the index of the "(" that starts the copyWith parameter list.
    // Returns null when the list cannot be read, so callers stay silent instead of guessing.
    internal fun extractParamNames(text: String, openParen: Int): Set<String>? {
        var depth = 0
        var closeParen = -1
        for (i in openParen until text.length) {
            when (text[i]) {
                '(' -> depth++
                ')' -> { depth--; if (depth == 0) { closeParen = i; break } }
            }
        }
        if (closeParen == -1) return null

        val names = mutableSetOf<String>()
        for (param in splitTopLevel(text.substring(openParen + 1, closeParen))) {
            names.add(paramName(param) ?: continue)
        }
        return names
    }

    // Splits on commas that are not nested inside (), <>, [] or {}. The braces/brackets that open an optional or
    // named group at the top level are dropped, so "String a, {int? b, int? c}" yields three entries.
    private fun splitTopLevel(params: String): List<String> {
        val parts = mutableListOf<String>()
        val current = StringBuilder()
        var depth = 0
        for (ch in params) {
            when (ch) {
                '(', '<' -> depth++
                ')', '>' -> if (depth > 0) depth--
                '{', '[' -> { if (depth == 0) continue else depth++ }
                '}', ']' -> { if (depth == 0) continue else depth-- }
            }
            if (ch == ',' && depth == 0) {
                parts.add(current.toString())
                current.setLength(0)
            } else {
                current.append(ch)
            }
        }
        parts.add(current.toString())
        return parts.map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun paramName(param: String): String? {
        // Drop the default value: "int? age = 3" -> "int? age"
        val declaration = param.substringBefore('=').trim()
        if (declaration.isEmpty()) return null

        // "this.name" / "super.name" initializing formals
        Regex("""\b(?:this|super)\.(\w+)""").find(declaration)?.let { return it.groupValues[1] }

        // Old-style function parameter "void onTap(int x)": the name sits before the first "("
        val parenIndex = declaration.indexOf('(')
        if (parenIndex != -1 && !functionTypeParam.containsMatchIn(declaration)) {
            return identifier.findAll(declaration.substring(0, parenIndex)).lastOrNull()?.value
        }

        // Otherwise the name is the last identifier: "required String? name", "void Function(int) cb"
        return identifier.findAll(declaration).lastOrNull()?.value
    }
}
