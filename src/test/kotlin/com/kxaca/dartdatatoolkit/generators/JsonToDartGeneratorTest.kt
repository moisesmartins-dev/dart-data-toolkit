package com.kxaca.dartdatatoolkit.generators

import org.junit.Assert.*
import org.junit.Test

class JsonToDartGeneratorTest {

    private val generator = JsonToDartGenerator()

    private fun gen(json: String, methods: Set<String> = emptySet(), className: String = "MyClass") =
        generator.generateClass(className, json, methods)

    // ── Basic class structure ─────────────────────────────────────────────────

    @Test
    fun `generates class declaration`() {
        val result = gen("""{"name": "John"}""")
        assertTrue(result.contains("class MyClass {"))
        assertTrue(result.endsWith("}"))
    }

    @Test
    fun `generates final field declarations`() {
        val result = gen("""{"name": "John", "age": 30}""")
        assertTrue(result.contains("final String name;"))
        assertTrue(result.contains("final int age;"))
    }

    @Test
    fun `generates named constructor`() {
        val result = gen("""{"name": "John"}""")
        assertTrue(result.contains("MyClass({"))
        assertTrue(result.contains("required this.name"))
    }

    // ── Type inference ────────────────────────────────────────────────────────

    @Test
    fun `infers String type`() {
        val result = gen("""{"title": "hello"}""")
        assertTrue(result.contains("final String title;"))
    }

    @Test
    fun `infers int type`() {
        val result = gen("""{"count": 5}""")
        assertTrue(result.contains("final int count;"))
    }

    @Test
    fun `infers double type`() {
        val result = gen("""{"price": 9.99}""")
        assertTrue(result.contains("final double price;"))
    }

    @Test
    fun `infers bool type`() {
        val result = gen("""{"active": true}""")
        assertTrue(result.contains("final bool active;"))
    }

    @Test
    fun `infers List type`() {
        val result = gen("""{"tags": ["a", "b"]}""")
        assertTrue(result.contains("final List<String> tags;"))
    }

    @Test
    fun `infers empty list as List dynamic`() {
        val result = gen("""{"items": []}""")
        assertTrue(result.contains("final List<dynamic> items;"))
    }

    @Test
    fun `nested object generates separate class`() {
        val result = gen("""{"meta": {"key": "value"}}""")
        assertTrue(result.contains("class Meta {"))
        assertTrue(result.contains("final String key;"))
        assertTrue(result.contains("class MyClass {"))
        assertTrue(result.contains("final Meta meta;"))
    }

    @Test
    fun `nested class gets selected methods`() {
        val result = gen("""{"location": {"lat": -25.4, "lon": -49.25}}""", setOf("toJson", "fromJson"))
        val locationBlock = result.substringBefore("class MyClass")
        assertTrue(locationBlock.contains("toJson"))
        assertTrue(locationBlock.contains("fromJson"))
        assertTrue(result.contains("location.toJson()"))
        assertTrue(result.contains("Location.fromJson("))
    }

    @Test
    fun `deeply nested objects generate all classes`() {
        val result = gen("""{"a": {"b": {"c": 1}}}""")
        assertTrue(result.contains("class B {"))
        assertTrue(result.contains("class A {"))
        assertTrue(result.contains("class MyClass {"))
        assertTrue(result.contains("final int c;"))
        assertTrue(result.contains("final B b;"))
        assertTrue(result.contains("final A a;"))
    }

    @Test
    fun `infers null as dynamic`() {
        val result = gen("""{"value": null}""")
        assertTrue(result.contains("final dynamic value;"))
    }

    // ── Key conversion ────────────────────────────────────────────────────────

    @Test
    fun `converts snake_case key to camelCase`() {
        val result = gen("""{"first_name": "John"}""")
        assertTrue(result.contains("firstName"))
    }

    @Test
    fun `converts kebab-case key to camelCase`() {
        val result = gen("""{"created-at": "2024-01-01"}""")
        assertTrue(result.contains("createdAt"))
    }

    @Test
    fun `leaves single-word key unchanged`() {
        val result = gen("""{"email": "a@b.com"}""")
        assertTrue(result.contains("email"))
    }

    // ── Method inclusion ──────────────────────────────────────────────────────

    @Test
    fun `no methods when set is empty`() {
        val result = gen("""{"x": 1}""", emptySet())
        assertFalse(result.contains("copyWith"))
        assertFalse(result.contains("toJson"))
        assertFalse(result.contains("fromJson"))
        assertFalse(result.contains("toString"))
    }

    @Test
    fun `includes only selected methods`() {
        val result = gen("""{"x": 1}""", setOf("toJson", "fromJson"))
        assertFalse(result.contains("copyWith"))
        assertTrue(result.contains("toJson"))
        assertTrue(result.contains("fromJson"))
        assertFalse(result.contains("toString()"))
    }

    @Test
    fun `includes all methods when all selected`() {
        val result = gen("""{"x": 1}""", setOf("copyWith", "toJson", "fromJson", "toString"))
        assertTrue(result.contains("copyWith"))
        assertTrue(result.contains("toJson"))
        assertTrue(result.contains("fromJson"))
        assertTrue(result.contains("toString"))
    }

    // ── Error handling ────────────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun `throws when root is array`() {
        gen("""[1, 2, 3]""")
    }

    @Test(expected = Exception::class)
    fun `throws on invalid json`() {
        gen("""not json""")
    }

    // ── Custom class name ─────────────────────────────────────────────────────

    @Test
    fun `uses provided class name`() {
        val result = gen("""{"id": 1}""", className = "Order")
        assertTrue(result.contains("class Order {"))
        assertTrue(result.contains("Order({"))
    }
}
