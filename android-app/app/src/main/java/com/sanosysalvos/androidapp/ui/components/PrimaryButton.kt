package com.sanosysalvos.androidapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sanosysalvos.androidapp.ui.theme.Primary
import com.sanosysalvos.androidapp.ui.theme.TextPrimary

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor = Primary),
        enabled = enabled
    ) {
        Text(text = text, color = TextPrimary)
    }
}
