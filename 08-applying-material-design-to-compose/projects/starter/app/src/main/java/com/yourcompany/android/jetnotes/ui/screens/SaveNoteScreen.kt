/*
 * Copyright (c) 2021 Kodeco Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.yourcompany.android.jetnotes.ui.screens

import android.annotation.SuppressLint
import androidx.compose.compiler.plugins.kotlin.ComposeFqNames.remember
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomDrawer
import androidx.compose.material.BottomDrawerState
import androidx.compose.material.BottomDrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberBottomDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourcompany.android.jetnotes.R
import com.yourcompany.android.jetnotes.domain.model.ColorModel
import com.yourcompany.android.jetnotes.domain.model.NEW_NOTE_ID
import com.yourcompany.android.jetnotes.domain.model.NoteModel
import com.yourcompany.android.jetnotes.ui.components.NoteColor
import com.yourcompany.android.jetnotes.util.fromHex
import com.yourcompany.android.jetnotes.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SaveNoteScreen(
  viewModel: MainViewModel,
  onNavigateBack: () -> Unit = {}
) {

  val noteEntry: NoteModel by viewModel.noteEntry
    .observeAsState(initial = NoteModel())

  val colors: List<ColorModel> by viewModel.colors
    .observeAsState(listOf())

  val bottomDrawerState: BottomDrawerState = 
    rememberBottomDrawerState(initialValue = BottomDrawerValue.Closed)

  val coroutineScope = rememberCoroutineScope()
  val moveNoteToTrashDialogShownState: MutableState<Boolean> =
    rememberSaveable {
      mutableStateOf(false)
    }

  Scaffold(
    topBar = {
      val isEditingMode: Boolean = noteEntry.id != NEW_NOTE_ID
      SaveNoteTopAppBar(
        isEditingMode = isEditingMode,
        onBackClick = { onNavigateBack.invoke() },
        onSaveNoteClick = {
          viewModel.saveNote(noteEntry)
          onNavigateBack.invoke()
        },
        onOpenColorPickerClick = {
          coroutineScope.launch {
            bottomDrawerState.open()
          }
        },
        onDeleteNoteClick = {
          moveNoteToTrashDialogShownState.value = true
          /*viewModel.moveNoteToTrash(noteEntry)
          onNavigateBack.invoke()*/
        }
      )
    },
    content = {
      BottomDrawer(
        drawerState = bottomDrawerState,
        drawerContent =  {
          ColorPicker(
            colors = colors,
            onColorSelect = { color ->
              val newNoteEntry = noteEntry.copy(color = color)
              viewModel.onNoteEntryChange(newNoteEntry)
              coroutineScope.launch {
                bottomDrawerState.close()
              }
            }
          )
        },
        content =  {
          SaveNoteContent(
            note = noteEntry,
            onNoteChange = { updateNoteEntry ->
              viewModel.onNoteEntryChange(updateNoteEntry)
            }
          )
        }
      )

      if (moveNoteToTrashDialogShownState.value) {
        AlertDialog(
          onDismissRequest = {
            moveNoteToTrashDialogShownState.value = false
          },
          title = { Text("Move note to the trash?") },
          text = {
            Text(
              "Are you sure you want to " +
                      "move this note to the trash?"
            )
          },
          confirmButton = {
            TextButton(onClick = {
              viewModel.moveNoteToTrash(noteEntry)
              onNavigateBack.invoke()
            }) {
              Text("Confirm")
            }
          },
          dismissButton = {
            TextButton(onClick = {
              moveNoteToTrashDialogShownState.value = false
            }) {
              Text("Dismiss")
            }
          }
        )
      }
    }
  )
}

@Composable
private fun SaveNoteTopAppBar(
  isEditingMode: Boolean,
  onBackClick: () -> Unit,
  onSaveNoteClick: () -> Unit,
  onOpenColorPickerClick: () -> Unit,
  onDeleteNoteClick: () -> Unit
) {
  TopAppBar(
    title = {
      Text(
        text = "Save Note",
        color = MaterialTheme.colors.onPrimary
      )
    },
    navigationIcon = {
      IconButton(onClick = onBackClick) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "Save Note Button",
          tint = MaterialTheme.colors.onPrimary
        )
      }
    },
    actions = {
      // Save note action icon
      IconButton(onClick = onSaveNoteClick) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Save Note",
          tint = MaterialTheme.colors.onPrimary
        )
      }

      // Open color picker action icon
      IconButton(onClick = onOpenColorPickerClick) {
        Icon(
          painter = painterResource(id = R.drawable.ic_baseline_color_lens_24),
          contentDescription = "Open Color Picker Button",
          tint = MaterialTheme.colors.onPrimary
        )
      }

      // Delete action icon (show only in editing mode)
      if (isEditingMode) {
        IconButton(onClick = onDeleteNoteClick) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Note Button",
            tint = MaterialTheme.colors.onPrimary
          )

        }
      }
    }
  )
}

@Preview(showBackground = true)
@Composable
private fun SaveNoteTopAppBarPreview() {
  SaveNoteTopAppBar(
    isEditingMode = false,
    onBackClick = {},
    onSaveNoteClick = {},
    onOpenColorPickerClick = {},
    onDeleteNoteClick = {}
  )
}

@Composable
private fun SaveNoteContent(
  note: NoteModel,
  onNoteChange: (NoteModel) -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    ContentTextField(
      label = "Title",
      text = note.title,
      onTextChange = { newTitle ->
        onNoteChange.invoke(note.copy(title = newTitle))
      }
    )

    ContentTextField(
      modifier = Modifier
        .heightIn(max = 240.dp)
        .padding(top = 16.dp),
      label = "Body",
      text = note.content,
      onTextChange = { newContent ->
        onNoteChange.invoke(note.copy(content = newContent))
      }
    )

    val canBeCheckedOff: Boolean = note.isCheckedOff != null

    NoteCheckOption(
      isChecked = canBeCheckedOff,
      onCheckedChange = { canBeCheckedOffNewValue ->
        val isCheckOff: Boolean? = if (canBeCheckedOffNewValue) false else null
        onNoteChange.invoke(note.copy(isCheckedOff = isCheckOff))
      }
    )

    PickedColor(color = note.color)
  }
}

@Preview(showBackground = true)
@Composable
fun SaveNoteContentPreview() {
  SaveNoteContent(
    note = NoteModel(title = "Title", content = "content"),
    onNoteChange = {}
  )
}

@Composable
private fun ContentTextField(
  modifier: Modifier = Modifier,
  label: String,
  text: String,
  onTextChange: (String) -> Unit
) {
  TextField(
    value = text,
    onValueChange = onTextChange,
    label = { Text(label) },
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp),
    colors = TextFieldDefaults.textFieldColors(
      backgroundColor = MaterialTheme.colors.surface
    )
  )
}

@Preview(showBackground = true)
@Composable
fun ContentTextFieldPreview() {
  ContentTextField(label = "Title", text = "This is title", onTextChange = {})
}

@Composable
private fun NoteCheckOption(
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .padding(8.dp)
      .padding(top = 16.dp)
  ) {
    Text(
      text = "Can note be checked off?",
      modifier = Modifier
        .weight(1f)
        .align(Alignment.CenterVertically)
    )
    Switch(
      checked = isChecked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.padding(start = 8.dp)
    )
  }
}

@Preview(showBackground = true)
@Composable
fun NoteCheckOptionPreview() {
  NoteCheckOption(true, onCheckedChange = {})
}

@Composable
private fun PickedColor(color: ColorModel) {
  Row(
    Modifier
      .padding(8.dp)
      .padding(top = 16.dp)
  ) {
    Text(
      text = "Picked color",
      modifier = Modifier
        .weight(1f)
        .align(Alignment.CenterVertically)
    )
    NoteColor(
      color = Color.fromHex(color.hex),
      size = 40.dp,
      border = 1.dp,
      modifier = Modifier.padding(4.dp)
    )
  }
}

@Preview(showBackground = true)
@Composable
fun PickedColorPreview() {
  PickedColor(color = ColorModel.DEFAULT)
}


@Composable
private fun ColorPicker(
  colors: List<ColorModel>,
  onColorSelect: (ColorModel) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "Color picker",
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(8.dp)
    )
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
      items(colors.size) { itemIndex ->
        val color = colors[itemIndex]
        ColorItem(
          color = color,
          onColorSelect = onColorSelect
        )
      }
    }
  }
}

@Composable
fun ColorItem(
  color: ColorModel,
  onColorSelect: (ColorModel) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        onClick = {
          onColorSelect(color)
        }
      )
  ) {
    NoteColor(
      modifier = Modifier.padding(10.dp),
      color = Color.fromHex(color.hex),
      size = 80.dp,
      border = 2.dp
    )
    Text(
      text = color.name,
      fontSize = 22.sp,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .align(Alignment.CenterVertically)
    )
  }
}

@Preview
@Composable
fun ColorItemPreview() {
  ColorItem(ColorModel.DEFAULT) {}
}

@Preview
@Composable
fun ColorPickerPreview() {
  ColorPicker(
    colors = listOf(
      ColorModel.DEFAULT,
      ColorModel.DEFAULT,
      ColorModel.DEFAULT
    )
  ) { }
}