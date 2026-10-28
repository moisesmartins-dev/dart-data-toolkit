package com.kxaca.dartdatatoolkit.generators

import com.kxaca.dartdatatoolkit.parser.DartClass
import com.kxaca.dartdatatoolkit.parser.DartProperty

class CopyWithGenerator {

    fun generate(dartClass: DartClass): String {
        val properties = dartClass.copyableProperties
        if (properties.isEmpty()) return ""

        val params = properties
            .joinToString(",\n    ", postfix = ",") { prop -> buildParamDeclaration(prop) }

        val assignments = properties
            .joinToString(",\n      ") { prop ->
                "${prop.name}: ${prop.name} ?? this.${prop.name}"
            }

        return """
            |
            |  ${dartClass.constructorName} copyWith({
            |    $params
            |  }) {
            |    return ${dartClass.constructorName}(
            |      $assignments,
            |    );
            |  }
        """.trimMargin()
    }

    // All params are made nullable so callers can omit unchanged fields
    private fun buildParamDeclaration(prop: DartProperty): String = "${prop.type}? ${prop.name}"
}
