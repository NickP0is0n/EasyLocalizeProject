package me.nickp0is0n.easylocalize.ui

import androidx.compose.desktop.AppWindow
import androidx.compose.ui.window.Notifier
import me.nickp0is0n.easylocalize.models.LocalizedString
import me.nickp0is0n.easylocalize.utils.LocalizeExporter
import java.awt.FileDialog

class MainWindowController {
    fun onExportButtonClick(content: List<LocalizedString>, window: AppWindow) {
        val exporter = LocalizeExporter()

        val openDialog = FileDialog(window.window)
        openDialog.mode = FileDialog.SAVE
        openDialog.isVisible = true

        val exportFile = openDialog.files[0]
        if (!exportFile.exists()) {
            exportFile.createNewFile()
        }
        exporter.toFile(content, exportFile)
    }
}