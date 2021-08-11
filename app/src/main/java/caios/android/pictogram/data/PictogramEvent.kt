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
    PictogramEvent.DIVING,
    PictogramEvent.RHYTHMIC_GYMNASTICS,
    PictogramEvent.RUGBY,
    PictogramEvent.TAEKWONDO,
    PictogramEvent.WRESTLING
)