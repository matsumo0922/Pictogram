package caios.android.pictogram.analyze

import android.annotation.SuppressLint
import android.graphics.*
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import caios.android.pictogram.R
import kotlin.math.abs
import kotlin.math.sqrt

@SuppressLint("ViewConstructor")
class PostureSurfaceView(surfaceView: SurfaceView): SurfaceView(surfaceView.context), SurfaceHolder.Callback {

    private val surfaceHolder = surfaceView.holder
    private val paint = Paint()

    private val minConfidence = 0.5

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

    fun drawPosture(postureData: PostureData, bitmap: Bitmap, viewSize: Size, time: Long): List<KeyPoint> {
        val canvas: Canvas? = surfaceHolder.lockCanvas()
        val scaleX = viewSize.width.toFloat() / bitmap.width
        val scaleY = viewSize.height.toFloat() / bitmap.height

        val drawKeyPoints = mutableListOf<KeyPoint>()

        canvas?.drawColor(0, PorterDuff.Mode.CLEAR)

        for (keyPoint in postureData.keyPoints) {
            if (keyPoint.score > minConfidence) {
                canvas?.drawCircle(keyPoint.position.x * scaleX, keyPoint.position.y * scaleY, 12f, paint)
                drawKeyPoints.add(KeyPoint(keyPoint.bodyPart, Position((keyPoint.position.x * scaleX).toInt(), (keyPoint.position.y * scaleY).toInt()), keyPoint.score))
            }
        }

        for(line in bodyPartsJoint) {
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

        val rightEarPosition = postureData.keyPoints.find { it.bodyPart == BodyPart.RIGHT_EYE }?.position
        val leftEarPosition = postureData.keyPoints.find { it.bodyPart == BodyPart.LEFT_EAR }?.position
        val nosePosition = postureData.keyPoints.find { it.bodyPart == BodyPart.NOSE }?.position

        if(rightEarPosition != null && leftEarPosition != null && nosePosition != null) {
            canvas?.drawCircle(nosePosition.x * scaleX, nosePosition.y * scaleY, getDistance(
                Position((rightEarPosition.x * scaleX).toInt(),(rightEarPosition.y * scaleY).toInt()),
                Position((leftEarPosition.x * scaleX).toInt(),(leftEarPosition.y * scaleY).toInt()),
            ) * 1.3f, paint)
        }

        canvas?.drawText("FPS: %.2f".format(1 / (time.toDouble() / 1000)), 0f * scaleX, 15f * scaleY, paint)

        surfaceHolder.unlockCanvasAndPost(canvas ?: return emptyList())

        return drawKeyPoints
    }

    private fun setPaint() {
        paint.color = ContextCompat.getColor(context, R.color.colorAccentGreen)
        paint.strokeWidth = 12f
        paint.textSize = 34f
    }

    private fun getDistance(point1: Position, point2: Position): Float {
        val x = abs(point1.x.toFloat() - point2.x)
        val y = abs(point1.y.toFloat() - point2.y)
        return sqrt(x * x + y * y)
    }
}