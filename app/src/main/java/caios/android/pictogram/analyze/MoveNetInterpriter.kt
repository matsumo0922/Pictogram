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

class MoveNetInterpriter(context: Context, model: Model, device: Device): Interpriter(context, model, device){

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

        val (keyPoints, positions, scoreSum) = getKeyPoints(outputTensorImage.floatArray, outputShape[2], detectBitmap.width, detectBitmap.height)
        val transformedKeyPoints = transformPositions(cropRect, keyPoints, positions)
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
            newKeyPoints[index].position = Position(points[index * 2].toInt(), points[index * + 1].toInt())
        }

        return newKeyPoints
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