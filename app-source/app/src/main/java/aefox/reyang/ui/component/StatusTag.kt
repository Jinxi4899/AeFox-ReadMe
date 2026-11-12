package aefox.reyang.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import aefox.reyang.ui.theme.UiText

/**
 * Small pill label drawn in the classic status card's top row (SukiSU StatusTag):
 * 10sp medium text on a fully rounded background.
 */
@Composable
fun StatusTag(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        fontSize = UiText.Tag,
        fontWeight = FontWeight.Medium,
        color = textColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
