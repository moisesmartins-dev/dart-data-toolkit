package com.kxaca.dartdatatoolkit.generators

import com.google.gson.*
import com.kxaca.dartdatatoolkit.parser.DartClass
import com.kxaca.dartdatatoolkit.parser.DartProperty

class JsonToDartGenerator {

    fun generateClass(className: String, jsonString: String, methods: Set<String>): String {
        val element = JsonParser.parseString(jsonString)
        require(element.isJsonObject) { "The JSON must be an object at the root (starts with { })" }

        val fields = parseFields(element.asJsonObject)

        val dartClass = DartClass(
            name = className,
            properties = fields.map { DartProperty(name = it.dartName, type = it.type, isNullable = false) },
            startOffset = 0,
            endOffset = 0,
            constructorName = className
        )

        val fieldDecls = fields.joinToString("\n") { "  final ${it.type} ${it.dartName};" }
        val ctorParams = fields.joinToString(",\n    ") { "required this.${it.dartName}" }

        val methodsCode = buildString {
            if ("copyWith" in methods) append(CopyWithGenerator().generate(dartClass))
            if ("toJson"   in methods) append(ToJsonGenerator().generate(dartClass))
            if ("fromJson" in methods) append(FromJsonGenerator().generate(dartClass))
            if ("toString" in methods) append(ToStringGenerator().generate(dartClass))
        }

        return buildString {
            append("class $className {\n")
            append(fieldDecls)
            append("\n\n")
            append("  $className({\n")
            append("    $ctorParams,\n")
            append("  });\n")
            if (methodsCode.isNotEmpty()) {
                append(methodsCode)
                append("\n")
            }
            append("}")
        }
    }

    private data class Field(val dartName: String, val type: String)

    private fun parseFields(obj: JsonObject): List<Field> =
        obj.entrySet().map { (key, value) -> Field(toCamelCase(key), inferType(value)) }

    private fun inferType(el: JsonElement): String = when {
        el.isJsonNull      -> "dynamic"
        el.isJsonObject    -> "Map<String, dynamic>"
        el.isJsonArray     -> {
            val arr = el.asJsonArray
            if (arr.size() == 0) "List<dynamic>" else "List<${inferType(arr[0])}>"
        }
        el.isJsonPrimitive -> {
            val p = el.asJsonPrimitive
            when {
                p.isBoolean -> "bool"
                p.isNumber  -> if (p.asString.contains('.')) "double" else "int"
                else        -> "String"
            }
        }
        else -> "dynamic"
    }

    private fun toCamelCase(key: String): String =
        key.split('_', '-', ' ').mapIndexed { i, part ->
            if (i == 0) part else part.replaceFirstChar { it.uppercase() }
        }.joinToString("")
}
