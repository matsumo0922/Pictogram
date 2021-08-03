package caios.android.pictogram.analyze

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

enum class BodyPart {
    NOSE,
    LEFT_EYE,
    RIGHT_EYE,
    LEFT_EAR,
    RIGHT_EAR,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE
}

const val MODEL_WIDTH = 257
const val MODEL_HEIGHT = 257