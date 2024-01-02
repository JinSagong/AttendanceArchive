package com.jin.attendance_archive.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CheckBoxRow(
    checked: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier.clickable(onClick = onClick).padding(end = 8f.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onClick.invoke() },
            modifier = Modifier.wrapContentSize()
        )
        Text(
            text = text,
            modifier = Modifier.wrapContentSize(),
            fontSize = 14f.sp
        )
    }
}