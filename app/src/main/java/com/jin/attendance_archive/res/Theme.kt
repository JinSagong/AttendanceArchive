package com.jin.attendance_archive.res

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jin.attendance_archive.res.MyColorScheme

@Composable
fun MyTheme(
    colorScheme: ColorScheme?,
    content: @Composable () -> Unit
) {
    val myColorScheme = if (isSystemInDarkTheme()) MyDarkColorScheme else MyLightColorScheme
    val rememberedMyColorScheme = remember { myColorScheme.copy() }.apply { update(myColorScheme) }
    val currentColorScheme =
        colorScheme ?: if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    CompositionLocalProvider(LocalMyColorScheme provides rememberedMyColorScheme) {
        MaterialTheme(colorScheme = currentColorScheme, typography = Typography) {
            Surface(modifier = Modifier.fillMaxSize()) { content.invoke() }
        }
    }
}

val LocalMyColorScheme = staticCompositionLocalOf { MyLightColorScheme }

/*
0. Common
status bar: primary
background: surface
text: onSurface

1. Checkbox
on: primary(outline), onPrimary(check)
off: onSurfaceVariant

2. Button
primary(bg)
onPrimary(content)

3. TextField
surfaceVariant(bg)
onSurfaceVariant(unfocused)
primary(cursor, focused)
onSurface(text)

4. OutlineTextField
outline(unfocused)
onSurfaceVariant(label, holder)
primary(cursor, focused)
onSurface(text)

5. Tab
primary

==> summary
primary: checkbox(on-outline), button(bg), outlineTF(cursor, focused), TF(cursor, focused), tab
onPrimary: checkbox(on-check), button(content)
surface: bg
onSurface: text, outlineTF(text), TF(text)
surfaceVariant: TF(bg)
onSurfaceVariant: checkbox(off), outlineTF(label, holder), TF(unfocused)
outline: outlineTF(unfocused)
**/

val MyLightColorScheme = MyColorScheme(
    cardContainer = Colors.cardContainerLight,
    onCardContainer = Colors.onCardContainerLight,
    cardMsgContainer = Colors.cardMsgContainerLight,
    onCardMsgContainer = Colors.onCardMsgContainerLight,
    progress = Colors.progressLight,
    red=Colors.redLight,
    green=Colors.greenLight,
    blue=Colors.blueLight,
    yellow=Colors.yellowLight
)

val MyDarkColorScheme = MyColorScheme(
    cardContainer = Colors.cardContainerDark,
    onCardContainer = Colors.onCardContainerDark,
    cardMsgContainer = Colors.cardMsgContainerDark,
    onCardMsgContainer = Colors.onCardMsgContainerDark,
    progress = Colors.progressDark,
    red=Colors.redDark,
    green=Colors.greenDark,
    blue=Colors.blueDark,
    yellow=Colors.yellowDark
)

val LightColorScheme = lightColorScheme(
    primary = Colors.primaryLight,
    onPrimary = Colors.onPrimaryLight,
    surface = Colors.surfaceLight,
    onSurface = Colors.onSurfaceLight,
    surfaceVariant = Colors.surfaceVariantLight,
    onSurfaceVariant = Colors.onSurfaceVariantLight,
    outline = Colors.outlineLight
)

val DarkColorScheme = darkColorScheme(
    primary = Colors.primaryDark,
    onPrimary = Colors.onPrimaryDark,
    surface = Colors.surfaceDark,
    onSurface = Colors.onSurfaceDark,
    surfaceVariant = Colors.surfaceVariantDark,
    onSurfaceVariant = Colors.onSurfaceVariantDark,
    outline = Colors.outlineDark
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    )
)