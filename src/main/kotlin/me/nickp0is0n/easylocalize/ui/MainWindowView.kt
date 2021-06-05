package me.nickp0is0n.easylocalize.ui

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import me.nickp0is0n.easylocalize.models.LocalizedString
import me.nickp0is0n.easylocalize.utils.LocalizeParser
import java.awt.FileDialog

class MainWindowView {
    private lateinit var stringList: SnapshotStateList<LocalizedString>
    private lateinit var fieldValuesModel: FieldValuesViewModel
    private var selectedID = -1
    private val controller = MainWindowController()

    @Composable
    fun MainUI() {
        val window = LocalAppWindow.current
        Row {
            fieldValuesModel = FieldValuesViewModel(
                stringFieldValue = remember { mutableStateOf("Select an ID") },
                commentFieldValue = remember { mutableStateOf("Select an ID") }
            )
            val originalList = retrieveStringList()
            stringList = remember { mutableStateListOf(*originalList.toTypedArray()) }
            StringList(stringList)
            Column {
                StringTextField()
                CommentTextField()
                Button (
                    onClick = {
                        controller.onExportButtonClick(stringList, window)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(30, 144, 255)),
                    modifier = Modifier.padding(top = 10.dp)
                        ) {
                    Text(text ="Export translations to file...", color = Color.White)
                }
            }
        }
    }

    @Composable
    private fun StringList(strings: List<LocalizedString>) {
        Column {
            Text(
                text = "String ID's",
                modifier = Modifier.padding(top = 10.dp, start = 10.dp)
            )
            LazyColumn (
                modifier = Modifier.padding(top = 10.dp)
            ) {
                items(strings) {
                    StringItem(it)
                }
            }
        }
    }

    @Composable
    private fun StringItem(item: LocalizedString) {
        Button(
            modifier = Modifier
                .padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 0.dp)
                .size(width = 300.dp, height = 50.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
            shape = RectangleShape,
            onClick = {
                fieldValuesModel.stringFieldValue.value = item.text
                fieldValuesModel.commentFieldValue.value = item.comment
                selectedID = stringList.indexOf(item)
            }
        ) {
            Text(item.id)
        }
    }

    @Composable
    private fun StringTextField() {
        Column {
            Text(
                text = "String",
                modifier = Modifier.padding(top = 10.dp)
            )
            TextField (value = fieldValuesModel.stringFieldValue.value,
                onValueChange = { run {
                    fieldValuesModel.stringFieldValue.value = it
                    if (selectedID != -1) {
                        val currentString = this@MainWindowView.stringList[selectedID]
                        this@MainWindowView.stringList[selectedID] = LocalizedString(
                            currentString.id,
                            it,
                            currentString.comment
                        )
                    }
                } },
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(width = 450.dp, height = 150.dp))
        }
    }

    @Composable
    private fun CommentTextField() {
        Column {
            Text(
                text = "Comment",
                modifier = Modifier.padding(top = 10.dp)
            )
            TextField (value = fieldValuesModel.commentFieldValue.value,
                onValueChange = { run {
                    fieldValuesModel.commentFieldValue.value = it
                    if (selectedID != -1) {
                        val currentString = this@MainWindowView.stringList[selectedID]
                        this@MainWindowView.stringList[selectedID] = LocalizedString(
                            currentString.id,
                            currentString.text,
                            it
                        )
                    }
                } },
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(width = 450.dp, height = 150.dp))
        }
    }

    @Composable
    private fun retrieveStringList(): List<LocalizedString> {
        val parser = LocalizeParser()
        val window = LocalAppWindow.current
        val openDialog = FileDialog(window.window)
        openDialog.isVisible = true
        val stringFile = openDialog.files[0]
        return parser.fromFile(stringFile)
    }

    @Composable
    private fun showExportSuccessAlert() {
        AlertDialog(
            title = {
                Text("Success")
            },
            text = {
                Text("Translation file successfully exported!")
            },
            onDismissRequest = {},
            confirmButton = {
                Button(onClick = {}) {
                    Text("OK")
                }
            }
        )
    }
}