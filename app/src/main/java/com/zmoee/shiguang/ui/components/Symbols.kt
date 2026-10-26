package com.zmoee.shiguang.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zmoee.shiguang.R

/**
 * Material Symbols Rounded is bundled as a variable icon font. Using the font's glyphs keeps the
 * icon set consistent with Material 3 Expressive without shipping dozens of bitmap assets.
 */
enum class Sym(val glyph: String) {
    Home("\uE9B2"),
    Search("\uEF7A"),
    Favorite("\uE87E"),
    Settings("\uE8B8"),
    Menu("\uE5D2"),
    Mic("\uE31D"),
    Person("\uF0D3"),
    ChevronRight("\uE5CC"),
    Add("\uE145"),
    Image("\uE3F4"),
    PhotoCamera("\uE412"),
    ArrowBack("\uE5C4"),
    MoreVert("\uE5D4"),
    Edit("\uF097"),
    Delete("\uE92E"),
    Close("\uE5CD"),
    Check("\uE668"),
    Star("\uF09A"),
    Sort("\uE164"),
    Info("\uE88E"),
    DeleteForever("\uE92B"),
    PhotoLibrary("\uE413"),
    PersonAdd("\uEA4D"),
    AddAPhoto("\uE439"),
    Undo("\uE166"),
    Contacts("\uE0BA"),
}

private val MaterialSymbolsFont = FontFamily(
    Font(R.font.material_symbols_rounded),
)

@Composable
fun Symbol(
    symbol: Sym,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
) {
    val density = LocalDensity.current
    val fontSize = with(density) { size.toSp() }
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            text = symbol.glyph,
            color = tint,
            maxLines = 1,
            style = LocalTextStyle.current.merge(
                TextStyle(
                    fontFamily = MaterialSymbolsFont,
                    fontSize = fontSize,
                    lineHeight = fontSize,
                    letterSpacing = 0.sp,
                    textAlign = TextAlign.Center,
                ),
            ),
        )
    }
}
