package com.kxaca.dartdatatoolkit.inspections

import org.junit.Assert.*
import org.junit.Test

class CopyWithAnalysisTest {

    private fun applyFix(text: String, className: String): String {
        val r = CopyWithAnalysis.buildReplacement(text, className)!!
        return StringBuilder(text).replace(r.start, r.end, r.text).toString()
    }

    // ── findOutdatedCopyWith ──────────────────────────────────────────────────

    @Test
    fun `no finding when copyWith covers every field`() {
        val code = """
            class User {
              final String name;
              final int age;

              User copyWith({
                String? name,
                int? age,
              }) {
                return User(name: name ?? this.name, age: age ?? this.age);
              }
            }
        """.trimIndent()

        assertTrue(CopyWithAnalysis.findOutdatedCopyWith(code).isEmpty())
    }

    @Test
    fun `reports the single missing field`() {
        val code = """
            class User {
              final String name;
              final int age;

              User copyWith({String? name}) {
                return User(name: name ?? this.name, age: age);
              }
            }
        """.trimIndent()

        val findings = CopyWithAnalysis.findOutdatedCopyWith(code)
        assertEquals(1, findings.size)
        assertEquals("User", findings[0].dartClass.name)
        assertEquals(listOf("age"), findings[0].missing)
    }

    @Test
    fun `reports every missing field`() {
        val code = """
            class User {
              final String name;
              final String? email;
              final int age;

              User copyWith() {
                return User();
              }
            }
        """.trimIndent()

        val findings = CopyWithAnalysis.findOutdatedCopyWith(code)
        assertEquals(listOf("name", "email", "age"), findings.single().missing)
    }

    @Test
    fun `finding range points at the copyWith identifier`() {
        val code = """
            class User {
              final String name;
              final int age;

              User copyWith({String? name}) => User(name: name ?? this.name, age: age);
            }
        """.trimIndent()

        val finding = CopyWithAnalysis.findOutdatedCopyWith(code).single()
        assertEquals("copyWith", code.substring(finding.nameStart, finding.nameEnd))
    }

    @Test
    fun `class without copyWith is not reported`() {
        val code = """
            class User {
              final String name;
              final int age;
            }
        """.trimIndent()

        assertTrue(CopyWithAnalysis.findOutdatedCopyWith(code).isEmpty())
    }

    @Test
    fun `static and private fields are not required in copyWith`() {
        val code = """
            class User {
              static const int maxAge = 120;
              static final String kind = 'user';
              final String _secret;
              final String name;

              User copyWith({String? name}) {
                return User(name: name ?? this.name);
              }
            }
        """.trimIndent()

        assertTrue(CopyWithAnalysis.findOutdatedCopyWith(code).isEmpty())
    }

    @Test
    fun `copyWith call inside another method is not treated as the declaration`() {
        val code = """
            class User {
              final String name;
              final int age;

              User rename(String n) {
                return copyWith(name: n);
              }

              User copyWith({String? name, int? age}) {
                return User(name: name ?? this.name, age: age ?? this.age);
              }
            }
        """.trimIndent()

        assertTrue(CopyWithAnalysis.findOutdatedCopyWith(code).isEmpty())
    }

    @Test
    fun `reads required, this-dot, default and generic parameters`() {
        val code = """
            class Box {
              final Map<String, List<int>> data;
              final int count;
              final String label;
              final bool flag;

              Box copyWith({
                Map<String, List<int>>? data,
                required int count,
                this.label,
                bool flag = false,
              }) {
                return Box();
              }
            }
        """.trimIndent()

        assertTrue(CopyWithAnalysis.findOutdatedCopyWith(code).isEmpty())
    }

    @Test
    fun `reads mixed positional and named parameters`() {
        val code = """
            class User {
              final String name;
              final int age;
              final bool admin;

              User copyWith(String name, {int? age}) {
                return User();
              }
            }
        """.trimIndent()

        assertEquals(listOf("admin"), CopyWithAnalysis.findOutdatedCopyWith(code).single().missing)
    }

    @Test
    fun `function typed parameters resolve to their name`() {
        val code = """
            class Button {
              final String label;
              final void Function(int) onTap;

              Button copyWith({String? label, void Function(int)? onTap}) {
                return Button();
              }
            }
        """.trimIndent()

        assertTrue(CopyWithAnalysis.findOutdatedCopyWith(code).isEmpty())
    }

    @Test
    fun `reports each class of the file independently`() {
        val code = """
            class A {
              final int x;
              final int y;
              A copyWith({int? x}) { return A(); }
            }

            class B {
              final int z;
              B copyWith({int? z}) { return B(); }
            }

            class C {
              final int p;
              final int q;
              C copyWith({int? p}) { return C(); }
            }
        """.trimIndent()

        val findings = CopyWithAnalysis.findOutdatedCopyWith(code)
        assertEquals(listOf("A", "C"), findings.map { it.dartClass.name })
        assertEquals(listOf("y"), findings[0].missing)
        assertEquals(listOf("q"), findings[1].missing)
    }

    // ── buildReplacement (quick fix) ──────────────────────────────────────────

    @Test
    fun `quick fix regenerates copyWith and clears the finding`() {
        val code = """
            class User {
              final String name;
              final String? email;
              final int age;

              User copyWith({String? name}) {
                return User(name: name ?? this.name);
              }
            }
        """.trimIndent()

        val fixed = applyFix(code, "User")

        assertTrue(CopyWithAnalysis.findOutdatedCopyWith(fixed).isEmpty())
        assertTrue(fixed.contains("String? name"))
        assertTrue(fixed.contains("String? email"))
        assertTrue(fixed.contains("int? age"))
    }

    @Test
    fun `quick fix keeps the method in place and leaves other members untouched`() {
        val code = """
            class User {
              final String name;
              final int age;

              User copyWith({String? name}) {
                return User(name: name ?? this.name);
              }

              @override
              String toString() => 'User';
            }
        """.trimIndent()

        val fixed = applyFix(code, "User")

        assertTrue(fixed.indexOf("copyWith") < fixed.indexOf("toString"))
        assertTrue(fixed.contains("@override\n  String toString() => 'User';"))
        assertEquals(1, Regex("""\bcopyWith\s*\(""").findAll(fixed).count())
    }

    @Test
    fun `quick fix only touches the requested class`() {
        val code = """
            class A {
              final int x;
              final int y;
              A copyWith({int? x}) { return A(); }
            }

            class B {
              final int z;
              final int w;
              B copyWith({int? z}) { return B(); }
            }
        """.trimIndent()

        val fixed = applyFix(code, "B")
        val remaining = CopyWithAnalysis.findOutdatedCopyWith(fixed)

        assertEquals(listOf("A"), remaining.map { it.dartClass.name })
    }

    @Test
    fun `no replacement when the class has no copyWith`() {
        val code = """
            class User {
              final String name;
            }
        """.trimIndent()

        assertNull(CopyWithAnalysis.buildReplacement(code, "User"))
    }

    @Test
    fun `no replacement for an unknown class`() {
        val code = "class User { final String name; User copyWith() { return User(); } }"
        assertNull(CopyWithAnalysis.buildReplacement(code, "Other"))
    }
}
