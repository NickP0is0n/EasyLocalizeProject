package me.nickp0is0n.easylocalize.ui

import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import me.nickp0is0n.easylocalize.models.ParserSettings

class ParserSettingsView (val parserSettings: ParserSettings) {
    @Composable
    fun SettingsWindow() {
        Window (
            title = "Parser settings",
            size = IntSize(360, 140)
        ) {
            Box(modifier = Modifier
                .background(color = Color(255, 255, 255))
                .fillMaxSize()) // white color bg
            SettingsCheckBoxes()
        }
    }

    @Composable
    private fun SettingsCheckBoxes() {
        Column(modifier = Modifier.padding(10.dp)) {
            CheckboxWithText(
                checked = parserSettings.ignoreComments,
                onCheckedChange = { newValue ->
                    parserSettings.ignoreComments = newValue
                },
                text = "Ignore comments"
            )
            CheckboxWithText(
                checked = parserSettings.ignoreCopyrightHeader,
                onCheckedChange = { newValue ->
                    parserSettings.ignoreCopyrightHeader = newValue
                },
                text = "Ignore copyright header"
            )
        }
    }

    @Composable
    private fun CheckboxWithText(checked: Boolean, onCheckedChange: (Boolean) -> Unit, text: String) {
        val state = remember { mutableStateOf(checked) }
        Row(modifier = Modifier.padding(10.dp)) {
            Checkbox(
                checked = state.value,
                onCheckedChange = { newValue ->
                    onCheckedChange(newValue)
                    state.value = newValue
                }
            )
            Text(text, modifier = Modifier.padding(start = 10.dp, top = 5.dp))
        }
    }
}