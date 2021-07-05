import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.IntSize
import me.nickp0is0n.easylocalize.ui.MainWindowView

fun main() = Window (
    title = "EasyLocalize 0.0.13 Alpha",
    resizable = false,
    size = IntSize(780, 455)
) {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    val currentView = MainWindowView()
    MaterialTheme {
        currentView.MainUI()
    }
}
