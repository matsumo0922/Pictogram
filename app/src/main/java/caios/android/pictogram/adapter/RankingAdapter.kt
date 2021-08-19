package caios.android.pictogram.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import caios.android.pictogram.R
import caios.android.pictogram.data.getEventResource
import caios.android.pictogram.databinding.ViewRankingListBinding
import caios.android.pictogram.game.ClearData
import caios.android.pictogram.game.sortedRanking
import caios.android.pictogram.global.ranking
import java.text.SimpleDateFormat
import java.util.*

class RankingHolder(val binding: ViewRankingListBinding): RecyclerView.ViewHolder(binding.root)

class RankingAdapter(
    private val context: Context,
    private val dataList: List<Pair<ClearData, Long>>,
): RecyclerView.Adapter<RankingHolder>() {

    private val rankingList = dataList.map { it.first }.sortedRanking()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingHolder {
        return RankingHolder(ViewRankingListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RankingHolder, position: Int) {
        with(holder) {
            val data = dataList[position]
            val clearData = data.first

            binding.eventImage1.setImageResource(getEventResource(clearData.eventData[0].event))
            binding.eventImage2.setImageResource(getEventResource(clearData.eventData[1].event))
            binding.eventImage3.setImageResource(getEventResource(clearData.eventData[2].event))
            binding.eventImage4.setImageResource(getEventResource(clearData.eventData[3].event))
            binding.eventImage5.setImageResource(getEventResource(clearData.eventData[4].event))

            binding.eventTime1.text = clearData.eventData[0].time.toSecond()
            binding.eventTime2.text = clearData.eventData[1].time.toSecond()
            binding.eventTime3.text = clearData.eventData[2].time.toSecond()
            binding.eventTime4.text = clearData.eventData[3].time.toSecond()
            binding.eventTime5.text = clearData.eventData[4].time.toSecond()

            binding.challengerName.text = clearData.challengerName
            binding.totalTime.text = "%.2f".format(data.second.toFloat() / 1000)
            binding.date.text = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date(clearData.date))

            when(val ranking = rankingList.indexOfFirst { it == clearData } + 1) {
                1 -> {
                    binding.medalImage.setImageResource(R.drawable.vec_medal_gold)
                    binding.rankingLayout.visibility = View.GONE
                    binding.medalImage.visibility = View.VISIBLE
                }
                2 -> {
                    binding.medalImage.setImageResource(R.drawable.ic_medal_silver)
                    binding.rankingLayout.visibility = View.GONE
                    binding.medalImage.visibility = View.VISIBLE
                }
                3 -> {
                    binding.medalImage.setImageResource(R.drawable.vec_medal_bronze)
                    binding.rankingLayout.visibility = View.GONE
                    binding.medalImage.visibility = View.VISIBLE
                }
                else -> {
                    binding.ranking.text = ranking.toString()
                    binding.rankingLayout.visibility = View.VISIBLE
                    binding.medalImage.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    private fun Long.toSecond(): String {
        return "%.2f ${context.getString(R.string.second)}".format(this.toFloat() / 1000)
    }
}