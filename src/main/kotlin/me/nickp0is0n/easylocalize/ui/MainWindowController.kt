package me.nickp0is0n.easylocalize.ui

import androidx.compose.desktop.AppWindow
import me.nickp0is0n.easylocalize.models.LocalizedString
import me.nickp0is0n.easylocalize.utils.LocalizeExporter
import java.awt.FileDialog

class MainWindowController {
    var exportedSuccessfully = false
    fun onExportButtonClick(content: List<LocalizedString>, window: AppWindow) {
        val exporter = LocalizeExporter()

        val openDialog = FileDialog(window.window)
        openDialog.mode = FileDialog.SAVE
        openDialog.isVisible = true

        val exportFile = try {
            openDialog.files[0]
        }
        catch (e: ArrayIndexOutOfBoundsException) {
            null
        }

        if (exportFile != null) {
            if (!exportFile.exists()) {
                exportFile.createNewFile()
            }
            exporter.toFile(content, exportFile)
            exportedSuccessfully = true
        }
    }
}