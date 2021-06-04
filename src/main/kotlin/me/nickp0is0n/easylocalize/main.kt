import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import me.nickp0is0n.easylocalize.ui.MainWindowView

fun main() = Window (
    title = "EasyLocalize 0.0.1 alpha",
    resizable = false) {
    val currentView = MainWindowView()
    MaterialTheme {
        currentView.MainUI()
    }
}
