import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.IntSize
import me.nickp0is0n.easylocalize.ui.MainWindowView

fun main() = Window (
    title = "EasyLocalize 0.0.1 alpha",
    resizable = false,
    size = IntSize(780, 450)
) {
    val currentView = MainWindowView()
    MaterialTheme {
        currentView.MainUI()
    }
}
