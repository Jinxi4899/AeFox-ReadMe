package aefox.reyang.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class AeFoxToastMessage(val text: String, val id: Long)

class AeFoxToastHostState {
    var current: AeFoxToastMessage? by mutableStateOf(null)
        private set

    fun show(text: String) {
        current = AeFoxToastMessage(text, System.nanoTime())
    }

    fun dismiss() {
        current = null
    }
}

val LocalAeFoxToast = staticCompositionLocalOf { AeFoxToastHostState() }

// Toast replacement that always shows the AeFox avatar, including on Android 12+
// where the platform Toast ignores custom views.
@Composable
fun AeFoxToastHost(
    state: AeFoxToastHostState,
    modifier: Modifier = Modifier,
) {
    val message = state.current
    LaunchedEffect(message) {
        if (message != null) {
            delay(2200)
            state.dismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF2B2B2B).copy(alpha = 0.94f))
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AeFoxIcon(size = 28.dp)
            Spacer(Modifier.width(10.dp))
            Text(
                text = message?.text.orEmpty(),
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 2,
            )
        }
    }
}
