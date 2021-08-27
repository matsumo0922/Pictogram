package caios.android.pictogram.analyze

import android.content.Context
import android.util.Log
import android.util.Size
import caios.android.pictogram.R
import caios.android.pictogram.data.*
import caios.android.pictogram.utils.LogUtils.TAG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.abs
import kotlin.math.sqrt

class PictogramComparator(
    private val context: Context,
    private val event: PictogramEvent
) {

    private val pictogramData = loadPictogramData()

    private fun loadPictogramData(): PictogramData {
        val json = context.resources.openRawResource(R.raw.pictogram).bufferedReader().use { it.readText() }
        val dataList = Gson().fromJson<Collection<PictogramData>>(json, object : TypeToken<Collection<PictogramData>>() {}.type)

        Log.d(TAG, "loadPictogramData: ${getEventName(event)}, $dataList")

        // なかったら死ぬ
        return dataList.find { it.eventName.equals(getEventName(event), ignoreCase = true) }!!
    }

    private fun getDistance(point1: Position, point2: Position): Float {
        val x = abs(point1.x.toFloat() - point2.x)
        val y = abs(point1.y.toFloat() - point2.y)
        return sqrt(x * x + y * y)
    }

    // 傾きが垂直の場合は9999を返す
    private fun getSlope(point1: Position, point2: Position): Float {
        val x = point1.x.toFloat() - point2.x
        val y = point1.y.toFloat() - point2.y
        val slope = y / x

        return if(!slope.isInfinite()) slope else 9999f
    }

    // 第1,第2引数が示す座標を真の値として、第3,第4が示す座標との相対誤差幅を返す（百分率じゃないよ）
    private fun comparateVector(firstPoint1: Position, firstPoint2: Position, secondPoint1: Position, secondPoint2: Position): Float {
        // 体格やカメラからの距離によって違う場合があるので廃止
        // val xDistance = getDistance(pointX1, pointX2)
        // val yDistance = getDistance(pointY1, pointY2)

        val firstSlope = getSlope(firstPoint1, firstPoint2)
        val secondSlope = getSlope(secondPoint1, secondPoint2)

        return abs(abs(firstSlope - secondSlope) / firstSlope)
    }

    fun comparate(keyPoints: List<KeyPoint>, viewSize: Size): Float {
        val scaleX = viewSize.width.toFloat() / pictogramData.imageSize.width
        val scaleY = viewSize.height.toFloat() / pictogramData.imageSize.height
        var scoreSum = 0f
        var scoreCount = 0

        if(keyPoints.size <= enumValues<BodyPart>().size * (2 / 3)) return 11f

        for(line in pictogramPartsJoint) {
            val aFirstPoint = keyPoints.find { it.bodyPart == line.first } ?: continue
            val aSecondPoint = keyPoints.find { it.bodyPart == line.second } ?: continue

            val cFirstPoint = pictogramData.bodyPoint.find { it.partsName.equals(bodyPartsName[line.first], ignoreCase = true) } ?: continue
            val cSecondPoint = pictogramData.bodyPoint.find { it.partsName.equals(bodyPartsName[line.second], ignoreCase = true) } ?: continue

            scoreSum += comparateVector(
                Position((cFirstPoint.x * scaleX).toInt(), (cFirstPoint.y * scaleY).toInt()),
                Position((cSecondPoint.x * scaleX).toInt(), (cSecondPoint.y * scaleY).toInt()),
                aFirstPoint.position,
                aSecondPoint.position,
            )
            scoreCount++
        }

        val average = (scoreSum / scoreCount).coerceAtMost(10f)

        if(average < 10f) {
            Log.d(TAG, "comparate: SCORE=$average, SUM=$scoreSum, COUNT=$scoreCount")
        }

        return average
    }

    private data class PictogramData(
        val eventName: String,
        val imageSize: ImageSize,
        val facePoint: BodyPoint,
        val bodyPoint: List<BodyPoint>
    )

    private data class BodyPoint(
        val partsName: String,
        val x: Float,
        val y: Float
    )

    private data class ImageSize(
        val width: Float,
        val height: Float
    )
}