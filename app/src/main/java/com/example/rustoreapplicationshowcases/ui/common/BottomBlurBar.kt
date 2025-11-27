package com.example.rustoreapplicationshowcases.ui.common

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Универсальный фон под нижней навигацией:
 * снимает область под собой в Bitmap, размывает и рисует как фон.
 *
 * Важно: это софтварный блюр, не использует BlurView / RenderScript,
 * поэтому не должен конфликтовать с RenderNode в Compose.
 */
@Composable
fun BottomBlurBar(
    modifier: Modifier = Modifier,
    barHeight: Dp = 80.dp,
    cornerRadius: Dp = barHeight / 2,
    overlayColor: Color = Color.White.copy(alpha = 0.10f),
    blurRadius: Int = 18,
    updateIntervalMs: Long = 60L,
    bottomPadding: Dp = 24.dp, // должен совпадать с vertical padding у бара
) {
    val view = LocalView.current
    val density = LocalDensity.current

    val barHeightPx = with(density) { barHeight.toPx().toInt() }
    val bottomPaddingPx = with(density) { bottomPadding.toPx().toInt() }

    var blurredImage by remember { mutableStateOf<ImageBitmap?>(null) }

    // Периодически обновляем скриншот и блюр только пока композабл в дереве
    LaunchedEffect(view, barHeightPx, blurRadius, updateIntervalMs) {
        if (barHeightPx <= 0) return@LaunchedEffect

        val rootView = view.rootView ?: return@LaunchedEffect
        val scale = 0.2f // даунскейл для скорости

        while (true) {
            val width = (rootView.width * scale).toInt()
            val height = (barHeightPx * scale).toInt()

            if (width <= 0 || height <= 0 || rootView.height <= 0) {
                delay(updateIntervalMs)
                continue
            }

            // Берём область ПОВЕРХ контрола бара, чтобы не захватывать сам BottomBlurBar
            // Диапазон бара: [barTop, barTop + barHeightPx]
            // Захватываем: [barTop - barHeightPx, barTop]
            val barTop = rootView.height - bottomPaddingPx - barHeightPx
            val top = barTop - barHeightPx
            if (top < 0) {
                delay(updateIntervalMs)
                continue
            }

            val srcBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(srcBitmap)

            // Рисуем только полосу под баром
            canvas.scale(scale, scale)
            canvas.translate(0f, -top.toFloat())
            rootView.draw(canvas)

            val config = srcBitmap.config ?: Bitmap.Config.ARGB_8888
            val blurred = srcBitmap.copy(config, true)

            // Блюр в фоновом потоке
            withContext(Dispatchers.Default) {
                stackBlur(blurred, blurRadius)
            }

            srcBitmap.recycle()

            blurredImage = blurred.asImageBitmap()

            delay(updateIntervalMs)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight),
        contentAlignment = Alignment.Center
    ) {
        val radius = cornerRadius

        if (blurredImage != null) {
            Image(
                bitmap = blurredImage!!,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(RoundedCornerShape(radius))
            )
        }

        // Лёгкий оверлей, чтобы сделать эффект "матового стекла"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(radius))
                .background(overlayColor)
        )
    }
}

private fun stackBlur(bitmap: Bitmap, radius: Int) {
    if (radius < 1) return

    val w = bitmap.width
    val h = bitmap.height
    if (w == 0 || h == 0) return

    val src = IntArray(w * h)
    bitmap.getPixels(src, 0, w, 0, 0, w, h)

    val tmp = IntArray(w * h)
    val dst = IntArray(w * h)

    // Горизонтальный бокс-блюр
    for (y in 0 until h) {
        for (x in 0 until w) {
            var rSum = 0
            var gSum = 0
            var bSum = 0
            var count = 0

            var k = -radius
            while (k <= radius) {
                val nx = x + k
                if (nx in 0 until w) {
                    val c = src[y * w + nx]
                    rSum += (c shr 16) and 0xFF
                    gSum += (c shr 8) and 0xFF
                    bSum += c and 0xFF
                    count++
                }
                k++
            }

            val idx = y * w + x
            val a = (src[idx] ushr 24) and 0xFF
            tmp[idx] = (a shl 24) or
                    ((rSum / count) shl 16) or
                    ((gSum / count) shl 8) or
                    (bSum / count)
        }
    }

    // Вертикальный бокс-блюр
    for (x in 0 until w) {
        for (y in 0 until h) {
            var rSum = 0
            var gSum = 0
            var bSum = 0
            var count = 0

            var k = -radius
            while (k <= radius) {
                val ny = y + k
                if (ny in 0 until h) {
                    val c = tmp[ny * w + x]
                    rSum += (c shr 16) and 0xFF
                    gSum += (c shr 8) and 0xFF
                    bSum += c and 0xFF
                    count++
                }
                k++
            }

            val idx = y * w + x
            val a = (tmp[idx] ushr 24) and 0xFF
            dst[idx] = (a shl 24) or
                    ((rSum / count) shl 16) or
                    ((gSum / count) shl 8) or
                    (bSum / count)
        }
    }

    bitmap.setPixels(dst, 0, w, 0, 0, w, h)
}

private fun clamp(value: Int, min: Int, max: Int): Int {
    return when {
        value < min -> min
        value > max -> max
        else -> value
    }
}


