package caios.android.pictogram.analyze

import android.content.Context
import android.graphics.*
import caios.android.pictogram.data.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MoveNetInterpriter(context: Context, model: Model, device: Device): Interpriter(context, model, device){

    companion object {
        private const val MIN_CROP_KEYPOINT_SCORE = 0.2f
        private const val TORSO_EXPANSION_RATIO = 1.9f
        private const val BODY_EXPANSION_RATIO = 1.2f
    }

    private data class TorsoAndBodyDistance(
        val maxTorsoXDistance: Float,
        val maxTorsoYDistance: Float,
        val maxBodyXDistance: Float,
        val maxBodyYDistance: Float
    )

    private val interpriter = getInterpreter()
    private val inputWidth = interpriter.getInputTensor(0).shape()[1]
    private val inputHeight = interpriter.getInputTensor(0).shape()[2]
    private val outputShape = interpriter.getOutputTensor(0).shape()

    private var cropRegion: RectF? = null

    override fun estimatePosture(bitmap: Bitmap): PostureData {
        val cropRect = getCropRegion(cropRegion, bitmap.width, bitmap.height)
        val detectBitmap = createDetectBitmap(bitmap, cropRect)

        val inputTensorImage = createInputImage(detectBitmap, inputWidth, inputHeight)
        val outputTensorImage = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32)

        interpriter.run(inputTensorImage.tensorBuffer.buffer, outputTensorImage.buffer)

        val keyPointsCount = outputShape[2]
        val (keyPoints, positions, scoreSum) = getKeyPoints(outputTensorImage.floatArray, keyPointsCount, detectBitmap.width, detectBitmap.height)
        val transformedKeyPoints = transformPositions(cropRect, keyPoints, positions)

        cropRegion = determineNextRegion(transformedKeyPoints, bitmap.width, bitmap.height)

        return PostureData(transformedKeyPoints, scoreSum / keyPointsCount)
    }

    // indexはBodyPartの順序に対応。BodyPartの順序いじったら死ぬ。Kotlinの言語仕様変わったら死ぬ。
    // 戻り値（キーポイントのリスト, 座標のリスト, トータルスコア）
    private fun getKeyPoints(output: FloatArray, keyPointsCount: Int, width: Int, height: Int): Triple<List<KeyPoint>, List<Float>, Float> {
        val keyPoints = mutableListOf<KeyPoint>()
        val positions = mutableListOf<Float>()
        var scoreSum = 0f

        val widthRatio = width.toFloat() / inputWidth
        val heightRatio = height.toFloat() / inputHeight

        for (index in 0 until keyPointsCount) {
            val x = output[index * 3 + 1] * inputWidth * widthRatio
            val y = output[index * 3 + 0] * inputHeight * heightRatio
            val score = output[index * 3 + 2]

            positions.add(x)
            positions.add(y)
            keyPoints.add(KeyPoint(BodyPart.fromIndex(index)!!, Position(x.toInt(), y.toInt()), score))

            scoreSum += score
        }

        return Triple(keyPoints, positions, scoreSum)
    }

    // positionsの方じゃない。KeyPoint.positionの方（ややこしい）
    private fun transformPositions(region: RectF, keyPoints: List<KeyPoint>, positions: List<Float>): List<KeyPoint> {
        val newKeyPoints = keyPoints.toList()
        val points = positions.toFloatArray()

        Matrix().apply {
            postTranslate(region.left, region.top)
            mapPoints(points)
        }

        for(index in keyPoints.indices) {
            newKeyPoints[index].position = Position(points[index * 2].toInt(), points[index * 2 + 1].toInt())
        }

        return newKeyPoints
    }

    private fun determineNextRegion(keyPoints: List<KeyPoint>, width: Int, height: Int): RectF {
        takeIf { isTorsoVisible(keyPoints) } ?: return initRegion(width, height)

        val centerX = (keyPoints[BodyPart.LEFT_HIP.ordinal].position.x + keyPoints[BodyPart.RIGHT_HIP.ordinal].position.x) / 2f
        val centerY = (keyPoints[BodyPart.LEFT_HIP.ordinal].position.y + keyPoints[BodyPart.RIGHT_HIP.ordinal].position.y) / 2f

        val targetKeyPoints = keyPoints.map { KeyPoint(it.bodyPart, Position(it.position.x * width, it.position.y * height), it.score) }
        val torsoAndBodyDistance = getTorsoAndBodyDistance(keyPoints, targetKeyPoints, centerX, centerY)

        val originalDistanceList = listOf(centerX, width - centerX, centerY, height - centerY)
        val scaledDistanceList = listOf(
            torsoAndBodyDistance.maxTorsoXDistance * TORSO_EXPANSION_RATIO,
            torsoAndBodyDistance.maxTorsoYDistance * TORSO_EXPANSION_RATIO,
            torsoAndBodyDistance.maxBodyXDistance * BODY_EXPANSION_RATIO,
            torsoAndBodyDistance.maxBodyYDistance * BODY_EXPANSION_RATIO
        )

        val cropLengthHalf = min(scaledDistanceList.maxOrNull() ?: 0f, originalDistanceList.maxOrNull() ?: 0f)
        val cropCorner = Pair(centerX - cropLengthHalf, centerY - cropLengthHalf)

        return if(cropLengthHalf > max(width, height) / 2f) initRegion(width, height) else RectF(
            cropCorner.first / width,
            cropCorner.second / height,
            (cropCorner.first + cropLengthHalf * 2) / width,
            (cropCorner.second * cropLengthHalf * 2) / height
        )
    }

    // このフレームで胴体（肩か腰）が正確に予測できているかを返す
    // indexはBodyPartの順序に対応。BodyPartの順序いじったら死ぬ。Kotlinの言語仕様変わったら死ぬ。
    private fun isTorsoVisible(keyPoints: List<KeyPoint>): Boolean {
        val leftHipVisible = keyPoints[BodyPart.LEFT_HIP.ordinal].score > MIN_CROP_KEYPOINT_SCORE
        val rightHipVisible = keyPoints[BodyPart.RIGHT_HIP.ordinal].score > MIN_CROP_KEYPOINT_SCORE
        val leftShoulderVisible = keyPoints[BodyPart.LEFT_SHOULDER.ordinal].score > MIN_CROP_KEYPOINT_SCORE
        val rightShoulderVisible = keyPoints[BodyPart.RIGHT_SHOULDER.ordinal].score > MIN_CROP_KEYPOINT_SCORE

        return ((leftHipVisible || rightHipVisible) && (leftShoulderVisible || rightShoulderVisible))
    }

    private fun getTorsoAndBodyDistance(keyPoints: List<KeyPoint>, targetPoints: List<KeyPoint>, centerX: Float, centerY: Float): TorsoAndBodyDistance {
        var maxTorsoXDistance = 0f
        var maxTorsoYDistance = 0f

        for(index in listOf(BodyPart.LEFT_HIP.ordinal, BodyPart.RIGHT_HIP.ordinal, BodyPart.LEFT_SHOULDER.ordinal, BodyPart.RIGHT_SHOULDER.ordinal)) {
            val distanceX = abs(centerX - targetPoints[index].position.x)
            val distanceY = abs(centerY - targetPoints[index].position.y)

            if(distanceX > maxTorsoXDistance) maxTorsoXDistance = distanceX
            if(distanceY > maxTorsoYDistance) maxTorsoYDistance = distanceY
        }

        var maxBodyXDistance = 0f
        var maxBodyYDistance = 0f

        for(index in keyPoints.indices) {
            if(keyPoints[index].score < MIN_CROP_KEYPOINT_SCORE) continue

            val distanceX = abs(centerX - keyPoints[index].position.x)
            val distanceY = abs(centerY - keyPoints[index].position.y)

            if(distanceX > maxBodyXDistance) maxBodyXDistance = distanceX
            if(distanceY > maxBodyYDistance) maxBodyYDistance = distanceY
        }

        return TorsoAndBodyDistance(maxTorsoXDistance, maxTorsoYDistance, maxBodyXDistance, maxBodyYDistance)
    }

    private fun createInputImage(bitmap: Bitmap, width: Int, height: Int): TensorImage {
        val size = if(bitmap.height >= bitmap.width) bitmap.width else bitmap.height
        val tensorImage = TensorImage(DataType.FLOAT32)
        val imageprocessor = ImageProcessor.Builder().apply {
            add(ResizeWithCropOrPadOp(size, size))
            add(ResizeOp(width, height, ResizeOp.ResizeMethod.BILINEAR))
        }.build()

        tensorImage.load(bitmap)

        return imageprocessor.process(tensorImage)
    }

    private fun createDetectBitmap(bitmap: Bitmap, cropRect: RectF): Bitmap {
        return Bitmap.createBitmap(cropRect.width().toInt(), cropRect.height().toInt(), Bitmap.Config.ARGB_8888).apply {
            Canvas(this).drawBitmap(bitmap, -cropRect.left, -cropRect.top, Paint())
        }
    }

    private fun getCropRegion(oldRegion: RectF?, width: Int, height: Int): RectF {
        return (oldRegion ?: initRegion(width, height)).run {
            RectF(
                left * width,
                top * height,
                right * width,
                bottom * height
            )
        }
    }

    private fun initRegion(imageWidth: Int, imageHeight: Int): RectF {
        var xMin = 0f
        var yMin = 0f
        var width = 1f
        var height = 1f

        if (imageWidth > imageHeight) {
            height = imageWidth.toFloat() / imageHeight
            yMin = (imageHeight / 2f - imageWidth / 2f) / imageHeight
        } else {
            width = imageHeight.toFloat() / imageWidth
            xMin = (imageWidth / 2f - imageHeight / 2) / imageWidth
        }

        return RectF(xMin, yMin, xMin + width, yMin + height)
    }
}