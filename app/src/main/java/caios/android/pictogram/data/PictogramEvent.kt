package caios.android.pictogram.data

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

// Json側との紐付け
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

// 判定精度や人体の構造的に無理なやつを除く（せっかく準備したけど）
val pictogramEventDisables = listOf(
    PictogramEvent.TENNIS,
    PictogramEvent.BEACH_VOLLEYBALL,
    PictogramEvent.RUGBY,
    PictogramEvent.WRESTLING
)