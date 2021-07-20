import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.IntSize
import me.nickp0is0n.easylocalize.ui.MainWindowView
import me.nickp0is0n.easylocalize.utils.AppInfo

fun main() = Window (
    title = AppInfo.windowTitle,
    resizable = false,
    size = IntSize(780, 455)
) {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    val currentView = MainWindowView()
    MaterialTheme {
        currentView.MainUI()
    }
}
