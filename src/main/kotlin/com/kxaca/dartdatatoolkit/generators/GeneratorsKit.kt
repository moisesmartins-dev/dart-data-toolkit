package com.kxaca.dartdatatoolkit.generators

import com.kxaca.dartdatatoolkit.parser.DartClass

class ToJsonGenerator {

    fun generate(dartClass: DartClass): String {
        if (dartClass.properties.isEmpty()) return ""

        val jsonMap = dartClass.properties
            .joinToString(",\n      ") { prop ->
                if (prop.isCustomClass) "'${prop.name}': ${prop.name}.toJson()"
                else "'${prop.name}': ${prop.name}"
            }

        return """
            |
            |  Map<String, dynamic> toJson() {
            |    return {
            |      $jsonMap,
            |    };
            |  }
        """.trimMargin()
    }
}

class FromJsonGenerator {

    fun generate(dartClass: DartClass): String {
        if (dartClass.properties.isEmpty()) return ""

        val assignments = dartClass.properties
            .joinToString(",\n      ") { prop ->
                if (prop.isCustomClass) "${prop.name}: ${prop.type}.fromJson(json['${prop.name}'] as Map<String, dynamic>)"
                else "${prop.name}: json['${prop.name}']"
            }

        return """
            |
            |  factory ${dartClass.constructorName}.fromJson(Map<String, dynamic> json) {
            |    return ${dartClass.constructorName}(
            |      $assignments,
            |    );
            |  }
        """.trimMargin()
    }
}

class ToStringGenerator {

    fun generate(dartClass: DartClass): String {
        if (dartClass.properties.isEmpty()) return ""

        val lines = dartClass.properties
            .joinToString("\n        ") { prop ->
                when {
                    prop.isCustomClass && prop.isNullable -> "'  ${prop.name}: \${${prop.name}?.toString()},\\n'"
                    prop.isCustomClass -> "'  ${prop.name}: \${${prop.name}.toString()},\\n'"
                    else -> "'  ${prop.name}: \$${prop.name},\\n'"
                }
            }

        return """
            |
            |  @override
            |  String toString() {
            |    return '${dartClass.constructorName}(\n'
            |        $lines
            |        ')';
            |  }
        """.trimMargin()
    }
}
