package me.nickp0is0n.easylocalize.utils

import me.nickp0is0n.easylocalize.models.LocalizedString
import me.nickp0is0n.easylocalize.models.ParserModel
import me.nickp0is0n.easylocalize.models.ParserSettings
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern

class LocalizeParser(val settings: ParserSettings) {
    private val parserModel = ParserModel(settings = settings)
    private val parsedStrings = mutableListOf<LocalizedString>()

    fun fromFile(file: File): List<LocalizedString> {
        val reader = BufferedReader(FileReader(file))
        var currentLine: String?
        var isCommentMultilined = false
        var isHeaderAlreadyExist = false

        while (reader.readLine().also { currentLine = it } != null) {
            when {
                isAComment(currentLine!!) -> {
                    if (!isCommentMultilined && parserModel.currentComment.isNotEmpty()) {
                        setCurrentComment(parserModel.currentComment + "\n" + parseComment(currentLine!!))
                    } else {
                        setCurrentComment(parserModel.currentComment + parseComment(currentLine!!))
                    }

                    if (parsedStrings.isEmpty() && !parserModel.multilineCommentMode && !isCommentMultilined) {
                        isHeaderAlreadyExist = true
                    }
                    if (parserModel.multilineCommentMode) {
                        isCommentMultilined = true
                    } else if (parserModel.currentComment.isNotEmpty() && parsedStrings.isEmpty() && !isHeaderAlreadyExist) {
                        parserModel.header = parserModel.currentComment
                        setCurrentComment("")
                        isHeaderAlreadyExist = true
                        isCommentMultilined = false
                    }
                }

                isLineBelongToUnfinishedString(currentLine!!, parserModel.currentString) -> {
                    if (settings.ignoreCopyrightHeader) {
                        parserModel.header = null
                    }
                    parseEndOfMultilineString(currentLine!!, isCommentMultilined)
                }

                currentLine != "" || parserModel.multilineCommentMode -> {
                    val wrappedStringPattern = Pattern.compile("([\"])(?:(?=(\\\\?))\\2.)*?\\1")
                    val patternMatcher = wrappedStringPattern.matcher(currentLine!!)
                    retrieveId(currentLine!!, patternMatcher)
                    retrieveTextString(currentLine!!, patternMatcher)
                    if (settings.ignoreCopyrightHeader) {
                        parserModel.header = null
                    }
                    finalizeLocalizedString(currentLine!!, isCommentMultilined)
                    isCommentMultilined = false
                }
            }
        }
        return parsedStrings
    }

    private fun finalizeLocalizedString(currentLine: String, isCommentMultilined: Boolean) {
        if (currentLine.endsWith("\";")) {
            parsedStrings.add(
                LocalizedString(
                    parserModel.currentId ?: "null",
                    parserModel.currentString ?: "null",
                    parserModel.currentComment,
                    isCommentMultilined,
                    parserModel.currentMark,
                    parserModel.header
                )
            )
            setCurrentComment("")
            parserModel.header = null
        }
    }

    private fun isLineBelongToUnfinishedString(currentLine: String, currentString: String?): Boolean {
        return !currentLine.startsWith("\"") && currentString != null
    }

    private fun isAComment(currentLine: String): Boolean {
        return currentLine.startsWith("//") || currentLine.startsWith("/*") || parserModel.multilineCommentMode
    }

    private fun retrieveId(currentLine: String, patternMatcher: Matcher) {
        if (!patternMatcher.find()) {
            throw IOException("ID is not found")
        }
        parserModel.currentId = currentLine.substring(patternMatcher.start() + 1, patternMatcher.end() - 1)
    }

    private fun retrieveTextString(currentLine: String, patternMatcher: Matcher) {
        if (!patternMatcher.find()) {
            val lastIndex = nthIndexOf(currentLine, '\"', 3)
            parserModel.currentString = currentLine.substring(lastIndex + 1)
        } else {
            parserModel.currentString = currentLine.substring(patternMatcher.start() + 1, patternMatcher.end() - 1)
        }
    }

    private fun parseEndOfMultilineString(currentLine: String, isCommentMultilined: Boolean) {
        var localCurrentLine = currentLine
        if (localCurrentLine.endsWith("\";")) {
            localCurrentLine = localCurrentLine.substring(0, localCurrentLine.length - 2)
            parserModel.currentString = parserModel.currentString + "\n" + localCurrentLine
            parsedStrings.add(
                LocalizedString(
                    parserModel.currentId ?: "null",
                    parserModel.currentString ?: "null",
                    parserModel.currentComment,
                    isCommentMultilined,
                    parserModel.currentMark,
                    parserModel.header
                )
            )
            setCurrentComment("")
            parserModel.header = null
        } else {
            parserModel.currentString = parserModel.currentString + "\n" + localCurrentLine
        }
    }

    private fun parseComment(currentLine: String): String {
        var localCurrentLine = currentLine
        var currentComment = ""

        when {
            localCurrentLine.startsWith("//") -> {
                if (localCurrentLine.contains("MARK:")) {
                    parserModel.currentMark = localCurrentLine.substring(8)
                    return currentComment
                }
                currentComment = "$currentComment${localCurrentLine.substring(2)}\n".trim { it <= ' ' }
                return currentComment
            }
            localCurrentLine.startsWith("/*") -> {
                parserModel.multilineCommentMode = true
            }
        }
        if (parserModel.multilineCommentMode) {
            localCurrentLine = localCurrentLine.replace("/*", "")
            currentComment = "$currentComment$localCurrentLine\n"
            if (localCurrentLine.endsWith("*/")) {
                currentComment = currentComment.substring(0, currentComment.length - 3)
                parserModel.multilineCommentMode = false
            }
        }
        return currentComment
    }

    private fun nthIndexOf(text: String, needle: Char, n: Int): Int {
        var nIndex = n
        for (i in text.indices) {
            if (text[i] == needle) {
                nIndex--
                if (nIndex == 0) {
                    return i
                }
            }
        }
        return -1
    }

    private fun setCurrentComment(comment: String) {
        if (settings.ignoreComments) {
            parserModel.currentComment = ""
        } else {
            parserModel.currentComment = comment
        }
    }
}