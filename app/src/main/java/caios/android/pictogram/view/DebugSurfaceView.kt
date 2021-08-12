package caios.android.pictogram.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.util.Size
import android.view.SurfaceView
import caios.android.pictogram.data.KeyPoint
import caios.android.pictogram.data.Position
import caios.android.pictogram.data.PostureData
import caios.android.pictogram.data.bodyPartsJoint

@SuppressLint("ViewConstructor")
class DebugSurfaceView(surfaceView: SurfaceView): ResultSurfaceView(surfaceView) {

    private val minConfidence = 0.5

    override fun drawPosture(postureData: PostureData, bitmap: Bitmap, viewSize: Size, time: Long): List<KeyPoint> {
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
            val firstPoint = postureData.keyPoints.find { line.first == it.bodyPart } ?: continue
            val secondPoint = postureData.keyPoints.find { line.second == it.bodyPart } ?: continue

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

        canvas?.drawText("FPS: %.2f".format(1 / (time.toDouble() / 1000)), 0f * scaleX, 15f * scaleY, paint)

        surfaceHolder.unlockCanvasAndPost(canvas ?: return emptyList())

        return drawKeyPoints
    }
}