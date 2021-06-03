import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import me.nickp0is0n.easylocalize.ui.MainWindowView

fun main() = Window {
    val currentView = MainWindowView()
    MaterialTheme {
        currentView.MainUI()
    }
}
