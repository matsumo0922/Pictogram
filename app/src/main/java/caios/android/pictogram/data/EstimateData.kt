package caios.android.pictogram.data

data class PostureData (
    val keyPoints: List<KeyPoint>,
    val score: Float,
    val parentHeight: Int,
    val parentWidth: Int
)

data class KeyPoint (
    var bodyPart: BodyPart = BodyPart.NOSE,
    var position: Position = Position(),
    var score: Float = 0.0f
)

data class Position(
    var x: Int = 0,
    var y: Int = 0
)

enum class Device {
    CPU, GPU, NNAPI
}

enum class Model {
    MOVENET_LIGHTNING, MOVENET_THUNDER, POSENET
}

const val THRESHOLD_DEGREE_MATCHES = 2.80f

const val MODEL_WIDTH = 257
const val MODEL_HEIGHT = 257