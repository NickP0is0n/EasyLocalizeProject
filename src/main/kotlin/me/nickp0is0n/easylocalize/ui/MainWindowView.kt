package me.nickp0is0n.easylocalize.ui

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.BoxWithTooltip
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.ui.window.v1.KeyStroke
import androidx.compose.ui.window.v1.Menu
import androidx.compose.ui.window.v1.MenuBar
import androidx.compose.ui.window.v1.MenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.nickp0is0n.easylocalize.models.LocalizedString
import me.nickp0is0n.easylocalize.models.ParserSettings
import me.nickp0is0n.easylocalize.utils.AppInfo
import me.nickp0is0n.easylocalize.utils.LocalizeParser
import java.awt.FileDialog
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.*

class MainWindowView {
    private lateinit var stringList: SnapshotStateList<LocalizedString>
    private lateinit var fieldValuesModel: FieldValuesViewModel
    private var currentSaveFile: File? = null
    private var selectedID = -1
    private val controller = MainWindowController()
    private val waitForFile = mutableStateOf(false)
    private val waitForSave = mutableStateOf(false)
    private val waitForParserSettings = mutableStateOf(false)
    private val parserSettings = ParserSettings()
    private val searchBarText = mutableStateOf("")
    private var menuBarInitialized = false

    @Composable
    fun MainUI() {
        val window = LocalAppWindow.current
        if (!menuBarInitialized) {
            window.setMenuBar(
                AppMenuBar()
            )
            menuBarInitialized = true
        }

        Box(modifier = Modifier
            .background(color = Color(255, 255, 255))
            .fillMaxSize())
        Row {
            fieldValuesModel = FieldValuesViewModel(
                stringFieldValue = remember { mutableStateOf("Select an ID") },
                commentFieldValue = remember { mutableStateOf("Select an ID") }
            )

            stringList = remember { mutableStateListOf(*listOf(LocalizedString("No file loaded", "", "")).toTypedArray()) }
            Column {
                SearchBar()
                StringList(stringList
                    .filter { it.id.contains(searchBarText.value) || it.text.contains(searchBarText.value) || it.comment.contains(searchBarText.value) }
                    .toMutableList()
                    .also {
                        if (it.size == 0) {
                            it.add(LocalizedString("Not found", "", ""))
                        }
                    }
                )
            }
            if (selectedID == -1) {
                setTextFieldDefaultValues()
            }
            Column {
                StringTextField()
                CommentTextField()
                Row (modifier = Modifier.padding(top = 10.dp)) {
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
                    ) {
                        Text(text ="Export translations to file...", color = Color.White)
                    }
                    Button (
                        onClick = {
                            val selection = StringSelection(stringList[selectedID].toString())
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(30, 144, 255)),
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(text ="Copy string to clipboard", color = Color.White)
                    }
                }
            }
            checkIfOpenButtonClicked()
            checkIfSaveButtonClicked()
            checkIfParserSettingsButtonClicked()
        }
    }

    @Composable
    private fun SearchBar() {
        OutlinedTextField(
            value = searchBarText.value,
            onValueChange = {
               searchBarText.value = it
            },
            label = { Text("Search") },
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp)
                .size(width = 300.dp, height = 56.dp)
        )
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
        BoxWithTooltip(tooltip = {
            // composable tooltip content
            Surface(
                modifier = Modifier.shadow(4.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = item.text,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }) {
            Button(
                modifier = Modifier
                    .size(width = 300.dp, height = 50.dp)
                    .border(width = 1.dp, Color(245, 245, 245)),
                colors = if(stringList.indexOf(item) != selectedID || item.id == "No file loaded") ButtonDefaults.buttonColors(backgroundColor = Color.White) else ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                shape = RectangleShape,
                elevation = null,
                onClick = {
                    if (selectedID != -1 && currentSaveFile != null) {
                        saveProjectFile()
                    }
                    fieldValuesModel.stringFieldValue.value = item.text
                    fieldValuesModel.commentFieldValue.value = item.comment
                    selectedID = stringList.indexOf(item)
                    if (selectedID != -1) {
                        this@MainWindowView.stringList[selectedID] = this@MainWindowView.stringList[selectedID]
                    } //selection color workaround
                }
            ) {
                Text(item.id)
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
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
                    .padding(top = 10.dp)
                    .size(width = 450.dp, height = 160.dp)
                    .onKeyEvent {
                        when {
                            (it.isAltPressed && it.key == Key.DirectionDown) -> {
                                if (selectedID != -1 && currentSaveFile != null) {
                                    saveProjectFile()
                                }

                                if (selectedID != -1 && selectedID + 1 < stringList.size) {
                                    selectedID++
                                    fieldValuesModel.stringFieldValue.value = stringList[selectedID].text
                                    fieldValuesModel.commentFieldValue.value = stringList[selectedID].comment
                                    this@MainWindowView.stringList[selectedID] = this@MainWindowView.stringList[selectedID] //selection color workaround
                                }
                                true
                            }

                            (it.isAltPressed && it.key == Key.DirectionUp) -> {
                                if (selectedID != -1 && currentSaveFile != null) {
                                    saveProjectFile()
                                }

                                if (selectedID != -1 && selectedID - 1 > 0) {
                                    selectedID--
                                    fieldValuesModel.stringFieldValue.value = stringList[selectedID].text
                                    fieldValuesModel.commentFieldValue.value = stringList[selectedID].comment
                                    this@MainWindowView.stringList[selectedID] = this@MainWindowView.stringList[selectedID] //selection color workaround
                                }
                                true
                            }

                            else -> false
                        }
                    })
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
                    .padding(top = 10.dp)
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
        val parser = LocalizeParser(parserSettings)
        val window = LocalAppWindow.current
        val openDialog = FileDialog(window.window)

        openDialog.isVisible = true
        if (openDialog.files.isEmpty()) {
            LocalAppWindow.current.setTitle(AppInfo.windowTitle)
            return listOf(LocalizedString("No file loaded", "", ""))
        }
        waitForFile.value = false
        LocalAppWindow.current.setTitle(AppInfo.windowTitle + " – " + openDialog.files[0].name)
        if (openDialog.files[0].extension == "elproject") {
            currentSaveFile = openDialog.files[0]
            ObjectInputStream(FileInputStream(currentSaveFile!!)).use {
                return it.readObject() as List<LocalizedString>
            }
        }
        val stringFile = openDialog.files[0]
        return try { parser.fromFile(stringFile) } catch (e: IOException) { listOf(LocalizedString("No file loaded", "", "")) }
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

    @OptIn(ExperimentalComposeUiApi::class)
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
                    shortcut = KeyStroke(Key.S)
                )
            ),
            Menu(
                name = "Tools",
                MenuItem(
                    name = "Parser settings",
                    onClick = {
                        waitForParserSettings.value = true
                    }
                )
            )
        )

    @Composable
    private fun checkIfParserSettingsButtonClicked() {
        if (waitForParserSettings.value) {
            ParserSettingsView(parserSettings).SettingsWindow()
            waitForParserSettings.value = false
        }
    }

    @Composable
    private fun setTextFieldDefaultValues() {
        if (selectedID == -1 && stringList.isNotEmpty()) {
            fieldValuesModel.stringFieldValue.value = stringList[0].text
            fieldValuesModel.commentFieldValue.value = stringList[0].comment
            selectedID = 0
        }
    }
}
