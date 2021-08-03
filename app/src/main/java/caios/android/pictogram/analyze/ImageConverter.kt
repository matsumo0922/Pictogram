package caios.android.pictogram.analyze

import android.content.ContentValues.TAG
import android.graphics.*
import android.media.Image
import android.util.Log

import android.util.Size
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


object ImageConverter {

    // ImageProxy -> Bitmap
    fun imageToToBitmap(image: Image, rotationDegrees: Int): Bitmap {
        val data = imageToByteArray(image)
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
        return cropBitmap(bitmap, Size(MODEL_WIDTH, MODEL_HEIGHT), rotationDegrees)
    }

    // TFLに渡せるサイズに加工
    private fun cropBitmap(bitmap: Bitmap, viewSize: Size, rotationDegrees: Int): Bitmap {
        val viewRatio = viewSize.width.toFloat() / viewSize.height.toFloat()
        val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        val startX: Int
        val startY: Int
        val sWidth: Int
        val sHeight: Int

        val sMatrix = Matrix()

        when {
            viewRatio > bitmapRatio -> {
                startX = 0
                startY = ((bitmap.height - (bitmap.width / viewRatio)) / 2).toInt()
                sWidth = bitmap.width
                sHeight = (bitmap.width / viewRatio).toInt()
                sMatrix.postScale(viewSize.width.toFloat() / bitmap.width, viewSize.width.toFloat() / bitmap.width)
            }
            viewRatio < bitmapRatio -> {
                startX = ((bitmap.width - (bitmap.height * viewRatio)) / 2).toInt()
                startY = 0
                sWidth = (bitmap.height * viewRatio).toInt()
                sHeight = bitmap.height
                sMatrix.postScale(viewSize.height.toFloat() / bitmap.height, viewSize.height.toFloat() / bitmap.height)
            }
            else                                                        -> {
                return bitmap
            }
        }

        sMatrix.postRotate(rotationDegrees.toFloat(), sWidth / 2f, sHeight / 2f)
        sMatrix.preScale(-1f, 1f)

        return Bitmap.createBitmap(bitmap, startX, startY, sWidth, sHeight, sMatrix, true)
    }

    // Image -> JPEGのバイト配列
    private fun imageToByteArray(image: Image): ByteArray? {
        var data: ByteArray? = null

        if (image.format == ImageFormat.JPEG) {
            val planes = image.planes
            val buffer: ByteBuffer = planes[0].buffer

            data = ByteArray(buffer.capacity())
            buffer.get(data)
        } else if (image.format == ImageFormat.YUV_420_888) {
            data = NV21toJPEG(YUV_420_888toNV21(image), image.width, image.height)
        }

        return data
    }

    // YUV_420_888 -> NV21
    private fun YUV_420_888toNV21(image: Image): ByteArray {
        val nv21: ByteArray
        val yBuffer: ByteBuffer = image.planes[0].buffer
        val uBuffer: ByteBuffer = image.planes[1].buffer
        val vBuffer: ByteBuffer = image.planes[2].buffer
        val ySize: Int = yBuffer.remaining()
        val uSize: Int = uBuffer.remaining()
        val vSize: Int = vBuffer.remaining()

        nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        return nv21
    }

    // NV21 -> JPEG
    private fun NV21toJPEG(nv21: ByteArray, width: Int, height: Int): ByteArray? {
        val out = ByteArrayOutputStream()

        YuvImage(nv21, ImageFormat.NV21, width, height, null).also {
            it.compressToJpeg(Rect(0, 0, width, height), 100, out)
        }

        return out.toByteArray()
    }
}