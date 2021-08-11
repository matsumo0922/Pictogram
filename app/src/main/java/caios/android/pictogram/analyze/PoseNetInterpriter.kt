package caios.android.pictogram.analyze

import android.content.Context
import android.graphics.Bitmap
import caios.android.pictogram.data.*
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class PoseNetInterpriter(context: Context, model: Model, device: Device): Interpriter(context, model, device) {

    @Suppress("UNCHECKED_CAST")
    override fun estimatePosture(bitmap: Bitmap): PostureData {
        val nonNullInterpreter = getInterpreter()
        val inputBuffer = createInputBuffer(bitmap)
        val outputMap = createOutputMap(nonNullInterpreter)

        nonNullInterpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputMap)

        val heatmaps = outputMap[0] as Array<Array<Array<FloatArray>>>
        val offsets = outputMap[1] as Array<Array<Array<FloatArray>>>

        val height = heatmaps[0].size
        val width = heatmaps[0][0].size
        val numKeyPoints = heatmaps[0][0][0].size

        val keyPositions = getKeyPositions(heatmaps, height, width, numKeyPoints)
        val (confidenceScores, xCoords, yCoords) = adjustKeyPoints(bitmap, keyPositions, heatmaps, offsets, height, width, numKeyPoints)

        return getPosture(bitmap, confidenceScores, xCoords, yCoords, numKeyPoints)
    }

    private fun createInputBuffer(bitmap: Bitmap): ByteBuffer {
        val batchSize = 1
        val bytesPerChannel = 4
        val inputChannel = 3

        val pixels = IntArray(bitmap.width * bitmap.height)
        val inputBuffer = ByteBuffer.allocateDirect(batchSize * bytesPerChannel * bitmap.height * bitmap.width * inputChannel).also {
            it.order(ByteOrder.nativeOrder())
            it.rewind()
        }

        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            inputBuffer.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            inputBuffer.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            inputBuffer.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
        }

        return inputBuffer
    }

    private fun createOutputMap(interpreter: Interpreter): HashMap<Int, Any> {
        val heatMapsShape = interpreter.getOutputTensor(0).shape()
        val offsetsShape = interpreter.getOutputTensor(1).shape()
        val displacementsFwdShape = interpreter.getOutputTensor(2).shape()
        val displacementsBwdShape = interpreter.getOutputTensor(3).shape()

        return HashMap<Int, Any>().apply {
            put(0, Array(heatMapsShape[0]) { Array(heatMapsShape[1]) { Array(heatMapsShape[2]) { FloatArray(heatMapsShape[3]) } } })
            put(1, Array(offsetsShape[0]) { Array(offsetsShape[1]) { Array(offsetsShape[2]) { FloatArray(offsetsShape[3]) } } })
            put(2, Array(displacementsFwdShape[0]) { Array(displacementsFwdShape[1]) { Array(displacementsFwdShape[2]) { FloatArray(displacementsFwdShape[3]) } }})
            put(3, Array(displacementsBwdShape[0]) { Array(displacementsBwdShape[1]) { Array(displacementsBwdShape[2]) { FloatArray(displacementsBwdShape[3]) } }})
        }
    }

    //キーポイントから最も最適な位置(row, col)を取得
    private fun getKeyPositions(heatMaps: Array<Array<Array<FloatArray>>>, height: Int, width: Int, numKeyPoints: Int): Array<Position> {
        val keyPositions = Array(numKeyPoints) { Position(0, 0) }

        for(keyPoint in 0 until numKeyPoints) {
            var maxValue = heatMaps[0][0][0][keyPoint]
            var maxRow = 0
            var maxCol = 0

            for(row in 0 until height) {
                for(col in 0 until width) {
                    if(heatMaps[0][row][col][keyPoint] > maxValue) {
                        maxValue = heatMaps[0][row][col][keyPoint]
                        maxRow = row
                        maxCol = col
                    }
                }
            }

            keyPositions[keyPoint] = Position(maxRow, maxCol)
        }

        return keyPositions
    }

    //オフセットで座標を調整して信頼度を返す
    private fun adjustKeyPoints(
        bitmap: Bitmap,
        keyPositions: Array<Position>,
        heatMaps: Array<Array<Array<FloatArray>>>,
        offsets: Array<Array<Array<FloatArray>>>,
        height: Int,
        width: Int,
        numKeyPoints: Int
    ): Triple<FloatArray, IntArray, IntArray> {
        val confidenceScores = FloatArray(numKeyPoints)
        val xCoords = IntArray(numKeyPoints)
        val yCoords = IntArray(numKeyPoints)

        for((index, position) in keyPositions.withIndex()) {
            val positionY = keyPositions[index].x
            val positionX = keyPositions[index].y

            yCoords[index] = (position.x / (height - 1).toFloat() * bitmap.height + offsets[0][positionY][positionX][index]).toInt()
            xCoords[index] = (position.y / (width - 1).toFloat() * bitmap.width + offsets[0][positionY][positionX][index + numKeyPoints]).toInt()

            confidenceScores[index] = sigmoid(heatMaps[0][positionY][positionX][index])
        }

        return Triple(confidenceScores, xCoords, yCoords)
    }

    //姿勢データを取得
    private fun getPosture(bitmap: Bitmap, confidenceScores: FloatArray, xCoords: IntArray, yCoords: IntArray, numKeyPoints: Int): PostureData {
        val keyPointList = Array(numKeyPoints) { KeyPoint() }
        var totalScore = 0.0f

        for((index, it) in enumValues<BodyPart>().withIndex()) {
            totalScore += confidenceScores[index]

            keyPointList[index].apply {
                bodyPart = it
                position.x = xCoords[index]
                position.y = yCoords[index]
                score = confidenceScores[index]
            }
        }

        return PostureData(keyPointList.toList(), totalScore / numKeyPoints, bitmap.height, bitmap.width)
    }
}
