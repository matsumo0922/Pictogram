package caios.android.pictogram.analyze

import com.github.kittinunf.fuel.core.Body

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

enum class PictogramEvent {
    ARCHERY
}

val bodyPartsJoint = listOf(
    Pair(BodyPart.LEFT_WRIST, BodyPart.LEFT_ELBOW),
    Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER),
    Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
    Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
    Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
    Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
    Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
    Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_SHOULDER),
    Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
    Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
    Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
    Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
)

val bodyPartsName = mapOf(
    BodyPart.NOSE to "Nose",
    BodyPart.RIGHT_WRIST to "RightWrist",
    BodyPart.RIGHT_ELBOW to "RightElbow",
    BodyPart.RIGHT_SHOULDER to "RightShoulder",
    BodyPart.LEFT_WRIST to "LeftWrist",
    BodyPart.LEFT_ELBOW to "LeftElbow",
    BodyPart.LEFT_SHOULDER to "LeftShoulder",
    BodyPart.RIGHT_HIP to "RightHip",
    BodyPart.RIGHT_KNEE to "RightKnee",
    BodyPart.RIGHT_ANKLE to "RightAnkle",
    BodyPart.LEFT_HIP to "LeftHip",
    BodyPart.LEFT_KNEE to "LeftKnee",
    BodyPart.LEFT_ANKLE to "LeftAnkle",
)

val pictogramEventName = mapOf(
    PictogramEvent.ARCHERY to "Archery"
)

const val MODEL_WIDTH = 257
const val MODEL_HEIGHT = 257