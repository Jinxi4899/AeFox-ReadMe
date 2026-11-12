package aefox.reyang.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import aefox.reyang.R

// Shared AeFox brand artwork. Every toast, snackbar, in-app notice and system
// notification pulls its icon from here so the shell stays visually consistent.
object AeFoxBrand {
    val smallIcon: Int = R.drawable.ic_stat_aefox
    val largeIcon: Int = R.drawable.aefox_icon_round
    val face: Int = R.drawable.aefox_face_open

    // Open -> 15 -> 33 -> 50 -> 66 -> 85 -> closed -> back, shared by splash and about.
    val blinkFrames: List<Int> = listOf(
        face,
        R.drawable.aefox_face_15,
        R.drawable.aefox_face_1,
        R.drawable.aefox_face_50,
        R.drawable.aefox_face_2,
        R.drawable.aefox_face_85,
        R.drawable.aefox_face_3,
        R.drawable.aefox_face_85,
        R.drawable.aefox_face_2,
        R.drawable.aefox_face_50,
        R.drawable.aefox_face_1,
        R.drawable.aefox_face_15,
        face,
    )
}

@Composable
fun AeFoxIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    contentDescription: String? = "AeFox",
) {
    Image(
        painter = painterResource(AeFoxBrand.largeIcon),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape),
    )
}

// Looping blink used by the About pages. Plays the 13-frame close/reopen
// sequence once, then holds the open frame still for a beat before looping, so
// the face is not blinking continuously. playMs / holdMs are tunable (ms).
@Composable
fun BlinkingAeFoxFace(
    modifier: Modifier = Modifier,
    playMs: Int = 700,
    holdMs: Int = 2300,
) {
    val totalMs = playMs + holdMs
    val transition = rememberInfiniteTransition(label = "AeFoxBlink")
    val elapsed by transition.animateFloat(
        initialValue = 0f,
        targetValue = totalMs.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = totalMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "AeFoxBlinkElapsed",
    )
    val frames = AeFoxBrand.blinkFrames
    val (index, blend) = if (elapsed < playMs) {
        // play the close/reopen sequence once
        val position = (elapsed / playMs) * (frames.size - 1)
        val i = position.toInt().coerceAtMost(frames.size - 2)
        i to (position - i).coerceIn(0f, 1f)
    } else {
        // hold the fully-open frame still
        0 to 0f
    }

    Box(modifier = modifier) {
        Image(
            painter = painterResource(frames[index]),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        Image(
            painter = painterResource(frames[index + 1]),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = blend },
        )
    }
}
