package com.kxaca.dartdatatoolkit.generators

import com.google.gson.*
import com.kxaca.dartdatatoolkit.parser.DartClass
import com.kxaca.dartdatatoolkit.parser.DartProperty

class JsonToDartGenerator {

    fun generateClass(className: String, jsonString: String, methods: Set<String>): String {
        val element = JsonParser.parseString(jsonString)
        require(element.isJsonObject) { "The JSON must be an object at the root (starts with { })" }
        return buildClasses(className, element.asJsonObject, methods).joinToString("\n\n")
    }

    private fun buildClasses(
        className: String,
        jsonObject: JsonObject,
        methods: Set<String>
    ): List<String> {
        val nestedBlocks = mutableListOf<String>()
        val fields = mutableListOf<Field>()

        for ((key, value) in jsonObject.entrySet()) {
            val dartName = toCamelCase(key)
            if (value.isJsonObject) {
                val nestedName = dartName.replaceFirstChar { it.uppercaseChar() }
                nestedBlocks += buildClasses(nestedName, value.asJsonObject, methods)
                fields += Field(dartName = dartName, type = nestedName, isCustomClass = true)
            } else {
                fields += Field(dartName = dartName, type = inferType(value))
            }
        }

        return nestedBlocks + listOf(buildClassString(className, fields, methods))
    }

    private fun buildClassString(className: String, fields: List<Field>, methods: Set<String>): String {
        val dartClass = DartClass(
            name = className,
            properties = fields.map {
                DartProperty(name = it.dartName, type = it.type, isNullable = false, isCustomClass = it.isCustomClass)
            },
            startOffset = 0,
            endOffset = 0,
            constructorName = className
        )

        val fieldDecls = fields.joinToString("\n") { "  final ${it.type} ${it.dartName};" }
        val ctorParams = fields.joinToString(",\n    ") { "required this.${it.dartName}" }

        val methodsCode = listOfNotNull(
            if ("copyWith" in methods) CopyWithGenerator().generate(dartClass).takeIf { it.isNotEmpty() } else null,
            if ("toJson"   in methods) ToJsonGenerator().generate(dartClass).takeIf { it.isNotEmpty() } else null,
            if ("fromJson" in methods) FromJsonGenerator().generate(dartClass).takeIf { it.isNotEmpty() } else null,
            if ("toString" in methods) ToStringGenerator().generate(dartClass).takeIf { it.isNotEmpty() } else null,
        ).joinToString("\n")

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

    private data class Field(
        val dartName: String,
        val type: String,
        val isCustomClass: Boolean = false
    )

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
