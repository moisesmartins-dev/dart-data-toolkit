package com.kxaca.dartdatatoolkit.parser

data class DartProperty(
    val name: String,
    val type: String,
    val isNullable: Boolean = false,
    val isRequired: Boolean = false,
    val defaultValue: String? = null,
    val isCustomClass: Boolean = false
)

data class DartClass(
    val name: String,
    val properties: List<DartProperty>,
    val startOffset: Int,
    val endOffset: Int,
    val constructorName: String = name
)

class DartClassParser {

    private val builtinDartTypes = setOf(
        "String", "int", "double", "bool", "num", "dynamic", "Object",
        "DateTime", "Duration", "Uri", "BigInt", "void"
    )
    private val collectionPrefixes = setOf("List", "Map", "Set", "Iterable", "Stream", "Future")

    private fun isCustomClass(type: String): Boolean {
        if (type in builtinDartTypes) return false
        return collectionPrefixes.none { type == it || type.startsWith("$it<") }
    }

    fun parseClass(content: String, cursorOffset: Int): DartClass? {
        val classRegex = Regex(
            """(?:abstract\s+)?class\s+(\w+)\s*(?:extends\s+\w+)?\s*(?:with\s+[\w\s,]+)?\s*\{"""
        )

        val classMatch = classRegex.findAll(content)
            .lastOrNull { it.range.first < cursorOffset } ?: return null

        val className = classMatch.groupValues[1]
        val classStartOffset = classMatch.range.first

        val openBraceIndex = content.indexOf('{', classMatch.range.last)
        if (openBraceIndex == -1) return null

        val closeBraceIndex = findMatchingBrace(content, openBraceIndex)
        if (closeBraceIndex == -1) return null

        val classBody = content.substring(openBraceIndex + 1, closeBraceIndex)

        return DartClass(
            name = className,
            properties = parseProperties(classBody),
            startOffset = classStartOffset,
            endOffset = closeBraceIndex,
            constructorName = className
        )
    }

    private fun stripMethodBodies(classBody: String): String {
        val sb = StringBuilder()
        var depth = 0
        for (ch in classBody) {
            when (ch) {
                '{' -> depth++
                '}' -> depth--
                else -> if (depth == 0) sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun parseProperties(classBody: String): List<DartProperty> {
        val properties = mutableListOf<DartProperty>()
        val strippedBody = stripMethodBodies(classBody)

        // Matches: final String? name; / final int age; / List<String> tags; etc.
        val propertyRegex = Regex(
            """(?:(?:final|late|static)\s+)?([A-Za-z]\w*(?:<[^>]+>)?)(\?)\s+(\w+)\s*(?:=\s*[^;]+)?;|(?:(?:final|late|static)\s+)?([A-Za-z]\w*(?:<[^>]+>)?)\s+(\w+)\s*(?:=\s*[^;]+)?;"""
        )

        propertyRegex.findAll(strippedBody).forEach { match ->
            // Groups 1+2+3 = nullable type; groups 4+5 = non-nullable type
            val (type, name, isNullable) = if (match.groupValues[1].isNotEmpty()) {
                Triple(match.groupValues[1], match.groupValues[3], true)
            } else {
                Triple(match.groupValues[4], match.groupValues[5], false)
            }

            val dartKeywords = setOf(
                "return", "class", "get", "set", "if", "else", "for", "while", "switch",
                "case", "break", "const", "var", "dynamic", "extends", "implements",
                "with", "new", "throw", "try", "catch", "import", "export", "typedef",
                "enum", "super", "void", "async", "await", "yield", "abstract", "factory"
            )
            if (name.isEmpty() || type in dartKeywords || name in dartKeywords) return@forEach

            properties.add(DartProperty(name = name, type = type, isNullable = isNullable, isCustomClass = isCustomClass(type)))
        }

        return properties
    }

    private fun findMatchingBrace(content: String, openIndex: Int): Int {
        var depth = 1
        for (i in openIndex + 1 until content.length) {
            when (content[i]) {
                '{' -> depth++
                '}' -> { depth--; if (depth == 0) return i }
            }
        }
        return -1
    }
}
