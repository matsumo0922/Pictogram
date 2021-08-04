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

typealias EstimationCallback = ((posture: PostureData, bitmap: Bitmap, time: Long) -> Unit)

class PostureEstimator(
    private val context: Context,
    private val device: Device,
    private val listener: EstimationCallback
): ImageAnalysis.Analyzer {

    private val interpriter = PoseNetInterpriter(context, device)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val bitmap: Bitmap
        val posture: PostureData

        val time = measureTimeMillis {
            bitmap = image.image?.let { ImageConverter.imageToToBitmap(it, image.imageInfo.rotationDegrees) } ?: return
            posture = interpriter.estimatePosture(bitmap)
        }

        image.close()

        listener(posture, bitmap, time)
    }
}