package com.kxaca.dartdatatoolkit.generators

import com.kxaca.dartdatatoolkit.parser.DartClass
import com.kxaca.dartdatatoolkit.parser.DartProperty
import org.junit.Assert.*
import org.junit.Test

class GeneratorsKitTest {

    private fun cls(vararg props: DartProperty, name: String = "User") =
        DartClass(name = name, properties = props.toList(), startOffset = 0, endOffset = 0, constructorName = name)

    // ── ToJsonGenerator ───────────────────────────────────────────────────────

    @Test
    fun `toJson returns empty on no properties`() {
        assertEquals("", ToJsonGenerator().generate(cls()))
    }

    @Test
    fun `toJson contains method signature`() {
        val result = ToJsonGenerator().generate(cls(DartProperty("name", "String")))
        assertTrue(result.contains("Map<String, dynamic> toJson()"))
    }

    @Test
    fun `toJson maps all properties`() {
        val result = ToJsonGenerator().generate(cls(
            DartProperty("name", "String"),
            DartProperty("age", "int")
        ))
        assertTrue(result.contains("'name': name"))
        assertTrue(result.contains("'age': age"))
    }

    @Test
    fun `toJson starts with newline`() {
        val result = ToJsonGenerator().generate(cls(DartProperty("x", "String")))
        assertTrue(result.startsWith("\n"))
    }

    // ── FromJsonGenerator ─────────────────────────────────────────────────────

    @Test
    fun `fromJson returns empty on no properties`() {
        assertEquals("", FromJsonGenerator().generate(cls()))
    }

    @Test
    fun `fromJson contains factory constructor`() {
        val result = FromJsonGenerator().generate(cls(DartProperty("name", "String")))
        assertTrue(result.contains("factory User.fromJson(Map<String, dynamic> json)"))
    }

    @Test
    fun `fromJson uses json bracket notation`() {
        val result = FromJsonGenerator().generate(cls(
            DartProperty("name", "String"),
            DartProperty("age", "int")
        ))
        assertTrue(result.contains("name: json['name']"))
        assertTrue(result.contains("age: json['age']"))
    }

    @Test
    fun `fromJson uses constructorName`() {
        val dartClass = cls(DartProperty("id", "String"), name = "Product")
        val result = FromJsonGenerator().generate(dartClass)
        assertTrue(result.contains("factory Product.fromJson"))
        assertTrue(result.contains("return Product("))
    }

    @Test
    fun `fromJson starts with newline`() {
        val result = FromJsonGenerator().generate(cls(DartProperty("x", "String")))
        assertTrue(result.startsWith("\n"))
    }

    // ── ToStringGenerator ─────────────────────────────────────────────────────

    @Test
    fun `toString returns empty on no properties`() {
        assertEquals("", ToStringGenerator().generate(cls()))
    }

    @Test
    fun `toString has override annotation`() {
        val result = ToStringGenerator().generate(cls(DartProperty("name", "String")))
        assertTrue(result.contains("@override"))
        assertTrue(result.contains("String toString()"))
    }

    @Test
    fun `toString includes all field interpolations`() {
        val result = ToStringGenerator().generate(cls(
            DartProperty("name", "String"),
            DartProperty("age", "int")
        ))
        assertTrue(result.contains("name: \$name"))
        assertTrue(result.contains("age: \$age"))
    }

    @Test
    fun `toString uses constructorName in output`() {
        val dartClass = cls(DartProperty("x", "String"), name = "Order")
        val result = ToStringGenerator().generate(dartClass)
        assertTrue(result.contains("Order("))
    }

    @Test
    fun `toString starts with newline`() {
        val result = ToStringGenerator().generate(cls(DartProperty("x", "String")))
        assertTrue(result.startsWith("\n"))
    }

    @Test
    fun `toString calls toString() on custom class properties`() {
        val result = ToStringGenerator().generate(cls(
            DartProperty("name", "String"),
            DartProperty("address", "Address", isCustomClass = true)
        ))
        assertTrue(result.contains("name: \$name"))
        assertTrue(result.contains("address: \${address.toString()}"))
    }

    @Test
    fun `toString uses safe call on nullable custom class properties`() {
        val result = ToStringGenerator().generate(cls(
            DartProperty("billing", "Address", isNullable = true, isCustomClass = true)
        ))
        assertTrue(result.contains("billing: \${billing?.toString()}"))
    }
}
