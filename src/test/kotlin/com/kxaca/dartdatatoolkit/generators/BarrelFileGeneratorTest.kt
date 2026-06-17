package com.kxaca.dartdatatoolkit.generators

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class BarrelFileGeneratorTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val generator = BarrelFileGenerator()
    private val analyzer  = BarrelFileAnalyzer()

    // ── BarrelFileGenerator ───────────────────────────────────────────────────

    @Test
    fun `generates header when file list is empty`() {
        val result = generator.generateBarrel(emptyList(), tmp.root)
        assertTrue(result.contains("barrel file"))
    }

    @Test
    fun `generates export for each file`() {
        val base = tmp.root
        val a = File(base, "alpha.dart").also { it.createNewFile() }
        val b = File(base, "beta.dart").also { it.createNewFile() }

        val result = generator.generateBarrel(listOf(a, b), base)
        assertTrue(result.contains("export 'alpha.dart';"))
        assertTrue(result.contains("export 'beta.dart';"))
    }

    @Test
    fun `exports are sorted alphabetically`() {
        val base = tmp.root
        val z = File(base, "zebra.dart").also { it.createNewFile() }
        val a = File(base, "alpha.dart").also { it.createNewFile() }

        val result = generator.generateBarrel(listOf(z, a), base)
        val alphaIdx = result.indexOf("alpha.dart")
        val zebraIdx = result.indexOf("zebra.dart")
        assertTrue(alphaIdx < zebraIdx)
    }

    @Test
    fun `uses relative paths`() {
        val base = tmp.root
        val sub = File(base, "models").also { it.mkdir() }
        val f   = File(sub, "user.dart").also { it.createNewFile() }

        val result = generator.generateBarrel(listOf(f), base)
        assertTrue(result.contains("export 'models/user.dart';"))
        assertFalse(result.contains(base.absolutePath))
    }

    @Test
    fun `adds file comments when requested`() {
        val base = tmp.root
        val f = File(base, "user.dart").also { it.createNewFile() }

        val result = generator.generateBarrel(listOf(f), base, withFileComments = true)
        assertTrue(result.contains("// user.dart"))
    }

    @Test
    fun `no file comments by default`() {
        val base = tmp.root
        val f = File(base, "user.dart").also { it.createNewFile() }

        val result = generator.generateBarrel(listOf(f), base)
        assertFalse(result.contains("// user.dart"))
    }

    // ── BarrelFileAnalyzer ────────────────────────────────────────────────────

    @Test
    fun `finds dart files in directory`() {
        val base = tmp.root
        File(base, "a.dart").createNewFile()
        File(base, "b.dart").createNewFile()
        File(base, "c.kt").createNewFile()

        val files = analyzer.findDartFiles(base, recursive = false)
        assertEquals(2, files.size)
        assertTrue(files.all { it.name.endsWith(".dart") })
    }

    @Test
    fun `excludes index files by default`() {
        val base = tmp.root
        File(base, "index.dart").createNewFile()
        File(base, "user.dart").createNewFile()

        val files = analyzer.findDartFiles(base, recursive = false)
        assertEquals(1, files.size)
        assertEquals("user.dart", files[0].name)
    }

    @Test
    fun `finds files recursively`() {
        val base = tmp.root
        val sub  = File(base, "models").also { it.mkdir() }
        File(base, "root.dart").createNewFile()
        File(sub,  "user.dart").createNewFile()

        val files = analyzer.findDartFiles(base, recursive = true)
        assertEquals(2, files.size)
    }

    @Test
    fun `does not recurse when recursive is false`() {
        val base = tmp.root
        val sub  = File(base, "models").also { it.mkdir() }
        File(base, "root.dart").createNewFile()
        File(sub,  "user.dart").createNewFile()

        val files = analyzer.findDartFiles(base, recursive = false)
        assertEquals(1, files.size)
        assertEquals("root.dart", files[0].name)
    }

    @Test
    fun `skips special directories`() {
        val base  = tmp.root
        val build = File(base, "build").also { it.mkdir() }
        File(base,  "user.dart").createNewFile()
        File(build, "gen.dart").createNewFile()

        val files = analyzer.findDartFiles(base, recursive = true)
        assertEquals(1, files.size)
        assertEquals("user.dart", files[0].name)
    }

    @Test
    fun `returns empty list for non-existent directory`() {
        val fake = File(tmp.root, "does_not_exist")
        val files = analyzer.findDartFiles(fake)
        assertTrue(files.isEmpty())
    }

    @Test
    fun `results are sorted by filename`() {
        val base = tmp.root
        File(base, "zebra.dart").createNewFile()
        File(base, "alpha.dart").createNewFile()
        File(base, "mango.dart").createNewFile()

        val files = analyzer.findDartFiles(base, recursive = false)
        assertEquals(listOf("alpha.dart", "mango.dart", "zebra.dart"), files.map { it.name })
    }
}
