package caios.android.pictogram.game

import android.content.Context
import caios.android.pictogram.data.PictogramEvent
import caios.android.pictogram.global.setting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

data class ClearData(
    val date: Long,
    val eventData: List<EventData>,
    var challengerName: String = "Unknown Challenger"
)

data class EventData(
    val event: PictogramEvent,
    var time: Long = -1L
)


class RankingControl(private val context: Context) {

    private val gson = Gson()

    fun setRanking(eventDataList: List<EventData>, date: Long = Calendar.getInstance().time.time): Int {
        val clearData = ClearData(date, eventDataList)
        val ranking = getRanking(clearData)

        setting.setString(PREFERENCE, clearData.date.toString(), gson.toJson(clearData))

        return ranking
    }

    fun setChallengerName(name: String, date: Long) {
        val clearData = getData(date)?.apply {
            challengerName = name
        } ?: return

        setting.setString(PREFERENCE, clearData.date.toString(), gson.toJson(clearData))
    }

    fun getAllData(): List<ClearData> {
        return setting.getAllPreferenceSpecificItem<String>(PREFERENCE).mapNotNull {
            it.key.toLongOrNull()?.let { date ->
                gson.fromJson(it.value, object : TypeToken<ClearData>() {}.type)
            }
        }
    }

    fun getData(date: Long): ClearData? {
        return setting.getStringOrNull(PREFERENCE, date.toString())?.let {
            gson.fromJson(it, object : TypeToken<ClearData>() {}.type)
        }
    }

    fun getData(ranking: Int): ClearData? {
        val allData = getAllData()
        val sortedList = allData.sortedRanking()
        return sortedList.elementAtOrNull(ranking - 1)
    }

    private fun getRanking(clearData: ClearData): Int {
        val allData = getAllData().toMutableList()

        if(!allData.contains(clearData)){
            allData.add(clearData)
        }

        val sortedList = allData.sortedRanking()
        return (sortedList.indexOfFirst { it == clearData } + 1)
    }

    companion object {
        const val PREFERENCE = "CAIOS-RANKING"
    }
}

fun List<ClearData>.sortedRanking(): List<ClearData> {
    val addGameTimeList = map { Pair(it, it.eventData.sumOf { event -> event.time }) }
    return addGameTimeList.sortedBy { it.second }.map { it.first }
}