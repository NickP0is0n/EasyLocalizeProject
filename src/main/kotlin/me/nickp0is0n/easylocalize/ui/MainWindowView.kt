package me.nickp0is0n.easylocalize.ui

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.nickp0is0n.easylocalize.models.LocalizedString
import me.nickp0is0n.easylocalize.models.ParserSettings
import me.nickp0is0n.easylocalize.utils.LocalizeParser
import java.awt.FileDialog
import java.io.*

class MainWindowView {
    private lateinit var stringList: SnapshotStateList<LocalizedString>
    private lateinit var fieldValuesModel: FieldValuesViewModel
    private var currentSaveFile: File? = null
    private var selectedID = -1
    private val controller = MainWindowController()
    private val waitForFile = mutableStateOf(false)
    private val waitForSave = mutableStateOf(false)
    private val parserSettings = ParserSettings()

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
            if (selectedID == -1) {
                setTextFieldDefaultValues()
            }
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
            checkIfSaveButtonClicked()
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
                if (selectedID != -1 && currentSaveFile != null) {
                    saveProjectFile()
                }
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
                readOnly = true,
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
        currentSaveFile = null
        val parser = LocalizeParser()
        val window = LocalAppWindow.current
        val openDialog = FileDialog(window.window)
        openDialog.isVisible = true
        if (openDialog.files.isEmpty()) {
            return listOf(LocalizedString("No file loaded", "", ""))
        }
        if (openDialog.files[0].extension == "elproject") {
            currentSaveFile = openDialog.files[0]
            ObjectInputStream(FileInputStream(currentSaveFile!!)).use {
                return it.readObject() as List<LocalizedString>
            }
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
                selectedID = -1
                setTextFieldDefaultValues()
            }
            //println("called")
        }
    }

    @Composable
    private fun checkIfSaveButtonClicked() {
        if (waitForSave.value) {
            val window = LocalAppWindow.current
            val saveDialog = FileDialog(window.window)
            saveDialog.mode = FileDialog.SAVE
            saveDialog.file = "*.elproject"
            saveDialog.isVisible = true
            if (saveDialog.files.isNotEmpty()) {
                currentSaveFile = saveDialog.files[0]
                saveProjectFile()
            }
            waitForSave.value = false
        }
    }

    private fun saveProjectFile() {
        CoroutineScope(Dispatchers.IO).launch {
            writeToProjectFile()
        }
    }

    private suspend fun writeToProjectFile() = withContext(Dispatchers.IO) {
        val list = stringList.toList()
        try {
            ObjectOutputStream(FileOutputStream(currentSaveFile!!)).use {
                it.writeObject(list)
            }
        }
        catch (e: IOException) {
            e.printStackTrace()
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
                ),
                MenuItem(
                    name = "Save project as...",
                    onClick = {
                        waitForSave.value = true
                    },
                    shortcut = KeyStroke(Key.O)
                )
            )
        )

    @Composable
    private fun setTextFieldDefaultValues() {
        if (selectedID == -1 && stringList.isNotEmpty()) {
            fieldValuesModel.stringFieldValue.value = stringList[0].text
            fieldValuesModel.commentFieldValue.value = stringList[0].comment
            selectedID = 0
        }
    }
}
