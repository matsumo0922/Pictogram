package caios.android.pictogram.data

import androidx.annotation.DrawableRes
import caios.android.pictogram.R

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

// Jsonとの紐付け
fun getEventName(event: PictogramEvent): String {
    return when(event) {
        PictogramEvent.ARCHERY          -> "Archery"
        PictogramEvent.WEIGHTLIFTING    -> "Weightlifting"
        PictogramEvent.VOLLEYBALL       -> "Volleyball"
        PictogramEvent.TENNIS           -> "Tennis"
        PictogramEvent.ATHLETICS        -> "Athletics"
        PictogramEvent.BADMINTON        -> "Badminton"
        PictogramEvent.BASKETBALL       -> "Basketball"
        PictogramEvent.BEACH_VOLLEYBALL -> "BeachVolleyball"
        PictogramEvent.BOXING           -> "Boxing"
        PictogramEvent.CYCLING          -> "Cycling"
        PictogramEvent.DIVING           -> "Diving"
        PictogramEvent.FENCING          -> "Fencing"
        PictogramEvent.FOOTBALL         -> "Football"
        PictogramEvent.GOLF             -> "Golf"
        PictogramEvent.HANDBALL         -> "Handball"
        PictogramEvent.HOCKEY           -> "Hockey"
        PictogramEvent.RHYTHMIC_GYMNASTICS -> "RhythmicGymnastics"
        PictogramEvent.RUGBY            -> "Rugby"
        PictogramEvent.SHOOTING         -> "Shooting"
        PictogramEvent.TABLE_TENNIS     -> "TableTennis"
        PictogramEvent.TAEKWONDO        -> "Taekwondo"
        PictogramEvent.WRESTLING        -> "Wrestling"
    }
}

// リソースとの紐付け
@DrawableRes fun getEventResource(event: PictogramEvent): Int {
    return when(event) {
        PictogramEvent.ARCHERY             -> R.drawable.vec_archery
        PictogramEvent.WEIGHTLIFTING       -> R.drawable.vec_weightlifting
        PictogramEvent.VOLLEYBALL          -> R.drawable.vec_volleyball
        PictogramEvent.TENNIS              -> R.drawable.vec_tennis
        PictogramEvent.ATHLETICS           -> R.drawable.vec_athletics
        PictogramEvent.BADMINTON           -> R.drawable.vec_badminton
        PictogramEvent.BASKETBALL          -> R.drawable.vec_basketball
        PictogramEvent.BEACH_VOLLEYBALL    -> R.drawable.vec_beach_volleyball
        PictogramEvent.BOXING              -> R.drawable.vec_boxing
        PictogramEvent.CYCLING             -> R.drawable.vec_cycling_road
        PictogramEvent.DIVING              -> R.drawable.vec_diving
        PictogramEvent.FENCING             -> R.drawable.vec_fencing
        PictogramEvent.FOOTBALL            -> R.drawable.vec_football
        PictogramEvent.GOLF                -> R.drawable.vec_golf
        PictogramEvent.HANDBALL            -> R.drawable.vec_handball
        PictogramEvent.HOCKEY              -> R.drawable.vec_hockey
        PictogramEvent.RHYTHMIC_GYMNASTICS -> R.drawable.vec_rhythmic_gymnastics
        PictogramEvent.RUGBY               -> R.drawable.vec_rugby_sevens
        PictogramEvent.SHOOTING            -> R.drawable.vec_shooting
        PictogramEvent.TABLE_TENNIS        -> R.drawable.vec_table_tennis
        PictogramEvent.TAEKWONDO           -> R.drawable.vec_taekwondo
        PictogramEvent.WRESTLING           -> R.drawable.vec_weightlifting
    }
}

// 判定精度や人体の構造的に無理なやつを除く（せっかく準備したけど）
val pictogramEventDisables = listOf(
    PictogramEvent.TENNIS,
    PictogramEvent.DIVING,
    PictogramEvent.RHYTHMIC_GYMNASTICS,
    PictogramEvent.RUGBY,
    PictogramEvent.SHOOTING,
    PictogramEvent.WRESTLING
)