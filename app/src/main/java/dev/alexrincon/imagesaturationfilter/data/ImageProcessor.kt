package dev.alexrincon.imagesaturationfilter.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

class ImageProcessor {

    fun applyFilter(
        input: Bitmap,
        saturation: Float
    ): Bitmap {

        val src = input.toSoftwareArgb8888()

        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val paint = Paint()

        val matrix = ColorMatrix().apply {
            setSaturation(saturation)
        }

        paint.colorFilter = ColorMatrixColorFilter(matrix)

        canvas.drawBitmap(src, 0f, 0f, paint)

        return output
    }

    /**
     * [Bitmap.Config.HARDWARE] (y otros formatos no compatibles con Canvas + ColorMatrix en CPU)
     * provocan fallos al copiar o dibujar; forzamos ARGB_8888 en software.
     */
    private fun Bitmap.toSoftwareArgb8888(): Bitmap {
        val cfg = config ?: return this
        return when (cfg) {
            Bitmap.Config.HARDWARE,
            Bitmap.Config.RGBA_F16 -> copy(Bitmap.Config.ARGB_8888, true)
            else -> this
        }
    }
}
