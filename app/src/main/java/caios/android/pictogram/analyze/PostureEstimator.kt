package caios.android.pictogram.analyze

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Size
import android.view.WindowManager
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import caios.android.pictogram.R
import kotlin.system.measureTimeMillis

class PostureEstimator(
    private val context: Context,
    private val device: Device,
    private val listener: EstimationListener
): ImageAnalysis.Analyzer {

    private val interpriter = PoseNetInterpriter(context, device)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val bitmap: Bitmap
        val posture: PostureData

        val time = measureTimeMillis {
            try {
                bitmap = image.image?.let { ImageConverter.imageToToBitmap(it, image.imageInfo.rotationDegrees) } ?: return
                posture = interpriter.estimatePosture(bitmap)
                image.close()
            } catch (e: Throwable) {
                listener.onError(e)
                return
            }
        }

        listener.onSuccess(posture, bitmap, time)
    }

    interface EstimationListener {
        fun onSuccess(posture: PostureData, bitmap: Bitmap, time: Long)
        fun onError(e: Throwable)
    }
}