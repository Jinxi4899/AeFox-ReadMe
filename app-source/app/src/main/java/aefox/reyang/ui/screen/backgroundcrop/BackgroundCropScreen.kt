package aefox.reyang.ui.screen.backgroundcrop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import aefox.reyang.R
import aefox.reyang.ui.LocalUiMode
import aefox.reyang.ui.UiMode
import aefox.reyang.ui.component.LocalAeFoxToast
import aefox.reyang.ui.navigation3.LocalNavigator
import aefox.reyang.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Crop aspect presets; SCREEN follows the device aspect. */
enum class BackgroundCropRatio(val fixedAspect: Float?) {
    SCREEN(null),
    SQUARE(1f),
    PORTRAIT(9f / 16f),
    LANDSCAPE(16f / 9f),
    ;

    fun aspectFor(screenWidth: Int, screenHeight: Int): Float =
        fixedAspect ?: (screenWidth.coerceAtLeast(1).toFloat() /
            screenHeight.coerceAtLeast(1).toFloat())

    @Composable
    fun label(): String = stringResource(
        when (this) {
            SCREEN -> R.string.background_crop_ratio_screen
            SQUARE -> R.string.background_crop_ratio_square
            PORTRAIT -> R.string.background_crop_ratio_portrait
            LANDSCAPE -> R.string.background_crop_ratio_landscape
        },
    )
}

@Immutable
data class BackgroundCropUiState(
    val bitmap: Bitmap?,
    val loading: Boolean,
    val saving: Boolean,
    val rotation: Int,
    val ratio: BackgroundCropRatio,
    val aspect: Float,
)

@Immutable
data class BackgroundCropActions(
    val onBack: () -> Unit,
    val onConfirm: () -> Unit,
    val onUseOriginal: () -> Unit,
    val onSelectRatio: (BackgroundCropRatio) -> Unit,
    val onRotate: () -> Unit,
    val onReset: () -> Unit,
)

/**
 * Background crop dispatcher: center-crops the staged pick to the chosen aspect (with optional
 * quarter-turn rotation) and applies the result as the global background. The preview mirrors
 * exactly what is written, so no interactive viewport is needed.
 */
@Composable
fun BackgroundCropScreen(sourcePath: String) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val toast = LocalAeFoxToast.current
    val viewModel = viewModel<SettingsViewModel>()
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current

    var bitmap by remember(sourcePath) { mutableStateOf<Bitmap?>(null) }
    var loading by remember(sourcePath) { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var rotation by rememberSaveable(sourcePath) { mutableStateOf(0) }
    var ratioName by rememberSaveable(sourcePath) {
        mutableStateOf(BackgroundCropRatio.SCREEN.name)
    }
    val ratio = runCatching { BackgroundCropRatio.valueOf(ratioName) }
        .getOrDefault(BackgroundCropRatio.SCREEN)
    val aspect = ratio.aspectFor(configuration.screenWidthDp, configuration.screenHeightDp)

    LaunchedEffect(sourcePath) {
        bitmap = withContext(Dispatchers.IO) {
            decodeSampled(File(sourcePath), MAX_PREVIEW_DIMENSION)
        }
        loading = false
    }

    val state = BackgroundCropUiState(
        bitmap = bitmap,
        loading = loading,
        saving = saving,
        rotation = rotation,
        ratio = ratio,
        aspect = aspect,
    )
    val actions = BackgroundCropActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onConfirm = {
            if (!saving) {
                saving = true
                scope.launch {
                    val output = withContext(Dispatchers.IO) {
                        renderCrop(File(sourcePath), rotation, aspect, context.cacheDir)
                    }
                    saving = false
                    if (output == null) {
                        toast.show(context.getString(R.string.background_crop_failed))
                    } else {
                        viewModel.applyBackgroundCrop(output)
                        navigator.pop()
                    }
                }
            }
        },
        onUseOriginal = {
            if (!saving) {
                saving = true
                viewModel.applyBackgroundCrop(File(sourcePath))
                saving = false
                navigator.pop()
            }
        },
        onSelectRatio = { ratioName = it.name },
        onRotate = { rotation = (rotation + 90) % 360 },
        onReset = { rotation = 0 },
    )

    val preview: @Composable (Modifier) -> Unit = { modifier ->
        CropPreview(state, modifier)
    }
    when (LocalUiMode.current) {
        UiMode.Miuix -> BackgroundCropScreenMiuix(state, actions, preview)
        UiMode.Material -> BackgroundCropScreenMaterial(state, actions, preview)
    }
}

/** Center-crop preview with the same rotation and aspect the renderer uses. */
@Composable
fun CropPreview(state: BackgroundCropUiState, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(state.aspect)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black),
        ) {
            state.bitmap?.let { source ->
                Image(
                    bitmap = source.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = state.rotation.toFloat() },
                )
            }
        }
    }
}

/** Small local constant, keeps the preview file free of layout unit imports elsewhere. */
private const val MAX_PREVIEW_DIMENSION = 2048
private const val MAX_OUTPUT_WIDTH = 1920

private fun decodeSampled(file: File, maxDimension: Int): Bitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxDimension) sample *= 2
    BitmapFactory.decodeFile(
        file.absolutePath,
        BitmapFactory.Options().apply { inSampleSize = sample },
    )
}.getOrNull()

/**
 * Rotates, center-crops to [aspect] and scales to at most [MAX_OUTPUT_WIDTH]; the result is a
 * JPEG in the cache directory (caller applies then deletes it).
 */
private fun renderCrop(source: File, rotation: Int, aspect: Float, cacheDir: File): File? =
    runCatching {
        val decoded = decodeSampled(source, MAX_PREVIEW_DIMENSION) ?: return null
        val rotated = if (rotation == 0) {
            decoded
        } else {
            Bitmap.createBitmap(
                decoded,
                0,
                0,
                decoded.width,
                decoded.height,
                Matrix().apply { postRotate(rotation.toFloat()) },
                true,
            ).also { if (it !== decoded) decoded.recycle() }
        }
        val width = rotated.width.toFloat()
        val height = rotated.height.toFloat()
        val cropWidth: Float
        val cropHeight: Float
        if (width / height > aspect) {
            cropHeight = height
            cropWidth = height * aspect
        } else {
            cropWidth = width
            cropHeight = width / aspect
        }
        val cropW = cropWidth.toInt().coerceIn(1, rotated.width)
        val cropH = cropHeight.toInt().coerceIn(1, rotated.height)
        val x = ((rotated.width - cropW) / 2).coerceAtLeast(0)
        val y = ((rotated.height - cropH) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(rotated, x, y, cropW, cropH)
        if (cropped !== rotated) rotated.recycle()
        val outWidth = minOf(MAX_OUTPUT_WIDTH, cropW)
        val outHeight = maxOf(1, (outWidth / aspect).toInt())
        val scaled = if (cropW == outWidth && cropH == outHeight) {
            cropped
        } else {
            Bitmap.createScaledBitmap(cropped, outWidth, outHeight, true)
                .also { if (it !== cropped) cropped.recycle() }
        }
        val output = File(cacheDir, "background_crop_result.jpg")
        output.outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        scaled.recycle()
        output.takeIf { it.length() > 0L }
    }.getOrNull()
