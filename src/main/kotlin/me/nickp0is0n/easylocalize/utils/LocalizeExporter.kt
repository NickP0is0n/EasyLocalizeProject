package me.nickp0is0n.easylocalize.utils

import me.nickp0is0n.easylocalize.models.LocalizedString
import java.io.File
import java.io.PrintWriter

class LocalizeExporter {
    fun toFile(localizedStrings: List<LocalizedString>, outputFile: File) {
        val writer = PrintWriter(outputFile)
        writer.use {
            localizedStrings.forEach {
                writer.println("$it\n")
            }
        }
    }
}