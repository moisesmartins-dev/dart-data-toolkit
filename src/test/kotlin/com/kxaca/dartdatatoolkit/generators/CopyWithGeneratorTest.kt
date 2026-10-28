package com.kxaca.dartdatatoolkit.generators

import com.kxaca.dartdatatoolkit.parser.DartClass
import com.kxaca.dartdatatoolkit.parser.DartProperty
import org.junit.Assert.*
import org.junit.Test

class CopyWithGeneratorTest {

    private val generator = CopyWithGenerator()

    private fun cls(vararg props: DartProperty) =
        DartClass(name = "User", properties = props.toList(), startOffset = 0, endOffset = 0)

    @Test
    fun `skips static and private properties`() {
        val result = generator.generate(
            cls(
                DartProperty("name", "String"),
                DartProperty("maxAge", "int", isStatic = true),
                DartProperty("_secret", "String")
            )
        )
        assertTrue(result.contains("String? name"))
        assertFalse(result.contains("maxAge"))
        assertFalse(result.contains("_secret"))
    }

    @Test
    fun `returns empty string when only static or private properties exist`() {
        val result = generator.generate(cls(DartProperty("_secret", "String"), DartProperty("max", "int", isStatic = true)))
        assertEquals("", result)
    }

    @Test
    fun `returns empty string when no properties`() {
        assertEquals("", generator.generate(cls()))
    }

    @Test
    fun `contains method signature`() {
        val result = generator.generate(cls(DartProperty("name", "String")))
        assertTrue(result.contains("User copyWith({"))
    }

    @Test
    fun `all params are nullable`() {
        val result = generator.generate(cls(
            DartProperty("name", "String"),
            DartProperty("age", "int")
        ))
        assertTrue(result.contains("String? name"))
        assertTrue(result.contains("int? age"))
    }

    @Test
    fun `uses null-coalescing for assignments`() {
        val result = generator.generate(cls(DartProperty("name", "String")))
        assertTrue(result.contains("name: name ?? this.name"))
    }

    @Test
    fun `returns correct constructor name`() {
        val dartClass = DartClass(
            name = "Product",
            properties = listOf(DartProperty("id", "String")),
            startOffset = 0,
            endOffset = 0,
            constructorName = "Product"
        )
        val result = generator.generate(dartClass)
        assertTrue(result.contains("Product copyWith({"))
        assertTrue(result.contains("return Product("))
    }

    @Test
    fun `generates all properties`() {
        val result = generator.generate(cls(
            DartProperty("a", "String"),
            DartProperty("b", "int"),
            DartProperty("c", "bool")
        ))
        assertTrue(result.contains("String? a"))
        assertTrue(result.contains("int? b"))
        assertTrue(result.contains("bool? c"))
        assertTrue(result.contains("a: a ?? this.a"))
        assertTrue(result.contains("b: b ?? this.b"))
        assertTrue(result.contains("c: c ?? this.c"))
    }

    @Test
    fun `starts with newline`() {
        val result = generator.generate(cls(DartProperty("x", "String")))
        assertTrue(result.startsWith("\n"))
    }
}
