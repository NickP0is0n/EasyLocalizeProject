package me.nickp0is0n.easylocalize.ui

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import me.nickp0is0n.easylocalize.models.LocalizedString
import me.nickp0is0n.easylocalize.utils.LocalizeParser
import java.awt.FileDialog

class MainWindowView {
    private lateinit var stringList: SnapshotStateList<LocalizedString>
    private lateinit var fieldValuesModel: FieldValuesViewModel
    private var selectedID = -1
    private val controller = MainWindowController()
    private val waitForFile = mutableStateOf(false)

    @Composable
    fun MainUI() {
        val window = LocalAppWindow.current
        window.setMenuBar(
            AppMenuBar()
        )

        Box(modifier = Modifier
            .background(color = Color(255, 255, 255))
            .fillMaxSize())
        Row {
            fieldValuesModel = FieldValuesViewModel(
                stringFieldValue = remember { mutableStateOf("Select an ID") },
                commentFieldValue = remember { mutableStateOf("Select an ID") }
            )

            val originalList = retrieveStringList()
            stringList = remember { mutableStateListOf(*originalList.toTypedArray()) }
            StringList(stringList)
            setTextFieldDefaultValues()
            Column {
                StringTextField()
                CommentTextField()
                Button (
                    onClick = {
                        controller.onExportButtonClick(stringList, window)
                        if (controller.exportedSuccessfully) {
                            val notifier = Notifier()
                            notifier.notify("Success", "Localization file has been successfully exported.")
                            controller.exportedSuccessfully = false // resets the value
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(30, 144, 255)),
                    modifier = Modifier.padding(top = 10.dp)
                        ) {
                    Text(text ="Export translations to file...", color = Color.White)
                }
            }
            checkIfOpenButtonClicked()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun StringList(strings: List<LocalizedString>) {
        Column {
            Text(
                text = "String ID's",
                modifier = Modifier.padding(top = 10.dp, start = 10.dp)
            )
            LazyColumn (
                modifier = Modifier
                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 8.dp)
                    .border(width = 2.dp, Color(245, 245, 245))
            ) {
                val groupedByMark = strings.groupBy {
                    it.mark
                }
                groupedByMark.forEach { (mark, strings) ->
                    if (mark != null) {
                        stickyHeader {
                            StringMarkHeader(mark)
                        }
                    }

                    items(strings) {
                        StringItem(it)
                    }
                }
            }
        }
    }

    @Composable
    private fun StringItem(item: LocalizedString) {
        Button(
            modifier = Modifier
                .size(width = 300.dp, height = 50.dp)
                .border(width = 1.dp, Color(245, 245, 245)),
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
            OutlinedTextField (value = fieldValuesModel.stringFieldValue.value,
                onValueChange = { run {
                    fieldValuesModel.stringFieldValue.value = it
                    if (selectedID != -1) {
                        val currentString = this@MainWindowView.stringList[selectedID]
                        this@MainWindowView.stringList[selectedID] = LocalizedString(
                            currentString.id,
                            it,
                            currentString.comment,
                            mark = currentString.mark,
                            copyrightHeader = currentString.copyrightHeader
                        )
                    }
                } },
                modifier = Modifier
                    .padding(top = 0.dp)
                    .size(width = 450.dp, height = 160.dp))
        }
    }

    @Composable
    private fun CommentTextField() {
        Column {
            Text(
                text = "Comment",
                modifier = Modifier.padding(top = 10.dp)
            )
            OutlinedTextField (value = fieldValuesModel.commentFieldValue.value,
                onValueChange = { run {
                    fieldValuesModel.commentFieldValue.value = it
                    if (selectedID != -1) {
                        val currentString = this@MainWindowView.stringList[selectedID]
                        this@MainWindowView.stringList[selectedID] = LocalizedString(
                            currentString.id,
                            currentString.text,
                            it,
                            mark = currentString.mark,
                            copyrightHeader = currentString.copyrightHeader
                        )
                    }
                } },
                modifier = Modifier
                    .padding(top = 0.dp)
                    .size(width = 450.dp, height = 160.dp))
        }
    }

    @Composable
    private fun StringMarkHeader(text: String) {
        Text(
            text = text,
            color = Color(30, 144, 255),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color(242, 245, 251))
                .padding(8.dp)
                .width(284.dp)
        )
    }

    @Composable
    private fun retrieveStringList(): List<LocalizedString> {
        val parser = LocalizeParser()
        val window = LocalAppWindow.current
        val openDialog = FileDialog(window.window)
        openDialog.isVisible = true
        if (openDialog.files.isEmpty()) {
            return listOf(LocalizedString("No file loaded", "", ""))
        }
        val stringFile = openDialog.files[0]
        waitForFile.value = false
        return parser.fromFile(stringFile)
    }

    @Composable
    private fun checkIfOpenButtonClicked() {
        if (waitForFile.value) {
            val newList = retrieveStringList()
            if (newList.isNotEmpty()) {
                stringList.clear()
                newList.forEach { stringList.add(it) }
                setTextFieldDefaultValues()
            }
            //println("called")
        }
    }

    @Composable
    private fun AppMenuBar(): MenuBar =
        MenuBar(
            Menu(
                name = "File",
                MenuItem(
                    name = "Open...",
                    onClick = {
                        waitForFile.value = true
                    },
                    shortcut = KeyStroke(Key.O)
                )
            )
        )

    @Composable
    private fun setTextFieldDefaultValues() {
        if (stringList.isNotEmpty()) {
            fieldValuesModel.stringFieldValue.value = stringList[0].text
            fieldValuesModel.commentFieldValue.value = stringList[0].comment
            selectedID = 0
        }
    }
}
