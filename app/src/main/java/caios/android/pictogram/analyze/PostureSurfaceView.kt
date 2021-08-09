package caios.android.pictogram.analyze

import android.annotation.SuppressLint
import android.graphics.*
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import caios.android.pictogram.R
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

@SuppressLint("ViewConstructor")
class PostureSurfaceView(surfaceView: SurfaceView) : SurfaceView(surfaceView.context), SurfaceHolder.Callback {

    private val surfaceHolder = surfaceView.holder
    private val paint = Paint()

    private val minConfidence = 0.5f

    //各パーツを顔の何倍で描写するか
    private val pictogramPartsSize = mapOf(
        BodyPart.RIGHT_WRIST to 0.3f,
        BodyPart.RIGHT_ELBOW to 0.5f,
        BodyPart.RIGHT_SHOULDER to 0.7f,
        BodyPart.LEFT_WRIST to 0.3f,
        BodyPart.LEFT_ELBOW to 0.5f,
        BodyPart.LEFT_SHOULDER to 0.7f,
        BodyPart.RIGHT_HIP to 0.7f,
        BodyPart.RIGHT_KNEE to 0.5f,
        BodyPart.RIGHT_ANKLE to 0.3f,
        BodyPart.LEFT_HIP to 0.7f,
        BodyPart.LEFT_KNEE to 0.5f,
        BodyPart.LEFT_ANKLE to 0.3f,
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

    private fun filterKeyPoints(keyPoints: List<KeyPoint>, scaleX: Float, scaleY: Float, thresholds: Float): List<KeyPoint> {
        val filteredList = mutableListOf<KeyPoint>()

        for (keyPoint in keyPoints) {
            if (keyPoint.score > thresholds) {
                filteredList.add(KeyPoint(keyPoint.bodyPart, rescalePosition(keyPoint.position, scaleX, scaleY), keyPoint.score))
            }
        }

        return filteredList
    }

    private fun drawFace(canvas: Canvas?, keyPoints: List<KeyPoint>): Float {
        val rightEarPosition = keyPoints.find { it.bodyPart == BodyPart.RIGHT_EAR }?.position
        val leftEarPosition = keyPoints.find { it.bodyPart == BodyPart.LEFT_EAR }?.position
        val nosePosition = keyPoints.find { it.bodyPart == BodyPart.NOSE }?.position
        val earPosition = rightEarPosition ?: leftEarPosition

        var radius = 0f

        if (earPosition != null && nosePosition != null && (rightEarPosition == null || leftEarPosition == null)) {
            val distance = getDistance(nosePosition, earPosition)
            val centerX = (nosePosition.x + earPosition.x) / 2f
            val centerY = (nosePosition.y + earPosition.y) / 2f

            radius = distance / 1.1f

            canvas?.drawCircle(centerX, centerY, radius, paint)
        } else if (nosePosition != null && rightEarPosition != null && leftEarPosition != null) {
            val distance = getDistance(leftEarPosition, rightEarPosition)
            val centerX = (nosePosition.x + rightEarPosition.x + leftEarPosition.x) / 3f
            val centerY = (nosePosition.y + rightEarPosition.y + leftEarPosition.y) / 3f

            radius = distance / 1.5f

            canvas?.drawCircle(centerX, centerY, radius, paint)
        }

        return radius
    }

    private fun drawBodyPoint(canvas: Canvas?, keyPoints: List<KeyPoint>, faceSize: Float) {
        for (keyPoint in keyPoints) {
            val radius = faceSize * (pictogramPartsSize[keyPoint.bodyPart] ?: continue)
            canvas?.drawCircle(keyPoint.position.x.toFloat(), keyPoint.position.y.toFloat(), radius, paint)
        }
    }

    private fun drawBodyJoint(canvas: Canvas?, keyPoints: List<KeyPoint>, faceSize: Float) {
        for (parts in pictogramPartsJoint) {
            val firstPoint = keyPoints.find { it.bodyPart == parts.first } ?: continue
            val secondPoint = keyPoints.find { it.bodyPart == parts.second } ?: continue
            val firstRadius = faceSize * (pictogramPartsSize[firstPoint.bodyPart] ?: continue)
            val secondRadius = faceSize * (pictogramPartsSize[secondPoint.bodyPart] ?: continue)

            val lineList = getExternalCommonTangent(
                firstPoint.position.x.toFloat(),
                firstPoint.position.y.toFloat(),
                firstRadius,
                secondPoint.position.x.toFloat(),
                secondPoint.position.y.toFloat(),
                secondRadius
            )

            val points = lineList.map { listOf(PointF(it.firstX, it.firstY), PointF(it.secondX, it.secondY)) }.flatten()
            val maxPoint = points.maxByOrNull { it.y } ?: continue
            val minPoint = points.minByOrNull { it.y } ?: continue
            val rectAngle = sortByJarvis(points, minPoint, maxPoint, minPoint, true, emptyList())

            val path = Path().apply {
                for ((index, point) in rectAngle.withIndex()) {
                    if(index == 0) moveTo(point.x, point.y)
                    else lineTo(point.x, point.y)
                }
                close()
            }

            canvas?.drawPath(path, paint)
        }
    }

    fun drawPosture(postureData: PostureData, bitmap: Bitmap, viewSize: Size, time: Long): List<KeyPoint> {
        val canvas: Canvas? = surfaceHolder.lockCanvas()
        val scaleX = viewSize.width.toFloat() / bitmap.width
        val scaleY = viewSize.height.toFloat() / bitmap.height

        canvas?.drawColor(0, PorterDuff.Mode.CLEAR)

        val drawKeyPoints = filterKeyPoints(postureData.keyPoints, scaleX, scaleY, minConfidence)
        val faceSize = drawFace(canvas, drawKeyPoints)

        drawBodyPoint(canvas, drawKeyPoints, faceSize)
        drawBodyJoint(canvas, drawKeyPoints, faceSize)

        canvas?.drawText("FPS: %.2f".format(1 / (time.toDouble() / 1000)), 0f * scaleX, 15f * scaleY, paint)

        surfaceHolder.unlockCanvasAndPost(canvas ?: return emptyList())

        return drawKeyPoints
    }

    private fun setPaint() {
        paint.color = ContextCompat.getColor(context, R.color.colorAccentGreen)
        paint.strokeWidth = 12f
        paint.textSize = 34f
    }

    private fun rescalePosition(position: Position, scaleX: Float, scaleY: Float): Position {
        return Position((position.x * scaleX).toInt(), (position.y * scaleY).toInt())
    }

    private fun getDistance(point1: Position, point2: Position): Float {
        val x = abs(point1.x.toFloat() - point2.x)
        val y = abs(point1.y.toFloat() - point2.y)
        return sqrt(x * x + y * y)
    }

    private fun sortByJarvis(points: List<PointF>, representativePoint: PointF, maxPointF: PointF, minPointF: PointF, chainFlag: Boolean, hullPoints: List<PointF>): List<PointF> {
        val pointAndAngleMap = mutableListOf<Pair<PointF, Float>>()

        for (point in points) {
            if (point == representativePoint) continue

            val x = point.x - representativePoint.x
            val y = point.y - representativePoint.y
            val rad = atan2(y, x)
            val deg = (rad * 180 / Math.PI).toFloat()
            val angle = if(chainFlag) deg else deg - 180

            pointAndAngleMap.add(point to if (angle >= 0) angle else 360 + angle)
        }

        val nextPoint = pointAndAngleMap.minByOrNull { it.second }!!.first
        val nextChainFlag = chainFlag && representativePoint != maxPointF
        val newHullPoints = hullPoints.toMutableList().apply { add(representativePoint) }

        return if(nextPoint == minPointF) newHullPoints else sortByJarvis(points, nextPoint, maxPointF, minPointF, nextChainFlag, newHullPoints)
    }

    private fun getExternalCommonTangent(
        firstCircleX: Float,
        firstCircleY: Float,
        firstCircleRadius: Float,
        secondCircleX: Float,
        secondCircleY: Float,
        secondCircleRadius: Float
    ): List<Line> {
        val circleDistance = getDistance(Position(firstCircleX.toInt(), firstCircleY.toInt()), Position(secondCircleX.toInt(), secondCircleY.toInt()))

        if (circleDistance <= abs(firstCircleRadius - secondCircleRadius)) return emptyList()

        val unitVectorX = (secondCircleX - firstCircleX) / circleDistance
        val unitVectorY = (secondCircleY - firstCircleY) / circleDistance
        val resultTangentsList = mutableListOf<Line>()

        var sign1 = +1

        while (sign1 >= -1) {
            val unknownVector = (firstCircleRadius - sign1 * secondCircleRadius) / circleDistance

            if (unknownVector * unknownVector > 1.0) {
                sign1 -= 2
                continue
            }

            val h = sqrt(0.0f.coerceAtLeast(1.0f - unknownVector * unknownVector))
            var sign2 = +1

            while (sign2 >= -1) {
                val unknownVectorX = unitVectorX * unknownVector - sign2 * h * unitVectorY
                val unknownVectorY = unitVectorY * unknownVector + sign2 * h * unitVectorX

                resultTangentsList.add(
                    Line(
                        firstCircleX + firstCircleRadius * unknownVectorX,
                        firstCircleY + firstCircleRadius * unknownVectorY,
                        secondCircleX + sign1 * secondCircleRadius * unknownVectorX,
                        secondCircleY + sign1 * secondCircleRadius * unknownVectorY,
                    )
                )

                sign2 -= 2
            }
            sign1 -= 2
        }

        return resultTangentsList
    }

    private data class Line(
        val firstX: Float,
        val firstY: Float,
        val secondX: Float,
        val secondY: Float
    )
}
