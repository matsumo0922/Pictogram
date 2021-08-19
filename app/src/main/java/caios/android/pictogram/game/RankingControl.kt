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

    fun setRanking(eventDataList: List<EventData>): Int {
        val clearData = ClearData(Calendar.getInstance().time.time, eventDataList)
        val ranking = getRanking(clearData)

        setting.setString(PREFERENCE, clearData.date.toString(), gson.toJson(clearData.eventData))

        return ranking
    }

    fun getAllData(): List<ClearData> {
        return setting.getAllPreferenceSpecificItem<String>(PREFERENCE).mapNotNull {
            it.key.toLongOrNull()?.let { date ->
                ClearData(date, gson.fromJson<Collection<EventData>>(it.value, object : TypeToken<Collection<EventData>>() {}.type).toList())
            }
        }
    }

    private fun getData(date: Long): ClearData? {
        return setting.getStringOrNull(PREFERENCE, date.toString())?.let {
            ClearData(date, gson.fromJson<Collection<EventData>>(it, object : TypeToken<Collection<EventData>>() {}.type).toList())
        }
    }

    private fun getData(ranking: Int): ClearData? {
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

// どうしても拡張関数として使いたいのでグローバル
fun List<ClearData>.sortedRanking(): List<ClearData> {
    val addGameTimeList = map { Pair(it, it.eventData.sumOf { event -> event.time }) }
    return addGameTimeList.sortedBy { it.second }.map { it.first }
}