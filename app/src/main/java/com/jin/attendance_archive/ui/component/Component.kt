package com.jin.attendance_archive.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jin.attendance_archive.res.LocalMyColorScheme

@OptIn(ExperimentalMaterial3Api::class)
object Component {
    @Composable
    fun Card(
        onClick: (() -> Unit)? = null,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        border: BorderStroke? = null,
        interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
        content: @Composable ColumnScope.() -> Unit
    ) {
        val colors = CardDefaults.cardColors(
            containerColor = LocalMyColorScheme.current.cardContainer,
            contentColor = LocalMyColorScheme.current.onCardContainer
        )
        if (onClick != null) Card(
            onClick,
            modifier,
            enabled,
            RoundedCornerShape(12f.dp),
            colors,
            CardDefaults.cardElevation(defaultElevation = 3f.dp),
            border,
            interactionSource,
            content
        ) else Card(
            modifier,
            RoundedCornerShape(12f.dp),
            colors,
            CardDefaults.cardElevation(defaultElevation = 3f.dp),
            border,
            content
        )
    }

    @Composable
    fun CardMsg(
        onClick: (() -> Unit)? = null,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        border: BorderStroke? = null,
        interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
        content: @Composable ColumnScope.() -> Unit
    ) {
        val colors = CardDefaults.cardColors(
            containerColor = LocalMyColorScheme.current.cardMsgContainer,
            contentColor = LocalMyColorScheme.current.onCardMsgContainer
        )
        if (onClick != null) Card(
            onClick,
            modifier,
            enabled,
            RoundedCornerShape(12f.dp),
            colors,
            CardDefaults.cardElevation(defaultElevation = 3f.dp),
            border,
            interactionSource,
            content
        ) else Card(
            modifier,
            RoundedCornerShape(12f.dp),
            colors,
            CardDefaults.cardElevation(defaultElevation = 3f.dp),
            border,
            content
        )
    }
}