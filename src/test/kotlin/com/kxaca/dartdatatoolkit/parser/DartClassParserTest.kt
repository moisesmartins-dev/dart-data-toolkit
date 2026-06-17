package com.kxaca.dartdatatoolkit.parser

import org.junit.Assert.*
import org.junit.Test

class DartClassParserTest {

    private val parser = DartClassParser()

    // ── parseClass ────────────────────────────────────────────────────────────

    @Test
    fun `returns null when content has no class`() {
        assertNull(parser.parseClass("void main() {}", 10))
    }

    @Test
    fun `returns null when cursor is before class declaration`() {
        val code = "// comment\nclass User { final String name; }"
        assertNull(parser.parseClass(code, 3))
    }

    @Test
    fun `parses simple class with String property`() {
        val code = """
            class User {
              final String name;
            }
        """.trimIndent()
        val result = parser.parseClass(code, 20)
        assertNotNull(result)
        assertEquals("User", result!!.name)
        assertEquals(1, result.properties.size)
        assertEquals("name", result.properties[0].name)
        assertEquals("String", result.properties[0].type)
        assertFalse(result.properties[0].isNullable)
    }

    @Test
    fun `parses nullable property`() {
        val code = """
            class User {
              final int? age;
            }
        """.trimIndent()
        val result = parser.parseClass(code, 20)!!
        val prop = result.properties[0]
        assertEquals("age", prop.name)
        assertEquals("int", prop.type)
        assertTrue(prop.isNullable)
    }

    @Test
    fun `parses lowercase primitive types`() {
        val code = """
            class Item {
              final int count;
              final double price;
              final bool active;
            }
        """.trimIndent()
        val result = parser.parseClass(code, 20)!!
        val names = result.properties.map { it.name }
        val types = result.properties.map { it.type }
        assertTrue(names.containsAll(listOf("count", "price", "active")))
        assertTrue(types.containsAll(listOf("int", "double", "bool")))
    }

    @Test
    fun `parses generic types`() {
        val code = """
            class Cart {
              final List<String> items;
              final Map<String, dynamic> meta;
            }
        """.trimIndent()
        val result = parser.parseClass(code, 20)!!
        val types = result.properties.map { it.type }
        assertTrue(types.contains("List<String>"))
        assertTrue(types.contains("Map<String, dynamic>"))
    }

    @Test
    fun `ignores constructor and method declarations`() {
        val code = """
            class User {
              final String name;
              User({required this.name});
              String greet() => 'hi';
            }
        """.trimIndent()
        val result = parser.parseClass(code, 20)!!
        assertEquals(1, result.properties.size)
        assertEquals("name", result.properties[0].name)
    }

    @Test
    fun `ignores dart keywords as property names`() {
        val code = """
            class Foo {
              final String return;
              final int count;
            }
        """.trimIndent()
        val result = parser.parseClass(code, 20)!!
        assertEquals(1, result.properties.size)
        assertEquals("count", result.properties[0].name)
    }

    @Test
    fun `picks closest class before cursor`() {
        val code = """
            class A {
              final String x;
            }
            class B {
              final int y;
            }
        """.trimIndent()
        val cursorInB = code.indexOf("final int")
        val result = parser.parseClass(code, cursorInB + 5)!!
        assertEquals("B", result.name)
    }

    @Test
    fun `parses abstract class`() {
        val code = """
            abstract class Shape {
              final double area;
            }
        """.trimIndent()
        val result = parser.parseClass(code, 30)!!
        assertEquals("Shape", result.name)
        assertEquals(1, result.properties.size)
    }

    @Test
    fun `endOffset points to closing brace`() {
        val code = "class Foo { final String x; }"
        val result = parser.parseClass(code, 15)!!
        assertEquals('}', code[result.endOffset])
    }

    @Test
    fun `returns empty properties list when class has none`() {
        val code = "class Empty { }"
        val result = parser.parseClass(code, 10)!!
        assertTrue(result.properties.isEmpty())
    }

    @Test
    fun `parses multiple mixed-nullability properties`() {
        val code = """
            class Product {
              final String id;
              final String? description;
              final double price;
              final int? stock;
            }
        """.trimIndent()
        val result = parser.parseClass(code, 30)!!
        assertEquals(4, result.properties.size)

        val byName = result.properties.associateBy { it.name }
        assertFalse(byName["id"]!!.isNullable)
        assertTrue(byName["description"]!!.isNullable)
        assertFalse(byName["price"]!!.isNullable)
        assertTrue(byName["stock"]!!.isNullable)
    }
}
