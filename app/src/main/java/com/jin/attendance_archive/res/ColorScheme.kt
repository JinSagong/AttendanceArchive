package com.jin.attendance_archive.res

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Stable
class MyColorScheme(
    cardContainer: Color,
    onCardContainer: Color,
    cardMsgContainer: Color,
    onCardMsgContainer: Color,
    progress: Color,
    red: Color,
    green: Color,
    blue: Color,
    yellow: Color
) {
    var cardContainer by mutableStateOf(cardContainer, structuralEqualityPolicy())
        internal set
    var onCardContainer by mutableStateOf(onCardContainer, structuralEqualityPolicy())
        internal set
    var cardMsgContainer by mutableStateOf(cardMsgContainer, structuralEqualityPolicy())
        internal set
    var onCardMsgContainer by mutableStateOf(onCardMsgContainer, structuralEqualityPolicy())
        internal set
    var progress by mutableStateOf(progress, structuralEqualityPolicy())
        internal set
    var red by mutableStateOf(red, structuralEqualityPolicy())
        internal set
    var green by mutableStateOf(green, structuralEqualityPolicy())
        internal set
    var blue by mutableStateOf(blue, structuralEqualityPolicy())
        internal set
    var yellow by mutableStateOf(yellow, structuralEqualityPolicy())
        internal set

    fun copy(
        cardContainer: Color = this.cardContainer,
        onCardContainer: Color = this.onCardContainer,
        cardMsgContainer: Color = this.cardMsgContainer,
        onCardMsgContainer: Color = this.onCardMsgContainer,
        progress: Color = this.progress,
        red: Color = this.red,
        green: Color = this.green,
        blue: Color = this.blue,
        yellow: Color = this.yellow
    ): MyColorScheme = MyColorScheme(
        cardContainer,
        onCardContainer,
        cardMsgContainer,
        onCardMsgContainer,
        progress,
        red,
        green,
        blue,
        yellow
    )

    fun update(colorScheme: MyColorScheme) {
        cardContainer = colorScheme.cardContainer
        onCardContainer = colorScheme.onCardContainer
        cardMsgContainer = colorScheme.cardMsgContainer
        onCardMsgContainer = colorScheme.onCardMsgContainer
        progress = colorScheme.progress
        red = colorScheme.red
        green = colorScheme.green
        blue = colorScheme.blue
        yellow = colorScheme.yellow
    }

    override fun toString(): String {
        return "ColorScheme(" +
                "cardContainer=$cardContainer" +
                "onCardContainer=$onCardContainer" +
                "cardMsgContainer=$cardMsgContainer" +
                "onCardMsgContainer=$onCardMsgContainer" +
                "progress=$progress" +
                "red=$red" +
                "green=$green" +
                "blue=$blue" +
                "yellow=$yellow" +
                ")"
    }
}