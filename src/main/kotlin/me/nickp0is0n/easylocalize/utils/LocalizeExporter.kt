package me.nickp0is0n.easylocalize.utils

import me.nickp0is0n.easylocalize.models.LocalizedString
import java.io.File
import java.io.PrintWriter

class LocalizeExporter {
    fun toFile(localizedStrings: List<LocalizedString>, outputFile: File) {
        val writer = PrintWriter(outputFile)
        var lastMark: String? = null
        var isFirstString = true
        var isHeader = false

        writer.use {
            localizedStrings.forEach {
                if (isFirstString && it.isCommentMultilined) {
                    writer.println("/*${it.comment}*/") // writing xcode copyright header on top
                    isHeader = true
                }
                if (it.mark != lastMark && it.mark != null) {
                    lastMark = it.mark
                    writer.println("\n// MARK:${it.mark}\n")
                }
                if (isFirstString && isHeader) {
                    writer.println(it.toStringWithoutComment())
                    isFirstString = false
                }
                else {
                    writer.println(it)
                }
            }
        }
    }
}