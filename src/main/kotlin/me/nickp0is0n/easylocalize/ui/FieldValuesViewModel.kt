package me.nickp0is0n.easylocalize.ui

import androidx.compose.runtime.MutableState

data class FieldValuesViewModel (
    val stringFieldValue: MutableState<String>,
    val commentFieldValue: MutableState<String>
    )