package caios.android.pictogram.analyze

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

typealias EstimationCallback = ((posture: PostureData, bitmap: Bitmap) -> Unit)

class PostureEstimator(
    private val context: Context,
    private val device: Device,
    private val listener: EstimationCallback
): ImageAnalysis.Analyzer {

    private val interpriter = PoseNetInterpriter(context, device)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val bitmap = image.image?.let { ImageConverter.imageToToBitmap(it, image.imageInfo.rotationDegrees) } ?: return
        val posture = interpriter.estimatePosture(bitmap)

        image.close()

        listener(posture, bitmap)
    }
}