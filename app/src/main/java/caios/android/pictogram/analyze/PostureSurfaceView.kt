package caios.android.pictogram.analyze

import android.annotation.SuppressLint
import android.graphics.*
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import caios.android.pictogram.R
import kotlin.math.min

@SuppressLint("ViewConstructor")
class PostureSurfaceView(surfaceView: SurfaceView): SurfaceView(surfaceView.context), SurfaceHolder.Callback {

    private val surfaceHolder = surfaceView.holder
    private val paint = Paint()

    private val minConfidence = 0.5

    private val bodyJoints = listOf(
        Pair(BodyPart.LEFT_WRIST, BodyPart.LEFT_ELBOW),
        Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    init {
        surfaceHolder.addCallback(this)
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT)
        surfaceView.setZOrderOnTop(true)
        setPaint()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

    override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

    fun drawPosture(postureData: PostureData, bitmap: Bitmap, viewSize: Size) {
        val canvas: Canvas? = surfaceHolder.lockCanvas()
        val scaleX = viewSize.width.toFloat() / bitmap.width
        val scaleY = viewSize.height.toFloat() / bitmap.height

        canvas?.drawColor(0, PorterDuff.Mode.CLEAR)
        canvas?.drawBitmap(bitmap, null, RectF(0f, 0f, bitmap.width * scaleX , bitmap.height * scaleY), null /*Paint().apply {
            isFilterBitmap = true
        }*/)

        for (keyPoint in postureData.keyPoints) {
            if (keyPoint.score > minConfidence) {
                canvas?.drawCircle(keyPoint.position.x * scaleX, keyPoint.position.y * scaleY, 4f, paint)
            }
        }

        for(line in bodyJoints) {
            val firstPoint = postureData.keyPoints[line.first.ordinal]
            val secondPoint = postureData.keyPoints[line.second.ordinal]

            if(firstPoint.score > minConfidence && secondPoint.score > minConfidence) {
                canvas?.drawLine(
                    firstPoint.position.x * scaleX,
                    firstPoint.position.y * scaleY,
                    secondPoint.position.x * scaleX,
                    secondPoint.position.y * scaleY
                    , paint
                )
            }
        }

        surfaceHolder.unlockCanvasAndPost(canvas ?: return)
    }

    private fun setPaint() {
        paint.color = ContextCompat.getColor(context, R.color.colorAccentGreen)
        paint.strokeWidth = 3f
    }
}