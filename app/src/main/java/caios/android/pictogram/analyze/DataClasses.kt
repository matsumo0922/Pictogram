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

enum class PictogramEvent {
    ARCHERY,
    WEIGHTLIFTING,
    VOLLEYBALL,
    TENNIS,
    ATHLETICS,
    BADMINTON,
    BASKETBALL,
    BEACH_VOLLEYBALL,
    BOXING,
    CYCLING,
    DIVING,
    FENCING,
    FOOTBALL,
    GOLF,
    HANDBALL,
    HOCKEY,
    RHYTHMIC_GYMNASTICS,
    RUGBY,
    SHOOTING,
    TABLE_TENNIS,
    TAEKWONDO,
    WRESTLING
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

val pictogramPartsJoint = listOf(
    Pair(BodyPart.LEFT_WRIST, BodyPart.LEFT_ELBOW),
    Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER),
    Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
    Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
    Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
    Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
    Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
    Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
)

val pictogramEventName = mapOf(
    PictogramEvent.ARCHERY to "Archery",
    PictogramEvent.WEIGHTLIFTING to "Weightlifting",
    PictogramEvent.VOLLEYBALL to "Volleyball",
    PictogramEvent.TENNIS to "Tennis",
    PictogramEvent.ATHLETICS to "Athletics",
    PictogramEvent.BADMINTON to "Badminton",
    PictogramEvent.BASKETBALL to "Basketball",
    PictogramEvent.BEACH_VOLLEYBALL to "BeachVolleyball",
    PictogramEvent.BOXING to "Boxing",
    PictogramEvent.CYCLING to "Cycling",
    PictogramEvent.DIVING to "Diving",
    PictogramEvent.FENCING to "Fencing",
    PictogramEvent.FOOTBALL to "Football",
    PictogramEvent.GOLF to "Golf",
    PictogramEvent.HANDBALL to "Handball",
    PictogramEvent.HOCKEY to "Hockey",
    PictogramEvent.RHYTHMIC_GYMNASTICS to "RhythmicGymnastics",
    PictogramEvent.RUGBY to "Rugby",
    PictogramEvent.SHOOTING to "Shooting",
    PictogramEvent.TABLE_TENNIS to "TableTennis",
    PictogramEvent.TAEKWONDO to "Taekwondo",
    PictogramEvent.WRESTLING to "Wrestling"
)

//debug
val pictogramEventDisables = listOf(
    PictogramEvent.TENNIS,
    PictogramEvent.GOLF,
    PictogramEvent.DIVING,
    PictogramEvent.RUGBY,
    PictogramEvent.WRESTLING
)

const val THRESHOLD_DEGREE_MATCHES = 1.30f

const val MODEL_WIDTH = 257
const val MODEL_HEIGHT = 257