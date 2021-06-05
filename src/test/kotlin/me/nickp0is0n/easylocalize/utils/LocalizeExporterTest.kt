package me.nickp0is0n.easylocalize.utils

import me.nickp0is0n.easylocalize.models.LocalizedString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileReader

class LocalizeExporterTest {
    val tempFile = File("tmptest")
    val exporter = LocalizeExporter()

    @BeforeEach
    fun setUp() {
        tempFile.createNewFile()
    }

    @AfterEach
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    @DisplayName("Writing basic string without comment")
    fun writeBasicString() {
        val expectedReader = FileReader(File(this.javaClass.classLoader.getResource("exporter/basic").toURI()))
        val actualReader = FileReader(tempFile)
        exporter.toFile(listOf(LocalizedString("CONTINUE-ERASE", "Continue and Erase", "")), tempFile)
        assertEquals(expectedReader.readText(), actualReader.readText())
    }

    @Test
    @DisplayName("Writing string with comment")
    fun writeBasicStringWithComment() {
        val expectedReader = FileReader(File(this.javaClass.classLoader.getResource("exporter/withComment").toURI()))
        val actualReader = FileReader(tempFile)
        exporter.toFile(listOf(LocalizedString("CONTINUE-ERASE", "Continue and Erase", "Sample comment")), tempFile)
        assertEquals(expectedReader.readText(), actualReader.readText())
    }

    @Test
    @DisplayName("Writing string with multi-lined comment")
    fun writeBasicStringWithMultilineComment() {
        val expectedReader = FileReader(File(this.javaClass.classLoader.getResource("exporter/withMultilineComment").toURI()))
        val actualReader = FileReader(tempFile)
        exporter.toFile(listOf(LocalizedString("CONTINUE-ERASE", "Continue and Erase", "Sample\ncomment")), tempFile)
        assertEquals(expectedReader.readText(), actualReader.readText())
    }

    @Test
    @DisplayName("Writing multi-lined string")
    fun writeMultilineString() {
        val expectedReader = FileReader(File(this.javaClass.classLoader.getResource("exporter/multilineString").toURI()))
        val actualReader = FileReader(tempFile)
        exporter.toFile(listOf(LocalizedString("CONTINUE-ERASE", "Continue\nand\nErase", "")), tempFile)
        assertEquals(expectedReader.readText(), actualReader.readText())
    }
}