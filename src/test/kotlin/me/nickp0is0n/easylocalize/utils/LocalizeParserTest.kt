package me.nickp0is0n.easylocalize.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import java.io.File

class LocalizeParserTest {
    val parser = LocalizeParser()

    @DisplayName("Reading basic string without comment")
    @Test
    fun readBasicString() {
        val result = parser.fromFile(File(this.javaClass.classLoader.getResource("parser/basic").toURI()))
        assertEquals(result[0].id, "CONTINUE-ERASE")
        assertEquals(result[0].text, "Continue and Erase")
    }

    @DisplayName("Reading string with comment")
    @Test
    fun readBasicStringWithComment() {
        val result = parser.fromFile(File(this.javaClass.classLoader.getResource("parser/withComment").toURI()))
        assertEquals("CONTINUE-ERASE", result[0].id)
        assertEquals("Continue and Erase", result[0].text)
        assertEquals("Sample comment\n", result[0].comment)
    }

    @DisplayName("Reading string with multiple single comments")
    @Test
    fun readBasicStringWithMultipleSingleComments() {
        val result = parser.fromFile(File(this.javaClass.classLoader.getResource("parser/withMultipleSingleComments").toURI()))
        assertEquals("CONTINUE-ERASE", result[0].id)
        assertEquals("Continue and Erase", result[0].text)
        assertEquals("Sample\ncomment\n", result[0].comment)
    }

    @DisplayName("Reading string with single-line multiline-style comment")
    @Test
    fun readBasicStringWithSingleMultilineComment() {
        val result = parser.fromFile(File(this.javaClass.classLoader.getResource("parser/withMultilineCommentSingle").toURI()))
        assertEquals(result[0].id, "CONTINUE-ERASE")
        assertEquals(result[0].text, "Continue and Erase")
        assertEquals(result[0].comment, "Sample comment")
    }

    @DisplayName("Reading string with multiline comment")
    @Test
    fun readBasicStringWithMultilineComment() {
        val result = parser.fromFile(File(this.javaClass.classLoader.getResource("parser/withMultilineCommentMulti").toURI()))
        assertEquals(result[0].id, "CONTINUE-ERASE")
        assertEquals(result[0].text, "Continue and Erase")
        assertEquals(result[0].comment, "Sample\ncomment")
    }

    @DisplayName("Reading string with multiline comment that contains empty lines")
    @Test
    fun readBasicStringWithMultilineCommentWithEmptyLines() {
        val result = parser.fromFile(File(this.javaClass.classLoader.getResource("parser/withMultilineCommentMultiWithEmptyLines").toURI()))
        assertEquals(result[0].id, "CONTINUE-ERASE")
        assertEquals(result[0].text, "Continue and Erase")
        assertEquals(result[0].comment, "Sample\n\ncomment")
    }

    @DisplayName("Reading multi-lined string")
    @Test
    fun readMulilinedString() {
        val result = parser.fromFile(File(this.javaClass.classLoader.getResource("parser/multilinedString").toURI()))
        assertEquals(result[0].id, "CONTINUE-ERASE")
        assertEquals(result[0].text, "Continue\nand\nErase")
    }

    @DisplayName("Reading multi-lined string that contains empty lines")
    @Test
    fun readMulilinedStringWithEmptyLines() {
        val result = parser.fromFile(File(this.javaClass.classLoader.getResource("parser/multilinedStringWithEmptyLines").toURI()))
        assertEquals(result[0].id, "CONTINUE-ERASE")
        assertEquals(result[0].text, "Continue\n\nand\n\nErase")
    }
}