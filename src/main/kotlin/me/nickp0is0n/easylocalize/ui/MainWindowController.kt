package me.nickp0is0n.easylocalize.ui

import androidx.compose.desktop.AppWindow
import androidx.compose.desktop.LocalAppWindow
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import me.nickp0is0n.easylocalize.models.LocalizedString
import me.nickp0is0n.easylocalize.utils.LocalizeExporter
import me.nickp0is0n.easylocalize.utils.LocalizeParser
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