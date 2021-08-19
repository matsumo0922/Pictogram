package caios.android.pictogram.game

import android.content.Context
import caios.android.pictogram.data.PictogramEvent
import caios.android.pictogram.global.setting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class RankingData(
    val ranking: Int,
    val time: Float,
    val date: Long,
    val events: List<PictogramEvent>
    )


class RankingControl(private val context: Context) {

    private val gson = Gson()

    fun setRanking(time: Float, date: Long): Int? {
        val rankingList = getAllRanking().toMutableList()
        val dataList = rankingList.map { Pair(it.time, it.date) }.toMutableList()

        dataList.add(time to date)

        val newRankingList = resetRanking(dataList)

        return newRankingList.find { it.date == date }?.ranking
    }

    fun getAllRanking(): List<RankingData> {
        val rankingStr = setting.getString(PREFERENCE, "Ranking", "[]")
        val rankingArray = gson.fromJson<Collection<RankingData>>(rankingStr, object : TypeToken<Collection<RankingData>>(){}.type)
        return rankingArray.toList()
    }

    private fun resetRanking(dataList: List<Pair<Float, Long>>): List<RankingData> {
        val sortedList = dataList.sortedBy { it.first }
        val sortedRankingDataList = sortedList.mapIndexed { index, data -> RankingData(index + 1, data.first, data.second) }
        val json = gson.toJson(sortedRankingDataList)

        setting.setString(PREFERENCE, "Ranking", json)

        return sortedRankingDataList
    }

    companion object {
        const val PREFERENCE = "CAIOS-RANKING"
    }
}