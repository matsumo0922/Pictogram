package caios.android.pictogram.view

import android.annotation.SuppressLint
import android.graphics.*
import android.util.Size
import android.view.SurfaceView
import caios.android.pictogram.data.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

@SuppressLint("ViewConstructor")
class PostureSurfaceView(surfaceView: SurfaceView) : ResultSurfaceView(surfaceView) {

    companion object {
        const val MIN_CONFIDENCE_SCORE_FACE = 0.2f
        const val MIN_CONFIDENCE_SCORE_BODY = 0.2f
    }

    private val pictogramFaceParts = listOf(
        BodyPart.RIGHT_EYE,
        BodyPart.LEFT_EYE,
        BodyPart.RIGHT_EAR,
        BodyPart.LEFT_EAR,
        BodyPart.NOSE
    )

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

    override fun drawPosture(postureData: PostureData, bitmap: Bitmap, viewSize: Size, time: Long): List<KeyPoint> {
        val canvas: Canvas? = surfaceHolder.lockCanvas()
        val scaleX = viewSize.width.toFloat() / bitmap.width
        val scaleY = viewSize.height.toFloat() / bitmap.height

        canvas?.drawColor(0, PorterDuff.Mode.CLEAR)

        val drawKeyPoints = filterKeyPoints(postureData.keyPoints, scaleX, scaleY)
        val faceSize = drawFace(canvas, drawKeyPoints)

        drawBodyPoint(canvas, drawKeyPoints, faceSize)
        drawBodyJoint(canvas, drawKeyPoints, faceSize)

        canvas?.drawText("FPS: %.2f".format(1 / (time.toDouble() / 1000)), 0f * scaleX, 15f * scaleY, paint)

        surfaceHolder.unlockCanvasAndPost(canvas ?: return emptyList())

        return drawKeyPoints
    }

    private fun filterKeyPoints(keyPoints: List<KeyPoint>, scaleX: Float, scaleY: Float): List<KeyPoint> {
        val filteredList = mutableListOf<KeyPoint>()

        for (keyPoint in keyPoints) {
            if (keyPoint.score > if(keyPoint.bodyPart in pictogramFaceParts) MIN_CONFIDENCE_SCORE_FACE else MIN_CONFIDENCE_SCORE_BODY) {
                filteredList.add(KeyPoint(keyPoint.bodyPart, rescalePosition(keyPoint.position, scaleX, scaleY), keyPoint.score))
            }
        }

        return filteredList
    }

    private fun drawFace(canvas: Canvas?, keyPoints: List<KeyPoint>): Float {
        val faceParts = mutableListOf<KeyPoint>()
        var sumX = 0f
        var sumY = 0f
        var count = 0f

        for(part in pictogramFaceParts) {
            keyPoints.find { it.bodyPart == part }?.let {
                faceParts.add(it)

                if(it.bodyPart != BodyPart.LEFT_EAR && it.bodyPart != BodyPart.RIGHT_EAR) {
                    sumX += it.position.x
                    sumY += it.position.y
                    count++
                }
            }
        }

        val center = Position((sumX / count).toInt(), (sumY / count).toInt())
        val distanceList = faceParts.map { getDistance(it.position, center) }.sortedDescending()
        val radius = distanceList.take(2).average().toFloat() * 1.2f

        canvas?.drawCircle(center.x.toFloat(), center.y.toFloat(), radius, paint)
        canvas?.drawCircle(center.x.toFloat(), center.y.toFloat(), 12f, Paint().apply { color = Color.RED })

        for (part in faceParts) {
            canvas?.drawCircle(part.position.x.toFloat(), part.position.y.toFloat(), 12f, Paint().apply { color = Color.BLUE })
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

        val nextPoint = pointAndAngleMap.minByOrNull { it.second }?.first
        val nextChainFlag = chainFlag && representativePoint != maxPointF
        val newHullPoints = hullPoints.toMutableList().apply { add(representativePoint) }

        return if(nextPoint == null || nextPoint == minPointF) newHullPoints else sortByJarvis(points, nextPoint, maxPointF, minPointF, nextChainFlag, newHullPoints)
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
